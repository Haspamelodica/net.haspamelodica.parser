package net.haspamelodica.parser.grammar.attributes;

import net.haspamelodica.parser.ast.Token;
import net.haspamelodica.parser.grammar.Production;
import net.haspamelodica.parser.grammar.Symbol;
import net.haspamelodica.parser.grammar.Symbol.SymbolType;
import net.haspamelodica.parser.grammar.Terminal;

public class TerminalValueReference<V> extends SymbolValueReference<V, Terminal<V>, Token<V>>
{
	private final Terminal<V> terminal;

	public TerminalValueReference(Production rootProduction, int position, Terminal<V> terminal)
	{
		super(NeighboringValuePositionType.TERMINAL_VALUE, rootProduction, position);
		this.terminal = terminal;

		Symbol parameterSymbol = getParameterSymbol(rootProduction, position);
		if(parameterSymbol.getType() != SymbolType.TERMINAL)
			throw new IllegalArgumentException("The supplied RHS symbol is not a terminal");
		if(!parameterSymbol.equals(terminal))
			throw new IllegalArgumentException("The supplied RHS symbol is a different terminal");
	}

	public Terminal<V> getTerminal()
	{
		return terminal;
	}

	@Override
	protected V getValueForNode(Token<V> target)
	{
		return target.getValue();
	}
	@Override
	protected void setValueForNode(Token<V> target, V value)
	{
		throw new UnsupportedOperationException("Can't set the value of a token");
	}

	@Override
	public String toString()
	{
		return "value[" + getPosition() + "]";
	}
}
