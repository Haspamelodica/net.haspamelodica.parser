package net.haspamelodica.parser.tokenizer.regexbased.nfa;

import java.util.HashSet;
import java.util.Set;

import net.haspamelodica.parser.tokenizer.CharGroup;

public class NFABuilder
{
	private State					startState;
	private final Set<Transition>	transitions;
	private final Set<State>		finalStates;

	public NFABuilder()
	{
		transitions = new HashSet<>();
		finalStates = new HashSet<>();
	}

	public NFABuilder startState(State startState)
	{
		this.startState = startState;
		return this;
	}
	public NFABuilder addTransition(State sourceState, CharGroup trigger, State targetState)
	{
		transitions.add(new Transition(sourceState, trigger, targetState));
		return this;
	}
	public NFABuilder addFinalState(State finalState)
	{
		finalStates.add(finalState);
		return this;
	}

	public NFA build()
	{
		return new NFA(startState, transitions, finalStates);
	}
}
