package net.haspamelodica.parser.tokenizer.regexbased.nfa;

public class State
{
	private final String name;

	public State(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
