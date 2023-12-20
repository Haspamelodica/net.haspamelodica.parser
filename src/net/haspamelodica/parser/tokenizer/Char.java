package net.haspamelodica.parser.tokenizer;

import java.util.stream.Stream;

public class Char implements Comparable<Char>
{
	private final int unicodeCodepoint;

	public Char(int unicodeCodepoint)
	{
		this.unicodeCodepoint = unicodeCodepoint;
	}

	private static final int	MAX_CACHED_CHAR	= Character.MAX_VALUE;
	private static final Char[]	CACHED_CHARS	= new Char[MAX_CACHED_CHAR];

	public static Char fromPrimitiveChar(char c)
	{
		return fromUnicodeCodePoint(c);
	}
	public static Char fromUnicodeCodePoint(int codepoint)
	{
		if(codepoint >= MAX_CACHED_CHAR)
			return new Char(codepoint);

		// This is not thread-safe, but it doesn't need to be
		if(CACHED_CHARS[codepoint] != null)
			return CACHED_CHARS[codepoint];

		return CACHED_CHARS[codepoint] = new Char(codepoint);
	}

	public boolean isRepresentableAsPrimitiveChar()
	{
		return Character.isBmpCodePoint(unicodeCodepoint);
	}
	public char toPrimitiveChar()
	{
		if(!isRepresentableAsPrimitiveChar())
			throw new IllegalStateException(this + " is not representable as a primitive char");
		return (char) unicodeCodepoint;
	}

	//we could in theory extend Char beyond Unicode
	public boolean isRepresentableAsUnicodeCodepoint()
	{
		return true;
	}
	public int toUnicodeCodepoint()
	{
		return unicodeCodepoint;
	}

	@Override
	public int compareTo(Char o)
	{
		return Integer.compare(unicodeCodepoint, o.unicodeCodepoint);
	}
	public int compareToChar(char c)
	{
		return Integer.compare(unicodeCodepoint, c);
	}
	public int compareToUnicodeCodepoint(int codepoint)
	{
		return Integer.compare(unicodeCodepoint, codepoint);
	}

	@Override
	public int hashCode()
	{
		return unicodeCodepoint;
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
		return unicodeCodepoint == ((Char) obj).unicodeCodepoint;
	}
	public boolean equalsChar(char c)
	{
		return c == unicodeCodepoint;
	}
	public boolean equalsUnicodeCodepoint(int codepoint)
	{
		return codepoint == unicodeCodepoint;
	}
	@Override
	public String toString()
	{
		return switch(unicodeCodepoint)
		{
			case '\r' -> "\\r";
			case '\n' -> "\\n";
			case '\\' -> "\\\\";
			default -> toStringNoEscaping();
		};
	}
	public String toStringNoEscaping()
	{
		return Character.toString(unicodeCodepoint);
	}

	public static Stream<Char> stringToCharStream(String chars)
	{
		return chars.codePoints().mapToObj(Char::fromUnicodeCodePoint);
	}

	public boolean isWhitespace()
	{
		return Character.isWhitespace(unicodeCodepoint);
	}
	public boolean isJavaIdentifierStart()
	{
		return Character.isJavaIdentifierStart(unicodeCodepoint);
	}
	public boolean isJavaIdentifierPart()
	{
		return Character.isJavaIdentifierPart(unicodeCodepoint);
	}
	public boolean isNewline()
	{
		//TODO are there others?
		return unicodeCodepoint == '\r' || unicodeCodepoint == '\n';
	}
	public Char toLowercase()
	{
		return fromUnicodeCodePoint(Character.toLowerCase(unicodeCodepoint));
	}
	public Char toUppercase()
	{
		return fromUnicodeCodePoint(Character.toUpperCase(unicodeCodepoint));
	}
}
