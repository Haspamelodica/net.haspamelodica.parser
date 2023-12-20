package net.haspamelodica.parser.parser.lrk.canonicalautomaton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import net.haspamelodica.parser.grammar.ContextFreeGrammar;
import net.haspamelodica.parser.grammar.Nonterminal;
import net.haspamelodica.parser.grammar.Production;
import net.haspamelodica.parser.grammar.Symbol;
import net.haspamelodica.parser.grammar.Symbol.SymbolType;
import net.haspamelodica.parser.grammar.Terminal;
import net.haspamelodica.parser.parser.lrk.ConcatKHelper;
import net.haspamelodica.parser.parser.lrk.Item;
import net.haspamelodica.parser.parser.lrk.LookaheadItem;
import net.haspamelodica.parser.parser.lrk.Word;

public class CanonicalAutomatonGenerator
{
	private final ContextFreeGrammar	grammar;
	private final int					lookaheadSize;
	private final ConcatKHelper			concatKHelper;

	private final Set<State>		allStates;
	private final Set<Transition>	transitions;

	public static CanonicalAutomaton generate(ContextFreeGrammar grammar, int lookaheadSize, ConcatKHelper concatKHelper)
	{
		return new CanonicalAutomatonGenerator(grammar, lookaheadSize, concatKHelper).generate();
	}

	private CanonicalAutomatonGenerator(ContextFreeGrammar grammar, int lookaheadSize, ConcatKHelper concatKHelper)
	{
		this.grammar = grammar;
		this.lookaheadSize = lookaheadSize;
		this.concatKHelper = concatKHelper;

		this.allStates = new HashSet<>();
		this.transitions = new HashSet<>();
	}

	private CanonicalAutomaton generate()
	{
		State initialState = constructInitialState();
		allStates.add(initialState);

		Queue<State> unfinishedStates = new LinkedList<>();
		unfinishedStates.add(initialState);
		for(State stateToExpand = unfinishedStates.poll(); stateToExpand != null; stateToExpand = unfinishedStates.poll())
		{
			Map<Symbol, Set<LookaheadItem>> itemsGroupedByNextSymbol = stateToExpand
					.getItems()
					.stream()
					.filter(i -> !i.getItem().isFinished())
					.collect(Collectors.groupingBy(i -> i.getItem().getNextSymbol(), Collectors.toSet()));

			for(Entry<Symbol, Set<LookaheadItem>> e : itemsGroupedByNextSymbol.entrySet())
			{
				Set<LookaheadItem> nextStateInitialItems = e
						.getValue()
						.stream()
						.map(LookaheadItem::advanceMarker)
						.collect(Collectors.toSet());
				State nextState = constructEpsilonClosureFromLookaheadItems(nextStateInitialItems);
				transitions.add(new Transition(stateToExpand, e.getKey(), nextState));
				if(allStates.add(nextState))
					unfinishedStates.add(nextState);
			}
		}

		return new CanonicalAutomaton(initialState, allStates, transitions);
	}

	private State constructInitialState()
	{
		Production initialStateProduction = findInitialStateProduction();

		Item initialStateItem = new Item(initialStateProduction, 0);
		Set<Word> eofLookahead = Set.of(Word.repeat(Terminal.EOF, lookaheadSize));
		State initialState = constructEpsilonClosureFromLookaheadItems(Set.of(new LookaheadItem(initialStateItem, eofLookahead)));
		return initialState;
	}

	private Production findInitialStateProduction()
	{
		Set<Production> initialStateProductions = grammar.getProductionsForLhs(grammar.getStartSymbol());
		if(initialStateProductions.size() != 1)
			throw new IllegalArgumentException("There is not exactly one production for the start symbol");
		Production initialStateProduction = initialStateProductions.stream().findAny().get();
		return initialStateProduction;
	}

	private State constructEpsilonClosureFromLookaheadItems(Set<LookaheadItem> initialItems)
	{
		Set<Item> initialItemsNoLookahead = new HashSet<>();
		for(LookaheadItem i : initialItems)
			initialItemsNoLookahead.add(i.getItem());

		Set<Item> allItems = new HashSet<>(initialItemsNoLookahead);
		Map<Item, Set<Item>> lookaheadInheriting = expandItems(allItems);

		Map<Item, Set<Word>> lookaheadPerItem = expandLookaheads(initialItems, initialItemsNoLookahead, allItems, lookaheadInheriting);

		Set<LookaheadItem> allLookaheadItems = lookaheadPerItem
				.entrySet()
				.stream()
				.map(e -> new LookaheadItem(e.getKey(), e.getValue()))
				.collect(Collectors.toSet());

		return new State(allLookaheadItems);
	}

	private Map<Item, Set<Word>> expandLookaheads(Set<LookaheadItem> initialItems, Set<Item> initialItemsNoLookahead, Set<Item> allItems, Map<Item, Set<Item>> lookaheadInheriting)
	{
		Map<Item, Set<Word>> lookaheadPerItem = new HashMap<>();
		for(Item i : allItems)
			lookaheadPerItem.put(i, new HashSet<>());
		for(LookaheadItem i : initialItems)
			lookaheadPerItem.put(i.getItem(), new HashSet<>(i.getAdmissibleLookaheads()));

		//TODO is this possible faster?
		//TODO almost a duplicate of ConcatKHelper.calculateFirstK
		Queue<Item> changedItems = new LinkedList<>(initialItemsNoLookahead);
		for(Item changedItem = changedItems.poll(); changedItem != null; changedItem = changedItems.poll())
		{
			Set<Word> changedItemLookahead = lookaheadPerItem.get(changedItem);
			if(changedItem.isFinished() || changedItem.getNextSymbol().getType() == SymbolType.TERMINAL)
				continue;
			List<Symbol> rhsSymbols = changedItem.getProduction().getRhs().getSymbols();
			List<Symbol> rhsAfterNextSym = rhsSymbols.subList(changedItem.getMarkerPosition() + 1, rhsSymbols.size());
			Set<Word> rhsAfterNextSymFirstK = concatKHelper.concatKSymbols(rhsAfterNextSym);
			Set<Word> newInheritedItemLookahead = concatKHelper.concatK(rhsAfterNextSymFirstK, changedItemLookahead);
			for(Item inheritingItem : lookaheadInheriting.get(changedItem))
				if(lookaheadPerItem.get(inheritingItem).addAll(newInheritedItemLookahead))
					changedItems.add(inheritingItem);
		}
		return lookaheadPerItem;
	}

	private Map<Item, Set<Item>> expandItems(Set<Item> allItems)
	{
		Map<Item, Set<Item>> lookaheadInheriting = new HashMap<>();

		Queue<Item> itemsToExpand = new LinkedList<>(allItems);
		for(Item itemToExpand = itemsToExpand.poll(); itemToExpand != null; itemToExpand = itemsToExpand.poll())
		{
			if(itemToExpand.isFinished())
				continue;
			Symbol nextSymbol = itemToExpand.getNextSymbol();

			if(nextSymbol.getType() == SymbolType.TERMINAL)
				continue;
			Nonterminal nextNonterminal = (Nonterminal) nextSymbol;

			HashSet<Item> lookaheadInheritingCurrentItem = new HashSet<>();
			lookaheadInheriting.put(itemToExpand, lookaheadInheritingCurrentItem);
			for(Production production : grammar.getProductionsForLhs(nextNonterminal))
			{
				Item expandedItem = new Item(production, 0);
				lookaheadInheritingCurrentItem.add(expandedItem);
				if(allItems.add(expandedItem))
					itemsToExpand.add(expandedItem);
			}
		}

		return lookaheadInheriting;
	}
}
