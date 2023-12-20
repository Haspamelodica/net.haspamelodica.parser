package net.haspamelodica.parser.tokenizer.regexbased.regex;

import java.util.List;
import java.util.Set;

import net.haspamelodica.parser.utils.CollectionsUtils;

public class Concatenation extends RegexNode
{
	private final RegexNode	left;
	private final RegexNode	right;

	private Concatenation(RegexNode left, RegexNode right)
	{
		super(Type.CONCATENATION);
		this.left = left;
		this.right = right;
	}

	public static RegexNode build(RegexNode left, RegexNode right)
	{
		if(left.getType() == Type.EPSILON)
			return right;
		if(right.getType() == Type.EPSILON)
			return left;
		return new Concatenation(left, right);
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
		return left.isEmpty() && right.isEmpty();
	}

	@Override
	protected Set<Symbol> calculateFirstSet()
	{
		Set<Symbol> result = left.getFirstSet();
		if(left.isEmpty())
			result = CollectionsUtils.union(result, right.getFirstSet());
		return result;
	}

	@Override
	protected void calculateChildrenNextSets(Set<Symbol> next)
	{
		right.calculateNextSets(next);
		Set<Symbol> leftNext = right.getFirstSet();
		if(right.isEmpty())
			leftNext = CollectionsUtils.union(leftNext, next);
		left.calculateNextSets(leftNext);
	}

	@Override
	protected Set<Symbol> calculateLastSet()
	{
		Set<Symbol> result = right.getLastSet();
		if(right.isEmpty())
			result = CollectionsUtils.union(result, left.getLastSet());
		return result;
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
		if(other.getType() != Type.CONCATENATION)
			return false;
		Concatenation otherCasted = (Concatenation) other;
		return left.sameRegex(otherCasted.left) && right.sameRegex(otherCasted.right);
	}

	@Override
	protected String toString(int parenthesisLevel)
	{
		String subResult = left.toString(1) + right.toString(1);
		if(parenthesisLevel < 2)
			return subResult;
		return "(" + subResult + ")";
	}

	@Override
	public RegexNode duplicate()
	{
		return build(left.duplicate(), right.duplicate());
	}
}
