package net.haspamelodica.parser.tokenizer;

import net.haspamelodica.parser.ast.Token;

public interface TokenStream
{
	public Token<?> nextToken() throws TokenizingException;
	public default String getCurrentLocationDescription()
	{
		return null;
	}
}
