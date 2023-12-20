package net.haspamelodica.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.haspamelodica.parser.ast.InnerNode;
import net.haspamelodica.parser.ast.Token;
import net.haspamelodica.parser.generics.ParameterizedTypeImpl;
import net.haspamelodica.parser.generics.TypedFunction;
import net.haspamelodica.parser.grammar.Terminal;
import net.haspamelodica.parser.grammar.attributes.Attribute;
import net.haspamelodica.parser.grammar.attributes.AttributeValue;
import net.haspamelodica.parser.grammar.attributes.evaluating.lattributed.LAttributedEvaluator;
import net.haspamelodica.parser.grammar.parser.AttributeGrammarParseResult;
import net.haspamelodica.parser.grammar.parser.GrammarParser;
import net.haspamelodica.parser.parser.ParseException;
import net.haspamelodica.parser.parser.Parser;
import net.haspamelodica.parser.parser.lrk.LRkParserGenerator;
import net.haspamelodica.parser.tokenizer.CharReader;
import net.haspamelodica.parser.tokenizer.TokenStream;

public class ShortParserExample
{
	public static void main(String[] args) throws ParseException
	{
		//--- define the grammar

		//which terminals does the tokenizer output?
		Terminal<Void> plus = new Terminal<>("+");
		Terminal<Void> times = new Terminal<>("*");
		Terminal<Void> openingParen = new Terminal<>("(");
		Terminal<Void> closingParen = new Terminal<>(")");
		Terminal<String> name = new Terminal<>("name");
		Terminal<Integer> integer = new Terminal<>("int");
		Terminal<Void> whitespace = new Terminal<>("whitespace");
		Set<Terminal<?>> allTerminals = Set.of(plus, times, openingParen, closingParen, name, integer, whitespace);

		ExampleParserResult<String> parserResult = createExampleParser(allTerminals, String.class, Integer.class, Function.identity());


		//--- try out the parsed grammar

		//mocked input: somename*123 + 456
		Queue<Token<?>> tokens = new LinkedList<>(List.of(
				new Token<>(name, "somename"),
				new Token<>(times, null),
				new Token<>(integer, 123),
				new Token<>(plus, null),
				new Token<>(integer, 456)));
		TokenStream mockedTokens = tokens::poll;

		//let the parsed grammar parse our mocked input
		InnerNode parsedExpression = parserResult.parser.parse(mockedTokens);
		//let the attribute evaluator calculate the value of the expression, letting the variable "somename" have the value 789...
		parserResult.attributeEvaluator.evaluate(parsedExpression, Set.of(new AttributeValue<>(parserResult.varValuesAttrib, Map.of("somename", 789))));
		int result = parsedExpression.getValueForAttribute(parserResult.valueAttrib);
		//...and print the result
		System.out.println(result);
	}

	public static <V, N> ExampleParserResult<V> createExampleParser(Set<Terminal<?>> allTerminals, Class<V> variableNameType,
			Class<N> rawIntType, Function<N, Integer> extractIntFromRawInt) throws ParseException
	{
		//which functions can be used in attribute equations?
		Map<String, TypedFunction> functions = Map.of(
				"add", TypedFunction.build(Integer::sum, Integer.class, Integer.class, Integer.class),
				"mult", TypedFunction.build((a, b) -> a * b, Integer.class, Integer.class, Integer.class),
				"mapGet", TypedFunction.buildT((BiFunction<Map<V, Integer>, V, Integer>) Map::get,
						Integer.class, new ParameterizedTypeImpl(null, Map.class, variableNameType, Integer.class), variableNameType),
				"int", TypedFunction.build(extractIntFromRawInt, Integer.class, rawIntType));

		String grammarString = """
				S -> E         {value[-1] = value[0];                   varValues[0] = varValues[-1]; };
				E -> T         {value[-1] = value[0];                   varValues[0] = varValues[-1]; }
				   | E '+' T   {value[-1] = add(value[0], value[2]);    varValues[0] = varValues[-1]; varValues[2] = varValues[-1]; };
				T -> F         {value[-1] = value[0];                   varValues[0] = varValues[-1]; }
				   | T '*' F   {value[-1] = mult(value[0], value[2]);   varValues[0] = varValues[-1]; varValues[2] = varValues[-1]; };
				F -> '(' E ')' {value[-1] = value[1];                   varValues[1] = varValues[-1]; }
				   | 'name'    {value[-1] = mapGet(varValues[-1], [0]); }
				   | 'int'     {value[-1] = int([0]); };
				""";

		//--- parse the grammar

		//parse it!
		AttributeGrammarParseResult grammarParseResult = GrammarParser.parseAttributeGrammar(CharReader.readString(grammarString), allTerminals, functions);
		//create a parser for the parsed grammar
		Parser parser = LRkParserGenerator.generate(grammarParseResult.getGrammar(), 1);
		//get references to the two attributes defined in the attribute system
		Map<String, Attribute<?>> attributesByName = grammarParseResult.getAttributesByName();
		@SuppressWarnings("unchecked")
		Attribute<Integer> valueAttrib = (Attribute<Integer>) attributesByName.get("value");
		@SuppressWarnings("unchecked")
		Attribute<Map<V, Integer>> varValuesAttrib = (Attribute<Map<V, Integer>>) attributesByName.get("varValues");

		//create an attribute evaluator for the attribute system of the parsed grammar
		LAttributedEvaluator attributeEvaluator = new LAttributedEvaluator(grammarParseResult.getAttributeSystem());

		return new ExampleParserResult<>(parser, attributeEvaluator, valueAttrib, varValuesAttrib);
	}

	public static final class ExampleParserResult<N>
	{
		public final Parser						parser;
		public final LAttributedEvaluator		attributeEvaluator;
		public final Attribute<Integer>			valueAttrib;
		public final Attribute<Map<N, Integer>>	varValuesAttrib;

		public ExampleParserResult(Parser parser, LAttributedEvaluator attributeEvaluator, Attribute<Integer> valueAttrib, Attribute<Map<N, Integer>> varValuesAttrib)
		{
			this.parser = parser;
			this.attributeEvaluator = attributeEvaluator;
			this.valueAttrib = valueAttrib;
			this.varValuesAttrib = varValuesAttrib;
		}
	}
}
