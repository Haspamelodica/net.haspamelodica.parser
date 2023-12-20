package net.haspamelodica.parser.grammar;

public class Nonterminal implements Symbol
{
	private final String name;

	public Nonterminal(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public SymbolType getType()
	{
		return SymbolType.NONTERMINAL;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
