package net.haspamelodica.parser.parser.lrk.canonicalautomaton;

import java.util.Set;

import net.haspamelodica.parser.parser.lrk.LookaheadItem;

public class State
{
	private final Set<LookaheadItem> items;

	public State(Set<LookaheadItem> items)
	{
		this.items = items;
	}

	public Set<LookaheadItem> getItems()
	{
		return items;
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		for(LookaheadItem item : items)
		{
			result.append(item);
			result.append("\n");
		}
		return result.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((items == null) ? 0 : items.hashCode());
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
		State other = (State) obj;
		if(items == null)
		{
			if(other.items != null)
				return false;
		} else if(!items.equals(other.items))
			return false;
		return true;
	}
}
