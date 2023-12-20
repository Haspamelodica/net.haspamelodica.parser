package net.haspamelodica.parser.tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

public interface CharReader
{
	public Char read() throws IOException;

	public static CharReader readString(String in)
	{
		Iterator<Char> chars = in.codePoints().mapToObj(Char::fromUnicodeCodePoint).iterator();
		return () -> chars.hasNext() ? chars.next() : null;
	}
	public static CharReader fromReader(Reader in)
	{
		return fromAbstractReader(in::read);
	}
	public static CharReader fromAbstractReader(AbstractReader in)
	{
		return new CharReader()
		{
			public Char read() throws IOException
			{
				int read = in.get();
				if(read == -1)
					return null;

				char high = (char) read;
				if(!Character.isHighSurrogate(high))
					return Char.fromPrimitiveChar(high);

				read = in.get();
				if(read == -1)
					return Char.fromPrimitiveChar(high);

				char low = (char) read;
				if(!Character.isLowSurrogate(low))
					return Char.fromPrimitiveChar(high);

				return Char.fromUnicodeCodePoint(Character.toCodePoint(high, low));
			}
		};
	}

	public static CharReader lowercasing(CharReader in)
	{
		return () ->
		{
			Char c = in.read();
			if(c == null)
				return c;
			return c.toLowercase();
		};
	}

	public static CharReader uppercasing(CharReader in)
	{
		return () ->
		{
			Char c = in.read();
			if(c == null)
				return c;
			return c.toUppercase();
		};
	}

	public static interface AbstractReader
	{
		public int get() throws IOException;
	}
}
