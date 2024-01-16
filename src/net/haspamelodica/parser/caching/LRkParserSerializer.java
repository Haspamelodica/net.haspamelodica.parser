package net.haspamelodica.parser.caching;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.haspamelodica.parser.grammar.ContextFreeGrammar;
import net.haspamelodica.parser.grammar.Nonterminal;
import net.haspamelodica.parser.grammar.Production;
import net.haspamelodica.parser.grammar.RightHandSide;
import net.haspamelodica.parser.grammar.Symbol;
import net.haspamelodica.parser.grammar.Terminal;
import net.haspamelodica.parser.parser.lrk.GenericLRkParser;
import net.haspamelodica.parser.parser.lrk.IOBiConsumer;
import net.haspamelodica.parser.parser.lrk.IOFunction;
import net.haspamelodica.parser.parser.lrk.Word;
import net.haspamelodica.parser.parser.lrk.action.Action;
import net.haspamelodica.parser.parser.lrk.action.Action.ActionType;
import net.haspamelodica.parser.parser.lrk.action.ErrorAction;
import net.haspamelodica.parser.parser.lrk.action.FinishAction;
import net.haspamelodica.parser.parser.lrk.action.ReduceAction;
import net.haspamelodica.parser.parser.lrk.action.ShiftAction;

public class LRkParserSerializer
{
	private static final int PARSER_VERSION_MAGIC = 0xd1d7df10;

