package net.haspamelodica.parser.grammar;

import java.util.List;
import java.util.stream.Collectors;

public class RightHandSide
{
	private final List<Symbol> symbols;

	public RightHandSide(List<Symbol> symbols)
	{
		this.symbols = List.copyOf(symbols);
	}

	public List<Symbol> getSymbols()
	{
		return symbols;
	}

	@Override
	public String toString()
	{
		return symbols.stream().map(Symbol::toString).collect(Collectors.joining(" "));
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbols == null) ? 0 : symbols.hashCode());
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
		RightHandSide other = (RightHandSide) obj;
		if(symbols == null)
		{
			if(other.symbols != null)
				return false;
		} else if(!symbols.equals(other.symbols))
			return false;
		return true;
	}
}
