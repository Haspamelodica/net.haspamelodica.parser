package net.haspamelodica.parser.tokenizer;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class CharGroup
{
	public abstract boolean contains(Char c);

	public CharGroup alternation(String name, CharGroup other)
	{
		return build(name, c -> contains(c) || other.contains(c));
	}

	public CharGroup invert(String name)
	{
		return build(name, c -> !contains(c));
	}

	public static CharGroup build(char... chars)
	{
		Char[] charsCopy = new Char[chars.length];
		for(int i = 0; i < chars.length; i ++)
			charsCopy[i] = Char.fromPrimitiveChar(chars[i]);
		return build(charsCopy);
	}
	public static CharGroup build(Char... chars)
	{
		if(chars.length == 0)
			return buildEmpty();
		Char[] charsSortedTmp = new Char[chars.length];
		int copyCounter = 0;
		//TODO can this be done faster?
		for(Char c : chars)
		{
			int insertionPoint = -Arrays.binarySearch(charsSortedTmp, 0, copyCounter, c) - 1;
			if(insertionPoint >= 0)
			{
				System.arraycopy(charsSortedTmp, insertionPoint, charsSortedTmp, insertionPoint + 1, copyCounter - insertionPoint);
				charsSortedTmp[insertionPoint] = c;
				copyCounter ++;
			}
		}

		Char[] charsSorted = Arrays.copyOf(charsSortedTmp, copyCounter);
		return new CharGroup()
		{
			@Override
			public boolean contains(Char c)
			{
				return Arrays.binarySearch(charsSorted, c) >= 0;
			}
			@Override
			public String toString()
			{
				if(charsSorted.length == 1)
					return charsSorted[0].toString();

				return "<any of " + Arrays.stream(charsSorted).map(Char::toString).collect(Collectors.joining()) + ">";
			}
		};
	}
	public static CharGroup buildEmpty()
	{
		return new CharGroup()
		{
			@Override
			public boolean contains(Char c)
			{
				return false;
			}
			@Override
			public String toString()
			{
				return "<empty>";
			}
		};
	}
	public static CharGroup buildAll()
	{
		return new CharGroup()
		{
			@Override
			public boolean contains(Char c)
			{
				return true;
			}
			@Override
			public String toString()
			{
				return "<all>";
			}
		};
	}
	public static CharGroup build(String chars)
	{
		return build(Char.stringToCharStream(chars).toArray(Char[]::new));
	}
	public static CharGroup build(String name, Predicate<Char> predicate)
	{
		return new CharGroup()
		{
			@Override
			public boolean contains(Char c)
			{
				return predicate.test(c);
			}
			@Override
			public String toString()
			{
				return name;
			}
		};
	}
}
