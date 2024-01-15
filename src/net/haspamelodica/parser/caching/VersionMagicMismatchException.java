package net.haspamelodica.parser.caching;

public class VersionMagicMismatchException extends Exception
{
	private final int	expected;
	private final int	actual;

	public VersionMagicMismatchException(int expected, int actual, String message)
	{
		super("%s: expected %08x but was %08x".formatted(message, expected, actual));
		this.expected = expected;
		this.actual = actual;
	}

	public int expected()
	{
		return expected;
	}
	public int actual()
	{
		return actual;
	}
}
