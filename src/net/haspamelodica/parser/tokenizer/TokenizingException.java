package net.haspamelodica.parser.tokenizer;

import net.haspamelodica.parser.parser.ParseException;

public class TokenizingException extends ParseException
{
	public TokenizingException()
	{
		super();
	}
	public TokenizingException(String message)
	{
		super(message);
	}
	public TokenizingException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public TokenizingException(Throwable cause)
	{
		super(cause);
	}
	protected TokenizingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
