package net.haspamelodica.parser.tokenizer.regexbased.regex;

import java.util.List;
import java.util.Set;

public class Epsilon extends RegexNode
{
	private Epsilon()
	{
		super(Type.EPSILON);
	}

	public static RegexNode build()
	{
		// Can't make this a singleton because next / last / empty set
		return new Epsilon();
	}

	@Override
	public int calculateSymbolIndices(int nextSymbolIndex)
	{
		return nextSymbolIndex;
	}

	@Override
	protected List<Symbol> calculateSymbolsLeftToRight()
	{
		return List.of();
	}

	@Override
	protected boolean calculateEmpty()
	{
		return true;
	}

	@Override
	protected Set<Symbol> calculateFirstSet()
	{
		return Set.of();
	}

	@Override
	protected void calculateChildrenNextSets(Set<Symbol> next)
	{}

	@Override
	protected Set<Symbol> calculateLastSet()
	{
		return Set.of();
	}

	@Override
	public boolean sameRegex(RegexNode other)
	{
		return other != null && other instanceof Epsilon;
	}

	@Override
	protected String toString(int parenthesisLevel)
	{
		return "";
	}

	@Override
	public RegexNode duplicate()
	{
		return new Epsilon();
	}
}
