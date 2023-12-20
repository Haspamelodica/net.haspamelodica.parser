package net.haspamelodica.parser.tokenizer.regexbased.regex;

import java.util.List;
import java.util.Set;

import net.haspamelodica.parser.utils.CollectionsUtils;

public class Alternation extends RegexNode
{
	private final RegexNode	left;
	private final RegexNode	right;

	private Alternation(RegexNode left, RegexNode right)
	{
		super(Type.ALTERNATION);
		this.left = left;
		this.right = right;
	}

	public static RegexNode build(RegexNode left, RegexNode right)
	{
		if(left.sameRegex(right))
			return left;
		return new Alternation(left, right);
	}

	@Override
	public int calculateSymbolIndices(int nextSymbolIndex)
	{
		return right.calculateSymbolIndices(left.calculateSymbolIndices(nextSymbolIndex));
	}

	@Override
	protected List<Symbol> calculateSymbolsLeftToRight()
	{
		return CollectionsUtils.concat(left.calculateSymbolsLeftToRight(), right.calculateSymbolsLeftToRight());
	}

	@Override
	protected boolean calculateEmpty()
	{
		return left.isEmpty() || right.isEmpty();
	}

	@Override
	protected Set<Symbol> calculateFirstSet()
	{
		return CollectionsUtils.union(left.getFirstSet(), right.getFirstSet());
	}

	@Override
	protected void calculateChildrenNextSets(Set<Symbol> next)
	{
		left.calculateNextSets(next);
		right.calculateNextSets(next);
	}

	@Override
	protected Set<Symbol> calculateLastSet()
	{
		return CollectionsUtils.union(left.getLastSet(), right.getLastSet());
	}

	public RegexNode getLeft()
	{
		return left;
	}
	public RegexNode getRight()
	{
		return right;
	}

	@Override
	public boolean sameRegex(RegexNode other)
	{
		if(other.getType() != Type.ALTERNATION)
			return false;
		Alternation otherCasted = (Alternation) other;
		return left.sameRegex(otherCasted.left) && right.sameRegex(otherCasted.right);
	}

	@Override
	protected String toString(int parenthesisLevel)
	{
		String subResult = left.toString(0) + "|" + right.toString(0);
		if(parenthesisLevel < 1)
			return subResult;
		return "(" + subResult + ")";
	}

	@Override
	public RegexNode duplicate()
	{
		return build(left.duplicate(), right.duplicate());
	}
}
