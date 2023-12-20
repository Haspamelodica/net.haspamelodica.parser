package net.haspamelodica.parser.grammar;

import java.util.List;

public class Production
{
	private final Nonterminal	lhs;
	private final RightHandSide	rhs;

	public Production(Nonterminal lhs, RightHandSide rhs)
	{
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public Nonterminal getLhs()
	{
		return lhs;
	}
	public RightHandSide getRhs()
	{
		return rhs;
	}

	@Override
	public String toString()
	{
		return "[" + toStringNoBrackets() + "]";
	}
	public String toStringNoBrackets()
	{
		return lhs + " -> " + rhs;
	}

	public static Production build(Nonterminal lhs, Symbol... rhs)
	{
		return build(lhs, List.of(rhs));
	}
	public static Production build(Nonterminal lhs, List<Symbol> rhs)
	{
		return new Production(lhs, new RightHandSide(rhs));
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lhs == null) ? 0 : lhs.hashCode());
		result = prime * result + ((rhs == null) ? 0 : rhs.hashCode());
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
		Production other = (Production) obj;
		if(lhs == null)
		{
			if(other.lhs != null)
				return false;
		} else if(!lhs.equals(other.lhs))
			return false;
		if(rhs == null)
		{
			if(other.rhs != null)
				return false;
		} else if(!rhs.equals(other.rhs))
			return false;
		return true;
	}
}
