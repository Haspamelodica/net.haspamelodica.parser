package net.haspamelodica.parser.parser.lrk.canonicalautomaton;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import net.haspamelodica.parser.grammar.Symbol;
import net.haspamelodica.parser.parser.lrk.LookaheadItem;

public class CanonicalAutomaton
{
	private final State				initialState;
	private final Set<State>		states;
	private final Set<Transition>	transitions;

	private final Map<State, Set<Transition>>		transitionsPerState;
	private final Map<State, Map<Symbol, State>>	targetsPerInputPerState;

	public CanonicalAutomaton(State initialState, Set<State> states, Set<Transition> transitions)
	{
		this.initialState = initialState;
		if(!states.contains(initialState))
			throw new IllegalArgumentException("Initial state not in the set of all states");

		this.states = Set.copyOf(states);
		Set<Set<LookaheadItem>> existingItemSets = new HashSet<>();
		for(State s : this.states)
			if(!existingItemSets.add(s.getItems()))
				throw new IllegalArgumentException("Two states with the same item set found");

		this.transitions = Set.copyOf(transitions);
		Map<State, Set<Symbol>> existingTransitions = new HashMap<>();
		for(Transition t : this.transitions)
		{
			if(!states.contains(t.getOrigin()) || !states.contains(t.getTarget()))
				throw new IllegalArgumentException("Transition using states not in the set of all states");
			Set<Symbol> outgoingTransitionsFromOrigin = existingTransitions.computeIfAbsent(t.getOrigin(), s -> new HashSet<>());
			if(!outgoingTransitionsFromOrigin.add(t.getInput()))
				throw new IllegalArgumentException("Two transitions from " + t.getOrigin() + " by input " + t.getInput());
		}

		this.transitionsPerState = calculateTransitionsPerState(this.transitions);
		this.targetsPerInputPerState = calculateTargetsPerInputPerState(transitionsPerState);
	}

	private static Map<State, Set<Transition>> calculateTransitionsPerState(Set<Transition> transitions)
	{
		return transitions.stream().collect(Collectors.groupingBy(
				Transition::getOrigin,
				Collectors.toUnmodifiableSet()));
	}

	private Map<State, Map<Symbol, State>> calculateTargetsPerInputPerState(Map<State, Set<Transition>> transitionsPerState)
	{
		return transitionsPerState
				.entrySet()
				.stream()
				.collect(Collectors.toUnmodifiableMap(Entry::getKey, e -> e
						.getValue()
						.stream()
						.collect(Collectors.toUnmodifiableMap(Transition::getInput, Transition::getTarget))));
	}

	public State getInitialState()
	{
		return initialState;
	}
	public Set<State> getStates()
	{
		return states;
	}
	public Set<Transition> getTransitions()
	{
		return transitions;
	}
	public Map<State, Map<Symbol, State>> getGotoTable()
	{
		return targetsPerInputPerState;
	}
	public Set<Transition> getTransitionsFromState(State origin)
	{
		return transitionsPerState.getOrDefault(origin, Collections.emptySet());
	}
	public State getTransitionTarget(State origin, Symbol input)
	{
		return targetsPerInputPerState.getOrDefault(origin, Collections.emptyMap()).get(input);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((initialState == null) ? 0 : initialState.hashCode());
		result = prime * result + ((states == null) ? 0 : states.hashCode());
		result = prime * result + ((transitions == null) ? 0 : transitions.hashCode());
		result = prime * result + ((transitionsPerState == null) ? 0 : transitionsPerState.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		CanonicalAutomaton other = (CanonicalAutomaton) obj;
		if(initialState == null)
		{
			if(other.initialState != null)
				return false;
		} else if(!initialState.equals(other.initialState))
			return false;
		if(states == null)
		{
			if(other.states != null)
				return false;
		} else if(!states.equals(other.states))
			return false;
		if(transitions == null)
		{
			if(other.transitions != null)
				return false;
		} else if(!transitions.equals(other.transitions))
			return false;
		if(transitionsPerState == null)
		{
			if(other.transitionsPerState != null)
				return false;
		} else if(!transitionsPerState.equals(other.transitionsPerState))
			return false;
		return true;
	}
}
