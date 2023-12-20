package net.haspamelodica.parser.tokenizer.regexbased.nfa;

import net.haspamelodica.parser.tokenizer.CharGroup;

public class Transition
{
	private final State		sourceState;
	private final CharGroup	trigger;
	private final State		targetState;

	public Transition(State sourceState, CharGroup trigger, State targetState)
	{
		this.sourceState = sourceState;
		this.trigger = trigger;
		this.targetState = targetState;
	}

	public State getSourceState()
	{
		return sourceState;
	}
	public CharGroup getTrigger()
	{
		return trigger;
	}
	public State getTargetState()
	{
		return targetState;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceState == null) ? 0 : sourceState.hashCode());
		result = prime * result + ((targetState == null) ? 0 : targetState.hashCode());
		result = prime * result + ((trigger == null) ? 0 : trigger.hashCode());
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
		if(sourceState == null)
		{
			if(other.sourceState != null)
				return false;
		} else if(!sourceState.equals(other.sourceState))
			return false;
		if(targetState == null)
		{
			if(other.targetState != null)
				return false;
		} else if(!targetState.equals(other.targetState))
			return false;
		if(trigger != other.trigger)
			return false;
		return true;
	}
}
