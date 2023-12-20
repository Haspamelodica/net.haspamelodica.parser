package net.haspamelodica.parser.tokenizer.regexbased.regex;

import java.util.List;
import java.util.Set;

import net.haspamelodica.parser.utils.CollectionsUtils;

public class Star extends RegexNode
{
	private final RegexNode child;

	private Star(RegexNode child)
	{
		super(Type.STAR);
		this.child = child;
	}

	public static RegexNode build(RegexNode child)
	{
		if(child.getType() == Type.STAR || child.getType() == Type.EPSILON)
			return child;
		return new Star(child);
	}

	@Override
	public int calculateSymbolIndices(int nextSymbolIndex)
	{
		return child.calculateSymbolIndices(nextSymbolIndex);
	}

	@Override
	protected List<Symbol> calculateSymbolsLeftToRight()
	{
		return child.calculateSymbolsLeftToRight();
	}

	@Override
	protected boolean calculateEmpty()
	{
		return true;
	}

	@Override
	protected Set<Symbol> calculateFirstSet()
	{
		return child.getFirstSet();
	}

	protected void calculateChildrenNextSets(Set<Symbol> next)
	{
		child.calculateNextSets(CollectionsUtils.union(next, child.getFirstSet()));
	}

	@Override
	protected Set<Symbol> calculateLastSet()
	{
		return child.calculateLastSet();
	}

	public RegexNode getChild()
	{
		return child;
	}

	@Override
	public boolean sameRegex(RegexNode other)
	{
		if(other.getType() != Type.STAR)
			return false;
		Star otherCasted = (Star) other;
		return child.sameRegex(otherCasted.child);
	}

	@Override
	protected String toString(int parenthesisLevel)
	{
		String subResult = child.toString(2) + "*";
		if(parenthesisLevel < 3)
			return subResult;
		return "(" + subResult + ")";
	}

	@Override
	public RegexNode duplicate()
	{
		return build(child.duplicate());
	}
}
