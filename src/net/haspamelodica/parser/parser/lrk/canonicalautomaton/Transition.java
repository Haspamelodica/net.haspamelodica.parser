package net.haspamelodica.parser.parser.lrk.canonicalautomaton;

import net.haspamelodica.parser.grammar.Symbol;

public class Transition
{
	private final State		origin;
	private final Symbol	input;
	private final State		target;

	public Transition(State origin, Symbol input, State target)
	{
		this.origin = origin;
		this.input = input;
		this.target = target;
	}

	public State getOrigin()
	{
		return origin;
	}
	public Symbol getInput()
	{
		return input;
	}
	public State getTarget()
	{
		return target;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((input == null) ? 0 : input.hashCode());
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		Transition other = (Transition) obj;
		if(input == null)
		{
			if(other.input != null)
				return false;
		} else if(!input.equals(other.input))
			return false;
		if(origin == null)
		{
			if(other.origin != null)
				return false;
		} else if(!origin.equals(other.origin))
			return false;
		if(target == null)
		{
			if(other.target != null)
				return false;
		} else if(!target.equals(other.target))
			return false;
		return true;
	}
}
