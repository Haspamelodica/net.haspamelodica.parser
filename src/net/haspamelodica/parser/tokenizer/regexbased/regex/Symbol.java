package net.haspamelodica.parser.tokenizer.regexbased.regex;

import java.util.List;
import java.util.Set;

import net.haspamelodica.parser.tokenizer.CharGroup;

public class Symbol extends RegexNode
{
	private final CharGroup	symbol;
	private int				symbolIndex;

	private Symbol(CharGroup symbol)
	{
		super(Type.SYMBOL);
		this.symbol = symbol;
		this.symbolIndex = -1;
	}

	public static RegexNode build(CharGroup symbol)
	{
		return new Symbol(symbol);
	}

	@Override
	public int calculateSymbolIndices(int nextSymbolIndex)
	{
		if(this.symbolIndex != -1)
			throw new IllegalStateException("The symbol index has already been calculated");
		this.symbolIndex = nextSymbolIndex;
		return nextSymbolIndex + 1;
	}

	@Override
	protected List<Symbol> calculateSymbolsLeftToRight()
	{
		return List.of(this);
	}

	@Override
	protected boolean calculateEmpty()
	{
		return false;
	}

	@Override
	protected Set<Symbol> calculateFirstSet()
	{
		return Set.of(this);
	}

	@Override
	protected void calculateChildrenNextSets(Set<Symbol> next)
	{}

	@Override
	protected Set<Symbol> calculateLastSet()
	{
		return Set.of(this);
	}

	public CharGroup getSymbol()
	{
		return symbol;
	}
	public int getSymbolIndex()
	{
		if(symbolIndex == -1)
			throw new IllegalStateException("The symbol index has not been calculated yet");
		return symbolIndex;
	}

	@Override
	public boolean sameRegex(RegexNode other)
	{
		if(other.getType() != Type.SYMBOL)
			return false;
		return ((Symbol) other).symbol.equals(symbol);
	}

	@Override
	protected String toString(int parenthesisLevel)
	{
		return symbol.toString();
	}

	@Override
	public RegexNode duplicate()
	{
		return build(symbol);
	}
}
