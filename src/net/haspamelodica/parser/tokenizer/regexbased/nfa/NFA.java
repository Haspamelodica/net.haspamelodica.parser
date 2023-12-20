package net.haspamelodica.parser.tokenizer.regexbased.nfa;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.haspamelodica.parser.tokenizer.Char;
import net.haspamelodica.parser.tokenizer.CharGroup;

public class NFA
{
	private final State				startState;
	private final Set<Transition>	transitions;
	private final Set<State>		finalStates;

	private final Map<State, Map<CharGroup, Set<State>>> nextStatesPerStatePerTrigger;

	public NFA(State startState, Set<Transition> transitions, Set<State> finalStates)
	{
		this.startState = startState;
		this.transitions = Collections.unmodifiableSet(transitions);
		this.finalStates = Collections.unmodifiableSet(finalStates);

		this.nextStatesPerStatePerTrigger = Collections.unmodifiableMap(transitions.stream().collect(
				Collectors.groupingBy(Transition::getSourceState,
						Collectors.collectingAndThen(Collectors.groupingBy(Transition::getTrigger,
								Collectors.mapping(Transition::getTargetState, Collectors.toUnmodifiableSet())),
								Collections::unmodifiableMap))));
	}

	public String graphviz()
	{
		StringBuilder result = new StringBuilder();
		result.append("digraph nfa {\n");
		result.append("rankdir=LR;\n");
		result.append("node [shape=doublecircle];\n");
		for(State finalState : finalStates)
			result.append("\"" + finalState + "\"; ");
		result.append("\n");
		result.append("node [shape=circle];\n");
		for(Transition transition : transitions)
			result.append("\"" + transition.getSourceState() + "\" -> \"" + transition.getTargetState() + "\" [label=\"" + transition.getTrigger() + "\"];\n");
		result.append("node [shape=none,width=0,height=0,margin=0]; beforestart [label=\"\"];\n");
		result.append("beforestart -> \"" + startState + "\";\n");
		result.append("}");
		return result.toString();
	}

	public State getStartState()
	{
		return startState;
	}
	public Set<Transition> getTransitions()
	{
		return transitions;
	}
	public Set<State> getFinalState()
	{
		return finalStates;
	}
	public Map<State, Map<CharGroup, Set<State>>> getNextStatesPerStatePerTrigger()
	{
		return nextStatesPerStatePerTrigger;
	}

	public Stream<State> getNextStates(State from, Char trigger)
	{
		return nextStatesPerStatePerTrigger
				.getOrDefault(from, Map.of())
				.entrySet()
				.stream()
				.filter(e -> e.getKey().contains(trigger))
				.map(Entry::getValue)
				.flatMap(Set::stream);
	}
	public Stream<State> getNextStates(Stream<State> from, Char trigger)
	{
		return from.flatMap(f -> getNextStates(f, trigger));
	}

	public boolean accepts(String input)
	{
		Stream<State> possibleStates = Stream.of(startState);
		for(Char c : (Iterable<Char>) () -> Char.stringToCharStream(input).iterator())
			// distinct is to speed things up
			possibleStates = getNextStates(possibleStates, c).distinct();
		return possibleStates.anyMatch(finalStates::contains);
	}

	public static NFABuilder builder()
	{
		return new NFABuilder();
	}
}
