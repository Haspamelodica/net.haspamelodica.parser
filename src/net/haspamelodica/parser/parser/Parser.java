package net.haspamelodica.parser.parser;

import net.haspamelodica.parser.ast.InnerNode;
import net.haspamelodica.parser.tokenizer.TokenStream;

public interface Parser
{
	public InnerNode parse(TokenStream tokens) throws ParseException;
}
