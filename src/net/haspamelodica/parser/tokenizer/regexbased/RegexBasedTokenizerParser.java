package net.haspamelodica.parser.tokenizer.regexbased;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.haspamelodica.parser.ast.InnerNode;
import net.haspamelodica.parser.generics.ParameterizedTypeImpl;
import net.haspamelodica.parser.generics.TypedFunction;
import net.haspamelodica.parser.grammar.attributes.Attribute;
import net.haspamelodica.parser.grammar.attributes.evaluating.lattributed.LAttributedEvaluator;
import net.haspamelodica.parser.grammar.parser.AttributeGrammarParseResult;
import net.haspamelodica.parser.grammar.parser.GrammarParser;
import net.haspamelodica.parser.parser.ParseException;
import net.haspamelodica.parser.parser.Parser;
import net.haspamelodica.parser.parser.lrk.LRkParserGenerator;
import net.haspamelodica.parser.tokenizer.Char;
import net.haspamelodica.parser.tokenizer.CharGroupAndTerminal;
import net.haspamelodica.parser.tokenizer.CharReader;
import net.haspamelodica.parser.tokenizer.Tokenizer;
import net.haspamelodica.parser.tokenizer.charbased.CharGroupTokenizer;
import net.haspamelodica.parser.tokenizer.regexbased.regex.Regex;
import net.haspamelodica.parser.utils.CollectionsUtils;

public class RegexBasedTokenizerParser
{
	private static final String attributeGrammarString = """
			Regexes
			 -> WS                  {regexes[-1] = emptyNRList(); }
			  | Regexes NRegex WS   {regexes[-1] = addNRList(regexes[0], regex[1]); } ;
			NRegex
			 -> Ident WS '='
			    WS RegexWS ';'      {regex[-1] = newNamedRegex(string[0], compileRegex(string[4])); }
			  | "==" WS RegexWS ';' {regex[-1] = newIgnoredRegex(compileRegex(string[3])); } ;
			RegexWS
			 -> RChar RegexWSE      {string[-1] = concat(charToString(char[0]), string[1]); } ;
			RegexWSE
			 -> WS                  {string[-1] = emptyString(); }
			  | WS RChar RegexWSE   {string[-1] = concat(string[0], concat(charToString(char[1]), string[2])); } ;
			Ident
			 -> IChar               {string[-1] = charToString(char[0]); }
			  | Ident IChar         {string[-1] = concat(string[0], charToString(char[1])); } ;
			RChar
			 -> 'c'                 {char[-1] = [0]; }
			  | '\\\\' ';'          {char[-1] = [1]; }
			  | '\\\\' '='          {char[-1] = [1]; }
			  | '\\\\' '\\\\'       {char[-1] = [1]; }
			  | '\\\\' 'w'          {char[-1] = [1]; } ;
			IChar
			 -> 'c'                 {char[-1] = [0]; }
			  | '\\\\' ';'          {char[-1] = [1]; }
			  | '\\\\' '='          {char[-1] = [1]; }
			  | '\\\\' '\\\\'       {char[-1] = [1]; }
			  | '\\\\' 'w'          {char[-1] = [1]; } ;
			WS
			 ->                     {string[-1] = emptyString(); }
			  | WS 'w'              {string[-1] = concat(string[0], charToString([1])); } ;
			""";

	private static final Tokenizer<CharReader>	tokenizer;
	private static final Parser					parser;
	private static final LAttributedEvaluator	attributeEvaluator;

	private static final Attribute<List<NamedRegex>> regexes;

	static
	{
		tokenizer = new CharGroupTokenizer(List.of(
				CharGroupAndTerminal.build("w", "<Whitespace>", Char::isWhitespace),
				CharGroupAndTerminal.build(';'),
				CharGroupAndTerminal.build('='),
				CharGroupAndTerminal.build('\\'),
				CharGroupAndTerminal.buildAll("c")));

		Type T_ListNamedRegex = new ParameterizedTypeImpl(null, List.class, NamedRegex.class);

		Map<String, TypedFunction> functions = Map.of(
				"charToString", TypedFunction.build(c -> c.toStringNoEscaping(), String.class, Char.class),
				"emptyString", TypedFunction.build(() -> "", String.class),
				"concat", TypedFunction.build(String::concat, String.class, String.class, String.class),
				"emptyNRList", TypedFunction.buildT(List::of, T_ListNamedRegex),
				"addNRList", TypedFunction.<List<NamedRegex>, NamedRegex, List<NamedRegex>> buildT(
						CollectionsUtils::pseudoAdd, T_ListNamedRegex, T_ListNamedRegex, NamedRegex.class),
				"compileRegex", TypedFunction.build(t ->
				{
					try
					{
						return RegexParser.parse(t);
					} catch(ParseException e)
					{
						throw new RuntimeException("Error parsing regex " + t, e);
					}
				}, Regex.class, String.class),
				"newNamedRegex", TypedFunction.build(NamedRegex::new, NamedRegex.class, String.class, Regex.class),
				"newIgnoredRegex", TypedFunction.build(r -> new NamedRegex(null, r), NamedRegex.class, Regex.class));

		AttributeGrammarParseResult grammarParseResult;
		try
		{
			grammarParseResult = GrammarParser.parseAttributeGrammar(
					CharReader.readString(attributeGrammarString),
					tokenizer.allTerminals(),
					functions);
		} catch(ParseException e)
		{
			throw new RuntimeException(e);
		}

		parser = LRkParserGenerator.generate(grammarParseResult.getGrammar(), 1);
		attributeEvaluator = new LAttributedEvaluator(grammarParseResult.getAttributeSystem());
		@SuppressWarnings("unchecked")
		Attribute<List<NamedRegex>> regexesL = (Attribute<List<NamedRegex>>) grammarParseResult.getAttributesByName().get("regexes");
		regexes = regexesL;
	}

	public static RegexBasedTokenizer create(String in) throws ParseException
	{
		return new RegexBasedTokenizer(parse(in));
	}
	public static List<NamedRegex> parse(String in) throws ParseException
	{
		return parse(CharReader.readString(in));
	}
	public static RegexBasedTokenizer create(CharReader in) throws ParseException
	{
		return new RegexBasedTokenizer(parse(in));
	}
	public static List<NamedRegex> parse(CharReader in) throws ParseException
	{
		InnerNode parsedAST = parser.parse(tokenizer.tokenize(in));
		attributeEvaluator.evaluate(parsedAST, Set.of());
		return parsedAST.getValueForAttribute(regexes);
	}

	private RegexBasedTokenizerParser()
	{}
}
