package net.haspamelodica.parser.parser.lrk;

import java.util.Map;

import net.haspamelodica.parser.grammar.Symbol;
import net.haspamelodica.parser.parser.lrk.action.Action;
import net.haspamelodica.parser.parser.lrk.canonicalautomaton.CanonicalAutomaton;
import net.haspamelodica.parser.parser.lrk.canonicalautomaton.State;

public class LRkParser extends GenericLRkParser<State>
{
	public LRkParser(CanonicalAutomaton canonicalAutomaton, Map<State, Map<Word, Action>> actionTable, int lookaheadSize)
	{
		this(canonicalAutomaton.getInitialState(), canonicalAutomaton.getGotoTable(), actionTable, lookaheadSize);
	}
	public LRkParser(State initialState, Map<State, Map<Symbol, State>> gotoTable, Map<State, Map<Word, Action>> actionTable, int lookaheadSize)
	{
		super(initialState, gotoTable, actionTable, lookaheadSize);
	}
}
