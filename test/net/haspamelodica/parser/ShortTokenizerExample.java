package net.haspamelodica.parser;

import net.haspamelodica.parser.ast.Token;
import net.haspamelodica.parser.parser.ParseException;
import net.haspamelodica.parser.tokenizer.CharReader;
import net.haspamelodica.parser.tokenizer.TokenStream;
import net.haspamelodica.parser.tokenizer.Tokenizer;
import net.haspamelodica.parser.tokenizer.regexbased.RegexBasedTokenizerParser;

public class ShortTokenizerExample
{
	public static void main(String[] args) throws ParseException
	{
		Tokenizer<CharReader> tokenizer = createTokenizer();

		//create some example input
		CharReader input = CharReader.readString("somename * 123 + 456");

		//let the tokenizer parse the input
		TokenStream tokens = tokenizer.tokenize(input);
		for(;;)
		{
			Token<?> token = tokens.nextToken();
			if(token == null)
				break;
			System.out.println(token);
		}
	}

	public static Tokenizer<CharReader> createTokenizer() throws ParseException
	{
		return RegexBasedTokenizerParser.create("""
				* = \\\\* ;
				+ = \\\\+ ;
				( = \\\\( ;
				) = \\\\) ;
				int = [0-9]+ ;
				name = <JavaIdentifierStart><JavaIdentifierPart>* ;
				== <Whitespace>* ;
				""");
	}
}
