package net.haspamelodica.parser.grammar.parser;

import java.io.IOException;
import java.util.Set;

import net.haspamelodica.parser.ast.Token;
import net.haspamelodica.parser.grammar.Terminal;
import net.haspamelodica.parser.tokenizer.Char;
import net.haspamelodica.parser.tokenizer.CharReader;
import net.haspamelodica.parser.tokenizer.SimpleLocationDescriptor;
import net.haspamelodica.parser.tokenizer.TokenStream;
import net.haspamelodica.parser.tokenizer.Tokenizer;
import net.haspamelodica.parser.tokenizer.TokenizingException;

public class GrammarTokenizer implements Tokenizer<CharReader>
{
	public static final Terminal<String>	IDENTIFIER	= new Terminal<>("identifier");
	public static final Terminal<String>	TERMINAL	= new Terminal<>("terminal");
	public static final Terminal<Integer>	INTEGER		= new Terminal<>("int");
	public static final Terminal<Void>		ARROW		= new Terminal<>("->");
	public static final Terminal<Void>		OR			= new Terminal<>("|");
	public static final Terminal<Void>		COMMA		= new Terminal<>(",");
	public static final Terminal<Void>		SEMICOLON	= new Terminal<>(";");
	public static final Terminal<Void>		BRACKET_O	= new Terminal<>("{");
	public static final Terminal<Void>		BRACKET_C	= new Terminal<>("}");
	public static final Terminal<Void>		SQUARE_O	= new Terminal<>("[");
	public static final Terminal<Void>		SQUARE_C	= new Terminal<>("]");
	public static final Terminal<Void>		PAREN_O		= new Terminal<>("(");
	public static final Terminal<Void>		PAREN_C		= new Terminal<>(")");
	public static final Terminal<Void>		EQUALS		= new Terminal<>("=");

	public static final Set<Terminal<?>> allTerminals = Set.of(IDENTIFIER, TERMINAL, INTEGER, ARROW, OR, COMMA, SEMICOLON, BRACKET_O,
			BRACKET_C, SQUARE_O, SQUARE_C, PAREN_O, PAREN_C, EQUALS);

	@Override
	public TokenStream tokenize(CharReader in)
	{
		return new GrammarTokenStream(in);
	}

	@Override
	public Set<Terminal<?>> allTerminals()
	{
		return allTerminals;
	}

	private static class GrammarTokenStream implements TokenStream
	{
		private final CharReader in;

		private State				state;
		private int					sign;
		private int					integer;
		private final StringBuilder	sb;

		private Char nextChar;

		private final SimpleLocationDescriptor locationDescriptor;

		public GrammarTokenStream(CharReader in)
		{
			this.in = in;
			this.state = State.DEFAULT;
			this.sb = new StringBuilder();

			this.locationDescriptor = new SimpleLocationDescriptor();
		}

