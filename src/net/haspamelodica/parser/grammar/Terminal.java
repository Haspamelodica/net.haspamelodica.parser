package net.haspamelodica.parser.grammar;

public class Terminal<V> implements Symbol
{
	public static final Terminal<Void> EOF = new Terminal<>("$", true);

	private final String name;

	private final boolean isEof;

	public Terminal(String name)
	{
		this(name, false);
	}
	private Terminal(String name, boolean isEOF)
	{
		this.name = name;
		this.isEof = isEOF;
	}

	public String getName()
	{
		return name;
	}
	public boolean isEof()
	{
		return isEof;
	}

	@Override
	public SymbolType getType()
	{
		return SymbolType.TERMINAL;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
