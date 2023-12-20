package net.haspamelodica.parser.ast;

import net.haspamelodica.parser.grammar.Terminal;

public class Token<V> implements ASTNode<Terminal<V>>
{
	private Terminal<V>	terminal;
	private V			value;

	public Token(Terminal<V> terminal, V value)
	{
		this.terminal = terminal;
		this.value = value;
	}

	public V getValue()
	{
		return value;
	}

	@Override
	public Terminal<V> getSymbol()
	{
		return terminal;
	}

	@Override
	public ASTNodeType getType()
	{
		return ASTNodeType.TOKEN;
	}

	@Override
	public String toString()
	{
		return value != null ? terminal + " <" + value + ">" : terminal.toString();
	}

	@Override
	public void append(StringBuilder result, int tabs)
	{
		result.append(" ".repeat(tabs));
		result.append(this);
		result.append(System.lineSeparator());
	}

	public static Token<Void> build(Terminal<Void> terminal)
	{
		return new Token<Void>(terminal, null);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((terminal == null) ? 0 : terminal.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Token<?> other = (Token<?>) obj;
		if(terminal == null)
		{
			if(other.terminal != null)
				return false;
		} else if(!terminal.equals(other.terminal))
			return false;
		if(value == null)
		{
			if(other.value != null)
				return false;
		} else if(!value.equals(other.value))
			return false;
		return true;
	}
}
