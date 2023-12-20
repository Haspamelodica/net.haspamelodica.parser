package net.haspamelodica.parser.tokenizer.regexbased;

import net.haspamelodica.parser.tokenizer.regexbased.regex.Regex;

public class NamedRegex
{
	private final String	name;
	private final Regex		regex;

	public NamedRegex(String name, Regex regex)
	{
		this.name = name;
		this.regex = regex;
	}

	public String getName()
	{
		return name;
	}
	public Regex getRegex()
	{
		return regex;
	}
}
