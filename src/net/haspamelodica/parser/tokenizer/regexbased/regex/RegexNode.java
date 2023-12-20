package net.haspamelodica.parser.tokenizer.regexbased.regex;

import java.util.List;
import java.util.Set;

import net.haspamelodica.parser.tokenizer.Char;
import net.haspamelodica.parser.tokenizer.CharGroup;

public abstract class RegexNode
{
	private final Type type;

	private List<Symbol> symbolsLeftToRight;

	private boolean		emptyCalculated;
	private boolean		empty;
	private Set<Symbol>	firstSet;
	private Set<Symbol>	nextSet;
	private Set<Symbol>	lastSet;

	public RegexNode(Type type)
	{
		this.type = type;
	}

	public abstract int calculateSymbolIndices(int nextSymbolIndex);
	public void calculateNextSets(Set<Symbol> next)
	{
		if(this.nextSet != null)
			throw new IllegalStateException("The next set has already been calculated");
		this.nextSet = Set.copyOf(next);
		calculateChildrenNextSets(this.nextSet);
	}

	protected abstract List<Symbol> calculateSymbolsLeftToRight();
	protected abstract boolean calculateEmpty();
	protected abstract Set<Symbol> calculateFirstSet();
	protected abstract void calculateChildrenNextSets(Set<Symbol> next);
	protected abstract Set<Symbol> calculateLastSet();

	public final Type getType()
	{
		return type;
	}
	public List<Symbol> getSymbolsLeftToRight()
	{
		if(symbolsLeftToRight == null)
			symbolsLeftToRight = calculateSymbolsLeftToRight();
		return symbolsLeftToRight;
	}
	public boolean isEmpty()
	{
		if(!emptyCalculated)
		{
			empty = calculateEmpty();
			emptyCalculated = true;
		}
		return empty;
	}
	public Set<Symbol> getFirstSet()
	{
		if(firstSet == null)
			firstSet = calculateFirstSet();
		return firstSet;
	}
	public Set<Symbol> getNextSet()
	{
		if(nextSet == null)
			throw new IllegalStateException("The next set has not been calculated yet");
		return nextSet;
	}
	public Set<Symbol> getLastSet()
	{
		if(lastSet == null)
			lastSet = calculateLastSet();
		return lastSet;
	}

	public abstract boolean sameRegex(RegexNode other);

	@Override
	public String toString()
	{
		return toString(0);
	}
	protected abstract String toString(int parenthesisLevel);

	public abstract RegexNode duplicate();

	public static RegexNode epsilon()
	{
		return Epsilon.build();
	}
	public static RegexNode symbol(Char symbol)
	{
		return symbol(CharGroup.build(symbol));
	}
	public static RegexNode symbol(CharGroup symbol)
	{
		return Symbol.build(symbol);
	}
	public static RegexNode alternation(RegexNode left, RegexNode right)
	{
		return Alternation.build(left, right);
	}
	public static RegexNode concatenation(RegexNode left, RegexNode right)
	{
		return Concatenation.build(left, right);
	}
	public static RegexNode star(RegexNode child)
	{
		return Star.build(child);
	}

	public static enum Type
	{
		EPSILON,
		SYMBOL,
		ALTERNATION,
		CONCATENATION,
		STAR;
	}
}
