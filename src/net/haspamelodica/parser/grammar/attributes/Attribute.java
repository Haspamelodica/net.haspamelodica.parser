package net.haspamelodica.parser.grammar.attributes;

public class Attribute<V>
{
	private final String name;

	public Attribute(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