	public static <STATE> void serialize(GenericLRkParser<STATE> parser,
			IOBiConsumer<Terminal<?>, DataOutputStream> serializeTerminal,
			IOBiConsumer<Nonterminal, DataOutputStream> serializeNonterminal,
			OutputStream out) throws IOException
	{
		serialize(parser, serializeTerminal, serializeNonterminal, new DataOutputStream(out));
	}
	public static <STATE> void serialize(GenericLRkParser<STATE> parser,
			IOBiConsumer<Terminal<?>, DataOutputStream> serializeTerminal,
			IOBiConsumer<Nonterminal, DataOutputStream> serializeNonterminal,
			DataOutputStream out) throws IOException
	{
		out.writeInt(PARSER_VERSION_MAGIC);

		Symbols symbols = categorizeSymbols(parser);
		// lookahead size and whether we have a generated start symbol
		boolean hasGeneratedStart = symbols.generatedStartSymbol().isPresent();
		out.writeInt(parser.getLookaheadSize() * (hasGeneratedStart ? -1 : 1));

		// create mapping of id -> nonterminal (except generated start) and id -> terminal (except EOF)
		List<Nonterminal> nonterminalsById = List.copyOf(symbols.nonterminalsExceptGeneratedStart());
		List<Terminal<?>> terminalsByIdModifiable = new ArrayList<>(symbols.terminals());

		out.writeInt(nonterminalsById.size());
		out.writeInt(terminalsByIdModifiable.size());

		// terminals
		for(Terminal<?> terminal : symbols.terminals())
			serializeTerminal.accept(terminal, out);

		// nonterminals (except generated start)
		for(Nonterminal nonterminal : nonterminalsById)
			serializeNonterminal.accept(nonterminal, out);

		terminalsByIdModifiable.add(0, Terminal.EOF);
		List<Terminal<?>> terminalsById = List.copyOf(terminalsByIdModifiable);

		if(hasGeneratedStart)
		{
			List<Nonterminal> nonterminalsByIdNew = new ArrayList<>(nonterminalsById.size() + 1);
			nonterminalsByIdNew.add(symbols.generatedStartSymbol().get());
			nonterminalsByIdNew.addAll(nonterminalsById);
			nonterminalsById = List.copyOf(nonterminalsByIdNew);
		}

		// create mapping of nonterminal -> id and terminal -> id
		Map<Nonterminal, Integer> idsByNonterminal = IntStream.range(0, nonterminalsById.size())
				.boxed().collect(Collectors.toUnmodifiableMap(nonterminalsById::get, Function.identity()));
		Map<Terminal<?>, Integer> idsByTerminal = IntStream.range(0, terminalsById.size())
				.boxed().collect(Collectors.toUnmodifiableMap(terminalsById::get, Function.identity()));
		Map<Symbol, Integer> idsBySymbol = new HashMap<>(idsByTerminal);
		idsByNonterminal.forEach((nonterminal, id) -> idsBySymbol.put(nonterminal, id + terminalsById.size()));

		// create mapping of state <-> id
		Map<STATE, Integer> idsByState = new HashMap<>();
		List<STATE> statesById = new ArrayList<>();
		idsByState.put(parser.getInitialState(), 0);
		statesById.add(parser.getInitialState());
		int stateCount = 1;
		//TODO theoretically, we have no guarantee that these are all states
		for(STATE state : parser.getActionTable().keySet())
			if(state != parser.getInitialState())
			{
				idsByState.put(state, stateCount);
				statesById.add(state);
				stateCount ++;
			}
		out.writeInt(stateCount);

		// goto table
		for(STATE state : statesById)
		{
			Map<Symbol, STATE> gotoEntry = parser.getGotoTable().get(state);
			if(gotoEntry == null)
				gotoEntry = Map.of();
			out.writeInt(gotoEntry.size());
			int i = 0;
			for(Entry<Symbol, STATE> entry : gotoEntry.entrySet())
			{
				out.writeInt(idsBySymbol.get(entry.getKey()));
				out.writeInt(idsByState.get(entry.getValue()));
				i ++;
			}
			if(i != gotoEntry.size())
				throw new IllegalArgumentException("Map isn't sane");
		}

		// action table
		for(STATE state : statesById)
		{
			Map<Word, Action> actionEntry = parser.getActionTable().get(state);
			if(actionEntry == null)
				actionEntry = Map.of();
			out.writeInt(actionEntry.size());
			int i = 0;
			for(Entry<Word, Action> entry : actionEntry.entrySet())
			{
				serializeWord(entry.getKey(), idsBySymbol, out);
				out.writeInt(entry.getValue().getType().ordinal());
				switch(entry.getValue().getType())
				{
					case SHIFT, ERROR ->
							{
							}
					case FINISH ->
					{
						FinishAction entryCasted = (FinishAction) entry.getValue();
						serializeProduction(entryCasted.getProduction(), idsBySymbol, idsByNonterminal, out);
						out.writeBoolean(entryCasted.dontIncludeStartSymbol());
					}
					case REDUCE -> serializeProduction(((ReduceAction) entry.getValue()).getProduction(), idsBySymbol, idsByNonterminal, out);
				}
				i ++;
			}
			if(i != actionEntry.size())
				throw new IllegalArgumentException("Map isn't sane");
		}
	}
	public static GenericLRkParser<?> deserialize(InputStream in,
			IOFunction<DataInputStream, Terminal<?>> deserializeTerminal,
			IOFunction<DataInputStream, Nonterminal> deserializeNonterminal) throws IOException, VersionMagicMismatchException
	{
		return deserialize(new DataInputStream(in), deserializeTerminal, deserializeNonterminal);
	}
	public static GenericLRkParser<?> deserialize(DataInputStream in,
			IOFunction<DataInputStream, Terminal<?>> deserializeTerminal,
			IOFunction<DataInputStream, Nonterminal> deserializeNonterminal) throws IOException, VersionMagicMismatchException
	{
		checkVersion(PARSER_VERSION_MAGIC, in.readInt(), "Parser version mismatch");
		int lookaheadSize = in.readInt();
		boolean hasGeneratedStart = lookaheadSize < 0;
		lookaheadSize = Math.abs(lookaheadSize);

		int nonterminalCount = in.readInt();
		int terminalCount = in.readInt();

		List<Terminal<?>> terminalsById = new ArrayList<>();
		terminalsById.add(Terminal.EOF);
		for(int i = 0; i < terminalCount; i ++)
			terminalsById.add(deserializeTerminal.apply(in));
		List<Nonterminal> nonterminalsById = new ArrayList<>();
		Nonterminal generatedStartNonterminal;
		if(hasGeneratedStart)
		{
			generatedStartNonterminal = new Nonterminal("S");
			nonterminalsById.add(generatedStartNonterminal);
		} else
			generatedStartNonterminal = null;
		for(int i = 0; i < nonterminalCount; i ++)
			nonterminalsById.add(deserializeNonterminal.apply(in));
		List<Symbol> symbolsById = new ArrayList<>(terminalsById);
		symbolsById.addAll(nonterminalsById);

		int stateCount = in.readInt();

		Map<Integer, Map<Symbol, Integer>> gotoTable = new HashMap<>();
		for(int gotoEntryI = 0; gotoEntryI < stateCount; gotoEntryI ++)
		{
			Map<Symbol, Integer> gotoEntry = new HashMap<>();
			int entryCount = in.readInt();
			for(int i = 0; i < entryCount; i ++)
			{
				Symbol symbol = symbolsById.get(in.readInt());
				gotoEntry.put(symbol, in.readInt());
			}
			gotoTable.put(gotoEntryI, Map.copyOf(gotoEntry));
		}

		Map<Integer, Map<Word, Action>> actionTable = new HashMap<>();
		for(int actionEntryI = 0; actionEntryI < stateCount; actionEntryI ++)
		{
			Map<Word, Action> actionEntry = new HashMap<>();
			int entryCount = in.readInt();
			for(int i = 0; i < entryCount; i ++)
			{
				Word word = deserializeWord(terminalsById, in);
				actionEntry.put(word, switch(ActionType.values()[in.readInt()])
				{
					case SHIFT -> ShiftAction.INSTANCE;
					case REDUCE -> new ReduceAction(deserializeProduction(symbolsById, nonterminalsById, in));
					case FINISH ->
					{
						Production production = deserializeProduction(symbolsById, nonterminalsById, in);
						yield new FinishAction(production, in.readBoolean());
					}
					case ERROR -> ErrorAction.INSTANCE;
				});
			}
			actionTable.put(actionEntryI, Map.copyOf(actionEntry));
		}

		// serialize() ensures the initial state always gets ID 0
		return new GenericLRkParser<>(0, generatedStartNonterminal, gotoTable, actionTable, lookaheadSize);
	}

