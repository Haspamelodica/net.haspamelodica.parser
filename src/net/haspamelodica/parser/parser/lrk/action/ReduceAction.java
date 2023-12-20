package net.haspamelodica.parser.parser.lrk.action;

import net.haspamelodica.parser.grammar.Production;

public class ReduceAction implements Action
{
	private final Production production;

	private final int rhsSize;

	public ReduceAction(Production production)
	{
		this.production = production;

		this.rhsSize = production.getRhs().getSymbols().size();
	}

	public Production getProduction()
	{
		return production;
	}
	public int getRhsSize()
	{
		return rhsSize;
	}

	@Override
	public ActionType getType()
	{
		return ActionType.REDUCE;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((production == null) ? 0 : production.hashCode());
		result = prime * result + rhsSize;
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
		ReduceAction other = (ReduceAction) obj;
		if(production == null)
		{
			if(other.production != null)
				return false;
		} else if(!production.equals(other.production))
			return false;
		if(rhsSize != other.rhsSize)
			return false;
		return true;
	}
}
