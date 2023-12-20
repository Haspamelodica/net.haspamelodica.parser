package net.haspamelodica.parser.parser.lrk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.haspamelodica.parser.grammar.ContextFreeGrammar;
import net.haspamelodica.parser.grammar.Production;
import net.haspamelodica.parser.grammar.Symbol;
import net.haspamelodica.parser.grammar.Symbol.SymbolType;
import net.haspamelodica.parser.parser.lrk.action.Action;
import net.haspamelodica.parser.parser.lrk.action.FinishAction;
import net.haspamelodica.parser.parser.lrk.action.ReduceAction;
import net.haspamelodica.parser.parser.lrk.action.ShiftAction;
import net.haspamelodica.parser.parser.lrk.canonicalautomaton.CanonicalAutomaton;
import net.haspamelodica.parser.parser.lrk.canonicalautomaton.CanonicalAutomatonGenerator;
import net.haspamelodica.parser.parser.lrk.canonicalautomaton.State;
import net.haspamelodica.parser.parser.lrk.canonicalautomaton.Transition;

public class LRkParserGenerator
{
	private final ContextFreeGrammar	grammar;
	private final boolean				grammarWasNormalized;
	private final int					lookaheadSize;

	private final ConcatKHelper			concatKHelper;
	private final CanonicalAutomaton	canonicalAutomaton;

	private final Map<State, Map<Word, Action>> actionTable;

	private LRkParserGenerator(ContextFreeGrammar grammar, int lookaheadSize)
	{
		this.grammar = grammar.normalize();
		this.grammarWasNormalized = !grammar.isNormalized();
		this.lookaheadSize = lookaheadSize;

		this.concatKHelper = new ConcatKHelper(this.grammar, lookaheadSize);
		this.canonicalAutomaton = CanonicalAutomatonGenerator.generate(this.grammar, lookaheadSize, concatKHelper);

		this.actionTable = new HashMap<>();
	}

	public static LRkParser generate(ContextFreeGrammar grammar, int lookaheadSize)
	{
		return new LRkParserGenerator(grammar, lookaheadSize).generate();
	}

	private LRkParser generate()
	{

		for(State state : canonicalAutomaton.getStates())
		{
			Map<Word, Action> actionsCurrentState = new HashMap<>();
			for(LookaheadItem item : state.getItems())
			{
				Item itemNoLookahead = item.getItem();
				Production production = itemNoLookahead.getProduction();
				if(itemNoLookahead.isFinished())
				{
					Action action;
					if(production.getLhs().equals(grammar.getStartSymbol()))
						action = new FinishAction(production, grammarWasNormalized);
					else
						action = new ReduceAction(production);
					for(Word lookahead : item.getAdmissibleLookaheads())
						setActionTableEntry(state, actionsCurrentState, action, lookahead);
				} else
				{
					Symbol nextSymbol = itemNoLookahead.getNextSymbol();
					if(nextSymbol.getType() != SymbolType.TERMINAL)
						continue;

					for(Word lookahead : getItemLookaheads(item))
						setActionTableEntry(state, actionsCurrentState, ShiftAction.INSTANCE, lookahead);
				}
			}
			actionTable.put(state, actionsCurrentState);
		}

		return new LRkParser(canonicalAutomaton, actionTable, lookaheadSize);
	}

	private void setActionTableEntry(State state, Map<Word, Action> actionsCurrentState, Action action, Word lookahead)
	{
		Action oldAction = actionsCurrentState.put(lookahead, action);
		if(oldAction != null && !action.equals(oldAction))
		{
			boolean hasMoreItems = false;
			String stateString = "";
			for(LookaheadItem item : state.getItems())
				if((item.getItem().isFinished() || item.getItem().getNextSymbol().getType() == SymbolType.TERMINAL)
						&& getItemLookaheads(item).contains(lookahead))
					stateString += item.toString() + "\n";
				else
					hasMoreItems = true;
			if(hasMoreItems)
				stateString += "[...]\n";
			List<Symbol> ambiguousInput = findAmbiguousInput(state, lookahead, oldAction, action);
			throw new IllegalArgumentException("The given grammar is not LR(" + lookaheadSize + "): Conflict in:\n" + stateString
					+ (lookaheadSize > 0 ? "for lookahead " + lookahead : "") + ". Example: " + ambiguousInput);
		}
	}

	private List<Symbol> findAmbiguousInput(State state, Word lookahead, Action oldAction, Action action)
	{
		//This is very inefficient, but that doesn't matter much because this method is only called on a conflict.
		for(int maxDepth = 0;; maxDepth ++)
		{
			List<Symbol> result = searchState(canonicalAutomaton.getInitialState(), state, new HashSet<>(), maxDepth);
			if(result != null)
				return List.copyOf(result);
		}
	}

	private List<Symbol> searchState(State currentState, State searchedState, Set<State> seenStates, int maxDepth)
	{
		if(maxDepth <= 0)
			return null;
		if(currentState.equals(searchedState))
			return new ArrayList<>();
		if(!seenStates.add(currentState))
			return null;
		for(Transition t : canonicalAutomaton.getTransitionsFromState(currentState))
		{
			List<Symbol> searchResult = searchState(t.getTarget(), searchedState, seenStates, maxDepth - 1);
			if(searchResult != null)
			{
				searchResult.add(0, t.getInput());
				return searchResult;
			}
		}
		seenStates.remove(currentState);
		return null;
	}

	private Set<Word> getItemLookaheads(LookaheadItem item)
	{
		List<Symbol> rhsSymbols = item.getItem().getProduction().getRhs().getSymbols();
		List<Symbol> rhsSymbolsAfterMarker = rhsSymbols.subList(item.getItem().getMarkerPosition(), rhsSymbols.size());
		return concatKHelper.concatK(concatKHelper.concatKSymbols(rhsSymbolsAfterMarker), item.getAdmissibleLookaheads());
	}
}
