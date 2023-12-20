package net.haspamelodica.parser.tokenizer.charbased;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.haspamelodica.parser.ast.Token;
import net.haspamelodica.parser.grammar.Terminal;
import net.haspamelodica.parser.tokenizer.Char;
import net.haspamelodica.parser.tokenizer.CharGroupAndTerminal;
import net.haspamelodica.parser.tokenizer.CharReader;
import net.haspamelodica.parser.tokenizer.SimpleLocationDescriptor;
import net.haspamelodica.parser.tokenizer.TokenStream;
import net.haspamelodica.parser.tokenizer.Tokenizer;
import net.haspamelodica.parser.tokenizer.TokenizingException;

public class CharGroupTokenizer implements Tokenizer<CharReader>
{
	private final List<CharGroupAndTerminal<Char>>	charGroups;
	private final Set<Terminal<?>>					allTerminals;

	public CharGroupTokenizer(List<CharGroupAndTerminal<Char>> charGroups)
	{
		this.charGroups = List.copyOf(charGroups);
		this.allTerminals = this.charGroups.stream().map(CharGroupAndTerminal::getTerminal).collect(Collectors.toUnmodifiableSet());
	}
	public static CharGroupTokenizer buildSimple(String specialCharacters, String otherCharactersTerminalName)
	{
		return buildSimple(Char.stringToCharStream(specialCharacters).collect(Collectors.toSet()),
				otherCharactersTerminalName);
	}
	public static CharGroupTokenizer buildSimple(Set<Char> specialCharacters, String otherCharactersTerminalName)
	{
		List<CharGroupAndTerminal<Char>> charGroups = specialCharacters
				.stream()
				.map((Function<Char, CharGroupAndTerminal<Char>>) c -> CharGroupAndTerminal.build(c.toStringNoEscaping(), c))
				.collect(Collectors.toCollection(ArrayList::new));
		if(otherCharactersTerminalName != null)
			charGroups.add(CharGroupAndTerminal.buildAll(otherCharactersTerminalName));
		return new CharGroupTokenizer(charGroups);
	}

	@Override
	public TokenStream tokenize(CharReader in)
	{
		return new CharTokenStream(in);
	}

	@Override
	public Set<Terminal<?>> allTerminals()
	{
		return allTerminals;
	}

	private class CharTokenStream implements TokenStream
	{
		private final CharReader				in;
		private final SimpleLocationDescriptor	locationDescriptor;

		public CharTokenStream(CharReader in)
		{
			this.in = in;
			this.locationDescriptor = new SimpleLocationDescriptor();
		}

		@Override
		public Token<Char> nextToken() throws TokenizingException
		{
			Char read;
			try
			{
				read = in.read();
			} catch(IOException e)
			{
				throw new TokenizingException(e);
			}
			if(read == null)
				return null;

			locationDescriptor.advance(read);

			for(CharGroupAndTerminal<Char> g : charGroups)
				if(g.getCharGroup().contains(read))
					return new Token<>(g.getTerminal(), read);

			throw new TokenizingException("Unexpected char: " + read);
		}

		public String getCurrentLocationDescription()
		{
			return locationDescriptor.getLocationDescription();
		}
	}
}
