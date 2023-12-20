package net.haspamelodica.parser.tokenizer.regexbased;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.haspamelodica.parser.ast.Token;
import net.haspamelodica.parser.grammar.Terminal;
import net.haspamelodica.parser.tokenizer.Char;
import net.haspamelodica.parser.tokenizer.CharReader;
import net.haspamelodica.parser.tokenizer.CharString;
import net.haspamelodica.parser.tokenizer.SimpleLocationDescriptor;
import net.haspamelodica.parser.tokenizer.TokenStream;
import net.haspamelodica.parser.tokenizer.Tokenizer;
import net.haspamelodica.parser.tokenizer.TokenizingException;
import net.haspamelodica.parser.tokenizer.regexbased.nfa.NFA;
import net.haspamelodica.parser.tokenizer.regexbased.nfa.State;
import net.haspamelodica.parser.tokenizer.regexbased.regex.Regex;

public class RegexBasedTokenizer implements Tokenizer<CharReader>
{
	private final List<RegexForTokenizing>	regexes;
	private final Set<Terminal<?>>			allTerminals;

	/**
	 * If, while tokenizing, the longest matching regex has name==null, the matched characters are discarded and another matching attempt is made.
	 */
	public RegexBasedTokenizer(List<NamedRegex> terminalPatternsByName)
	{
		this.regexes = terminalPatternsByName.stream().map(RegexForTokenizing::new).collect(Collectors.toUnmodifiableList());
		this.allTerminals = regexes.stream().map(RegexForTokenizing::getTerminal).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public TokenStream tokenize(CharReader in)
	{
		return new RegexBasedTokenStream(in);
	}

	@Override
	public Set<Terminal<?>> allTerminals()
	{
		return allTerminals;
	}

	private class RegexBasedTokenStream implements TokenStream
	{
		private final List<Char>	lookahead;
		private final List<Char>	currentTokenValue;
		private final List<Char>	afterCurrentToken;

		private final CharReader				in;
		private final SimpleLocationDescriptor	locationDescriptor;

		public RegexBasedTokenStream(CharReader in)
		{
			this.in = in;
			this.lookahead = new ArrayList<>();
			this.currentTokenValue = new ArrayList<>();
			this.afterCurrentToken = new ArrayList<>();

			this.locationDescriptor = new SimpleLocationDescriptor();
		}

		@Override
		public Token<CharString> nextToken() throws TokenizingException
		{
			for(;;)
			{
				List<RegexEvaluatingState> states = regexes.stream().map(RegexEvaluatingState::new)
						.collect(Collectors.toCollection(ArrayList::new));
				RegexEvaluatingState lastFinishedState = null;
				do
				{
					Optional<RegexEvaluatingState> firstFinished = states.stream().filter(RegexEvaluatingState::isFinished).findFirst();
					if(firstFinished.isPresent())
					{
						currentTokenValue.addAll(afterCurrentToken);
						afterCurrentToken.clear();
						lastFinishedState = firstFinished.get();
					}

					Char read = readChar();
					if(read == null)
						break;
					afterCurrentToken.add(read);
					states.forEach(r -> r.step(read));
					states.removeIf(RegexEvaluatingState::canNotBeFinished);
				} while(!states.isEmpty());
				if(lastFinishedState != null)
				{
					if(currentTokenValue.isEmpty())
						if(afterCurrentToken.isEmpty())
							//EOF, but we matched anyway because there is at least one regex allowing epsilon
							return null;
						else
							//Not EOF
							throw new TokenizingException("Longest matched string has length 0 near " + getCurrentLocationDescription() +
									", would result in an endless loop. Used pattern: " + lastFinishedState.getRegex() +
									", next chars: " + new CharString(afterCurrentToken));
					Terminal<CharString> terminal = lastFinishedState.getTerminal();
					if(terminal == null)
					//matched an ignored regex: discard matched chars, try again
					{
						currentTokenValue.clear();
						lookahead.addAll(0, afterCurrentToken);
						afterCurrentToken.clear();
						continue;
					}
					Token<CharString> result = new Token<>(terminal, new CharString(currentTokenValue));
					currentTokenValue.clear();
					lookahead.addAll(0, afterCurrentToken);
					afterCurrentToken.clear();
					return result;
				} else if(afterCurrentToken.isEmpty())
					//EOF
					return null;
				else
					//Not EOF, but still no regex matched
					throw new TokenizingException("Unexpected characters near " + getCurrentLocationDescription() + ": " + afterCurrentToken);

			}
		}

		private Char readChar() throws TokenizingException
		{
			if(!lookahead.isEmpty())
				return lookahead.remove(0);
			else
				try
				{
					Char c = in.read();
					locationDescriptor.advance(c);
					return c;
				} catch(IOException e)
				{
					throw new TokenizingException(e);
				}

		}

		@Override
		public String getCurrentLocationDescription()
		{
			return locationDescriptor.getLocationDescription();
		}
	}

	private class RegexForTokenizing
	{
		private final Regex					regex;
		private final NFA					nfa;
		private final Terminal<CharString>	terminal;
		private final Set<State>			startStates;

		public RegexForTokenizing(NamedRegex regex)
		{
			this.regex = regex.getRegex();
			this.nfa = BerrySethi.toNFA(regex.getRegex());
			this.startStates = Set.of(nfa.getStartState());

			this.terminal = regex.getName() == null ? null : new Terminal<>(regex.getName());
		}

		public Terminal<CharString> getTerminal()
		{
			return terminal;
		}
		public Set<State> getStartStates()
		{
			return startStates;
		}
		public Set<State> getNextStates(Set<State> from, Char trigger)
		{
			return nfa.getNextStates(from.stream(), trigger).collect(Collectors.toUnmodifiableSet());
		}
		public boolean isFinalState(State state)
		{
			return nfa.getFinalState().contains(state);
		}

		@Override
		public String toString()
		{
			return regex.toString();
		}
	}

	private class RegexEvaluatingState
	{
		private final RegexForTokenizing	regex;
		private Set<State>					possibleStates;

		public RegexEvaluatingState(RegexForTokenizing regex)
		{
			this.regex = regex;
			this.possibleStates = regex.getStartStates();
		}

		public RegexForTokenizing getRegex()
		{
			return regex;
		}
		public Terminal<CharString> getTerminal()
		{
			return regex.getTerminal();
		}

		public void step(Char read)
		{
			possibleStates = regex.getNextStates(possibleStates, read);
		}

		public boolean canNotBeFinished()
		{
			//this does not work for all NFAs, but for all NFAs returned by Berry-Sethi.
			return possibleStates.isEmpty();
		}

		public boolean isFinished()
		{
			return possibleStates.stream().anyMatch(regex::isFinalState);
		}
	}
}