	private static void serializeWord(Word word, Map<Symbol, Integer> idsBySymbol, DataOutputStream out) throws IOException
	{
		out.writeInt(word.getLength());
		for(Terminal<?> terminal : word.getTerminals())
			out.writeInt(idsBySymbol.get(terminal));
	}

	private static Word deserializeWord(List<Terminal<?>> terminalsById, DataInputStream in) throws IOException
	{
		int length = in.readInt();
		List<Terminal<?>> terminals = new ArrayList<>(length);
		for(int i = 0; i < length; i ++)
			terminals.add(terminalsById.get(in.readInt()));
		return new Word(terminals);
	}

	private static void serializeProduction(Production production, Map<Symbol, Integer> idsBySymbol, Map<Nonterminal, Integer> idsByNonterminal, DataOutputStream out) throws IOException
	{
		out.writeInt(idsByNonterminal.get(production.getLhs()));
		List<Symbol> rhs = production.getRhs().getSymbols();
		out.writeInt(rhs.size());
		for(Symbol symbol : rhs)
			out.writeInt(idsBySymbol.get(symbol));
	}

	private static Production deserializeProduction(List<Symbol> symbolsById, List<Nonterminal> nonterminalsById, DataInputStream in) throws IOException
	{
		Nonterminal lhs = nonterminalsById.get(in.readInt());
		int rhsSize = in.readInt();
		List<Symbol> rhs = new ArrayList<>(rhsSize);
		for(int i = 0; i < rhsSize; i ++)
			rhs.add(symbolsById.get(in.readInt()));
		return new Production(lhs, new RightHandSide(rhs));
	}

	private static void checkVersion(int expected, int actual, String message) throws IOException, VersionMagicMismatchException
	{
		if(actual != expected)
			throw new VersionMagicMismatchException(expected, actual, message);
	}

	public static record Symbols(Set<Nonterminal> nonterminalsExceptGeneratedStart, Set<Terminal<?>> terminals, Optional<Nonterminal> generatedStartSymbol)
	{}

	public static Symbols categorizeSymbols(GenericLRkParser<?> parser)
	{
		return categorizeSymbols(streamSymbolsExceptGeneratedStartAndEOF(parser), Optional.ofNullable(parser.getGeneratedStartSymbolIfAny()));
	}

	public static Symbols categorizeSymbols(ContextFreeGrammar grammar)
	{
		return categorizeSymbols(grammar.getAllSymbols().stream(), null);
	}

	public static Symbols categorizeSymbols(Stream<Symbol> symbols, Optional<Nonterminal> generatedStartSymbol)
	{
		Set<Nonterminal> nonterminals = new HashSet<>();
		Set<Terminal<?>> terminals = new HashSet<>();
		for(Symbol symbol : (Iterable<Symbol>) symbols::iterator)
			switch(symbol.getType())
			{
				case NONTERMINAL -> nonterminals.add((Nonterminal) symbol);
				case TERMINAL -> terminals.add((Terminal<?>) (Terminal<?>) symbol);
			}
		return new Symbols(nonterminals, terminals, generatedStartSymbol);
	}

	private static <STATE> Stream<Symbol> streamSymbolsExceptGeneratedStartAndEOF(GenericLRkParser<STATE> parser)
	{
		Stream<Symbol> allSymbols = Stream.concat(
				parser
						.getActionTable()
						.values()
						.stream()
						.map(Map::entrySet)
						.flatMap(Set::stream)
						.flatMap(e -> Stream.concat(e.getKey().getTerminals().stream(), switch(e.getValue().getType())
						{
							case ERROR, SHIFT -> Stream.empty();
							case REDUCE -> streamProductionSymbols(((ReduceAction) e.getValue()).getProduction());
							case FINISH -> streamProductionSymbols(((FinishAction) e.getValue()).getProduction());
						})),
				parser
						.getGotoTable()
						.values()
						.stream()
						.map(Map::keySet)
						.flatMap(Set::stream))
				.filter(s -> !s.equals(Terminal.EOF));
		Nonterminal generatedStartSymbol = parser.getGeneratedStartSymbolIfAny();
		if(generatedStartSymbol == null)
			return allSymbols;
		return allSymbols.filter(s -> !s.equals(generatedStartSymbol));
	}
	private static Stream<Symbol> streamProductionSymbols(Production production)
	{
		return Stream.concat(Stream.of(production.getLhs()), production.getRhs().getSymbols().stream());
	}
	private LRkParserSerializer()
	{}
}
