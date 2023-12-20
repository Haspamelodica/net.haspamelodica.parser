package net.haspamelodica.parser.tokenizer;

import java.util.Set;

import net.haspamelodica.parser.grammar.Terminal;

public interface Tokenizer<INPUT>
{
	public TokenStream tokenize(INPUT in);
	public Set<Terminal<?>> allTerminals();
}
