package net.haspamelodica.parser;

import java.util.Map;
import java.util.Set;

import net.haspamelodica.parser.ShortParserExample.ExampleParserResult;
import net.haspamelodica.parser.ast.InnerNode;
import net.haspamelodica.parser.grammar.attributes.AttributeValue;
import net.haspamelodica.parser.parser.ParseException;
import net.haspamelodica.parser.tokenizer.CharReader;
import net.haspamelodica.parser.tokenizer.CharString;
import net.haspamelodica.parser.tokenizer.Tokenizer;

public class ShortParserWithTokenizerExample
{
	public static void main(String[] args) throws ParseException
	{
		//create the example tokenizer
		Tokenizer<CharReader> tokenizer = ShortTokenizerExample.createTokenizer();

		//create the example parser
		ExampleParserResult<CharString> parserResult = ShortParserExample.createExampleParser(tokenizer.allTerminals(),
				CharString.class, CharString.class, s -> Integer.parseInt(s.toStringNoEscaping()));

		//create some input
		CharReader input = CharReader.readString("somename * 123 + 456");

		//let the parsed grammar parse our tokenized input
		InnerNode parsedExpression = parserResult.parser.parse(tokenizer.tokenize(input));
		//let the attribute evaluator calculate the value of the expression, letting the variable "somename" have the value 789...
		parserResult.attributeEvaluator.evaluate(parsedExpression, Set.of(new AttributeValue<>(parserResult.varValuesAttrib,
				Map.of(CharString.ofString("somename"), 789))));
		int result = parsedExpression.getValueForAttribute(parserResult.valueAttrib);
		//...and print the result
		System.out.println(result);
	}
}
