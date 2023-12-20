package net.haspamelodica.parser.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CharString
{
	private final List<Char> chars;

	public CharString(List<Char> chars)
	{
		this.chars = List.copyOf(chars);
	}
	public CharString(Char... chars)
	{
		this.chars = List.of(chars);
	}

	public List<Char> getChars()
	{
		return chars;
	}

	public Stream<Char> stream()
	{
		return chars.stream();
	}

	public CharString concat(CharString other)
	{
		List<Char> concatChars = new ArrayList<>(chars);
		concatChars.addAll(other.chars);
		return new CharString(concatChars);
	}

	public String toStringNoEscaping()
	{
		return stream().map(Char::toStringNoEscaping).collect(Collectors.joining());
	}

	@Override
	public String toString()
	{
		return stream().map(Char::toString).collect(Collectors.joining());
	}

	public static CharString ofString(String string)
	{
		return new CharString(string.codePoints().mapToObj(Char::fromUnicodeCodePoint).collect(Collectors.toList()));
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chars == null) ? 0 : chars.hashCode());
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
		CharString other = (CharString) obj;
		if(chars == null)
		{
			if(other.chars != null)
				return false;
		} else if(!chars.equals(other.chars))
			return false;
		return true;
	}
}
