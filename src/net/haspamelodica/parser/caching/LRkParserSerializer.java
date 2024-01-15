package net.haspamelodica.parser.caching;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

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
		out.writeInt(parser.getLookaheadSize());

		// create mapping of nonterminal <-> id and terminal <-> id
		Map<Nonterminal, Integer> idsByNonterminal = new HashMap<>();
		List<Nonterminal> nonterminalsById = new ArrayList<>();
		int nonterminalCount = 0;
		Map<Terminal<?>, Integer> idsByTerminal = new HashMap<>();
		List<Terminal<?>> terminalsById = new ArrayList<>();
		int terminalCount = 0;
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
						.flatMap(Set::stream));
		for(Symbol symbol : (Iterable<Symbol>) allSymbols::iterator)
			switch(symbol.getType())
			{
				case NONTERMINAL ->
				{
					Nonterminal nonterminal = (Nonterminal) symbol;
					if(idsByNonterminal.get(nonterminal) == null)
					{
						idsByNonterminal.put(nonterminal, nonterminalCount);
						nonterminalsById.add(nonterminal);
						nonterminalCount ++;
					}
				}
				case TERMINAL ->
				{
					Terminal<?> terminal = (Terminal<?>) symbol;
					if(idsByTerminal.get(terminal) == null)
					{
						idsByTerminal.put(terminal, terminalCount);
						terminalsById.add(terminal);
						terminalCount ++;
					}
				}
			}
		out.writeInt(nonterminalCount);
		out.writeInt(terminalCount);
		Map<Symbol, Integer> idsBySymbol = new HashMap<>(idsByTerminal);
		int terminalCountFinal = terminalCount;
		idsByNonterminal.forEach((nonterminal, id) -> idsBySymbol.put(nonterminal, id + terminalCountFinal));

		// terminals
		for(Terminal<?> terminal : terminalsById)
			serializeTerminal.accept(terminal, out);

		// nonterminals
		for(Nonterminal nonterminal : nonterminalsById)
			serializeNonterminal.accept(nonterminal, out);

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
	private static Stream<Symbol> streamProductionSymbols(Production production)
	{
		return Stream.concat(Stream.of(production.getLhs()), production.getRhs().getSymbols().stream());
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

		int nonterminalCount = in.readInt();
		int terminalCount = in.readInt();

		List<Terminal<?>> terminalsById = new ArrayList<>();
		for(int i = 0; i < terminalCount; i ++)
			terminalsById.add(deserializeTerminal.apply(in));
		List<Nonterminal> nonterminalsById = new ArrayList<>();
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
		return new GenericLRkParser<>(0, gotoTable, actionTable, lookaheadSize);
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

	private LRkParserSerializer()
	{}
}
