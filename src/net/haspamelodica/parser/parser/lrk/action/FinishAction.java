package net.haspamelodica.parser.parser.lrk.action;

import net.haspamelodica.parser.grammar.Production;

public class FinishAction implements Action
{
	private final Production	production;
	private final boolean		dontIncludeStartSymbol;

	private final int rhsSize;

	public FinishAction(Production production, boolean dontIncludeStartSymbol)
	{
		this.production = production;
		this.dontIncludeStartSymbol = dontIncludeStartSymbol;

		this.rhsSize = production.getRhs().getSymbols().size();
	}

	public Production getProduction()
	{
		return production;
	}
	public boolean dontIncludeStartSymbol()
	{
		return dontIncludeStartSymbol;
	}
	public int getRhsSize()
	{
		return rhsSize;
	}

	@Override
	public ActionType getType()
	{
		return ActionType.FINISH;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (dontIncludeStartSymbol ? 1231 : 1237);
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
		FinishAction other = (FinishAction) obj;
		if(dontIncludeStartSymbol != other.dontIncludeStartSymbol)
			return false;
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