		@Override
		public Token<?> nextToken() throws TokenizingException
		{
			for(;;)
			{
				Char read = nextChar;
				nextChar = null;
				if(read == null)
					try
					{
						read = in.read();
						locationDescriptor.advance(read);
					} catch(IOException e)
					{
						throw new TokenizingException(e);
					}
				switch(state)
				{
					case DEFAULT:
						if(read == null)
							return null;
						else if(read.equalsChar('\''))
							state = State.IN_LITERAL;
						else if(read.equalsChar('"'))
							state = State.IN_STRING;
						else if(read.equalsChar('|'))
							return Token.build(OR);
						else if(read.equalsChar('{'))
							return Token.build(BRACKET_O);
						else if(read.equalsChar('}'))
							return Token.build(BRACKET_C);
						else if(read.equalsChar('['))
							return Token.build(SQUARE_O);
						else if(read.equalsChar(']'))
							return Token.build(SQUARE_C);
						else if(read.equalsChar('('))
							return Token.build(PAREN_O);
						else if(read.equalsChar(')'))
							return Token.build(PAREN_C);
						else if(read.equalsChar('='))
							return Token.build(EQUALS);
						else if(read.equalsChar('-'))
							state = State.AFTER_MINUS;
						else if(read.equalsChar('/'))
							state = State.AFTER_SLASH;
						else if(read.equalsChar(';'))
							return Token.build(SEMICOLON);
						else if(read.equalsChar(','))
							return Token.build(COMMA);
						else if(read.compareToChar('0') >= 0 && read.compareToChar('9') <= 0)
						{
							integer = read.toPrimitiveChar() - '0';
							sign = 1;
							state = State.IN_INTEGER;
						} else if(read.isJavaIdentifierStart())
						{
							sb.append(read.toStringNoEscaping());
							state = State.IN_IDENTIFIER;
						} else if(!read.isWhitespace())
							throw new TokenizingException("Unexpected char: " + read);
						break;

					case AFTER_MINUS:
						if(read == null)
							throw new TokenizingException("Unexpected EOF");
						else if(read.equalsChar('>'))
						{
							state = State.DEFAULT;
							return Token.build(ARROW);
						} else if(read.compareToChar('0') >= 0 && read.compareToChar('9') <= 0)
						{
							integer = read.toPrimitiveChar() - '0';
							sign = -1;
							state = State.IN_INTEGER;
						} else
							throw new TokenizingException("Unexpected char: " + read);
						break;

					case AFTER_SLASH:
						if(read == null)
							throw new TokenizingException("Unexpected EOF");
						else if(read.equalsChar('/'))
							state = State.IN_LINE_COMMENT;
						else if(read.equalsChar('*'))
							state = State.IN_MULTILINE_COMMENT;
						else
							throw new TokenizingException("Unexpected char: " + read);
						break;

					case IN_INTEGER:
						if(read.compareToChar('0') >= 0 && read.compareToChar('9') <= 0)
							integer = integer * 10 + read.toPrimitiveChar() - '0';
						else
						{
							nextChar = read;
							state = State.DEFAULT;
							return new Token<>(INTEGER, sign * integer);
						}
						break;

					case IN_IDENTIFIER:
						if(read.isJavaIdentifierPart())
							sb.append(read.toStringNoEscaping());
						else
						{
							nextChar = read;
							state = State.DEFAULT;
							return new Token<>(IDENTIFIER, readAndClearSB());
						}
						break;

					case IN_LITERAL:
						if(read == null || read.equalsChar('\r') || read.equalsChar('\n'))
							throw new TokenizingException("Unterminated literal");
						else if(read.equalsChar('\''))
						{
							state = State.DEFAULT;
							return new Token<>(TERMINAL, readAndClearSB());
						} else if(read.equalsChar('\\'))
							state = State.IN_LITERAL_NEXT_ESCAPED;
						else
							sb.append(read.toStringNoEscaping());
						break;

					case IN_LITERAL_NEXT_ESCAPED:
						state = State.IN_LITERAL;
						if(read == null || read.equalsChar('\r') || read.equalsChar('\n'))
							throw new TokenizingException("Unterminated literal");
						else if(read.equalsChar('r'))
							sb.append('\r');
						else if(read.equalsChar('n'))
							sb.append('\n');
						else
							sb.append(read.toStringNoEscaping());
						break;

					case IN_STRING:
						if(read == null || read.equalsChar('\r') || read.equalsChar('\n'))
							throw new TokenizingException("Unterminated string");
						else if(read.equalsChar('"'))
							state = State.DEFAULT;
						else if(read.equalsChar('\\'))
							state = State.IN_STRING_NEXT_ESCAPED;
						else
							return new Token<>(TERMINAL, read.toStringNoEscaping());
						break;

					case IN_STRING_NEXT_ESCAPED:
						state = State.IN_STRING;
						if(read == null || read.equalsChar('\r') || read.equalsChar('\n'))
							throw new TokenizingException("Unterminated string");
						else if(read.equalsChar('r'))
							return new Token<>(TERMINAL, "\r");
						else if(read.equalsChar('n'))
							return new Token<>(TERMINAL, "\n");
						else
							return new Token<>(TERMINAL, read.toStringNoEscaping());

					case IN_LINE_COMMENT:
						// No need to handle \r\n and such: newlines are whitespace and thus ignored by the DEFAULT state anyway
						if(read == null)
							break;
						else if(read.isNewline())
							state = State.DEFAULT;
						break;

					case IN_MULTILINE_COMMENT:
						if(read == null)
							break;
						else if(read.equalsChar('*'))
							state = State.IN_MULTILINE_COMMENT_AFTER_STAR;
						break;

					case IN_MULTILINE_COMMENT_AFTER_STAR:
						if(read == null)
							break;
						else if(read.equalsChar('/'))
							state = State.DEFAULT;
						else if(read.equalsChar('*'))
							state = State.IN_MULTILINE_COMMENT_AFTER_STAR;
						else
							state = State.IN_MULTILINE_COMMENT;
						break;

					default:
						throw new IllegalStateException("Unknown enum constant: " + state);
				}
			}
		}

		@Override
		public String getCurrentLocationDescription()
		{
			return locationDescriptor.getLocationDescription();
		}

		private String readAndClearSB()
		{
			String result = sb.toString();
			sb.setLength(0);
			return result;
		}

		private static enum State
		{
			DEFAULT,
			AFTER_MINUS,
			AFTER_SLASH,
			IN_INTEGER,
			IN_IDENTIFIER,
			IN_LITERAL,
			IN_LITERAL_NEXT_ESCAPED,
			IN_STRING,
			IN_STRING_NEXT_ESCAPED,
			IN_LINE_COMMENT,
			IN_MULTILINE_COMMENT,
			IN_MULTILINE_COMMENT_AFTER_STAR;
		}
	}
}
