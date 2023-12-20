package net.haspamelodica.parser.tokenizer.regexbased;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.haspamelodica.parser.ast.InnerNode;
import net.haspamelodica.parser.generics.ParameterizedTypeImpl;
import net.haspamelodica.parser.generics.TypedFunction;
import net.haspamelodica.parser.grammar.attributes.Attribute;
import net.haspamelodica.parser.grammar.attributes.AttributeValue;
import net.haspamelodica.parser.grammar.attributes.evaluating.lattributed.LAttributedEvaluator;
import net.haspamelodica.parser.grammar.parser.AttributeGrammarParseResult;
import net.haspamelodica.parser.grammar.parser.GrammarParser;
import net.haspamelodica.parser.parser.ParseException;
import net.haspamelodica.parser.parser.Parser;
import net.haspamelodica.parser.parser.lrk.LRkParserGenerator;
import net.haspamelodica.parser.tokenizer.Char;
import net.haspamelodica.parser.tokenizer.CharGroup;
import net.haspamelodica.parser.tokenizer.CharReader;
import net.haspamelodica.parser.tokenizer.Tokenizer;
import net.haspamelodica.parser.tokenizer.charbased.CharGroupTokenizer;
import net.haspamelodica.parser.tokenizer.regexbased.regex.Regex;
import net.haspamelodica.parser.tokenizer.regexbased.regex.RegexNode;

public class RegexParser
{
	private static final String attributeGrammarString = """
			Alt
			 ->                { regex[-1] = epsilon(); }
			  | Con            { regex[-1] = regex[0]; namedgroups[0] = namedgroups[-1]; }
			  | '|' Alt        { regex[-1] = alternation(epsilon(), regex[1]); namedgroups[1] = namedgroups[-1]; }
			  | Con '|' Alt    { regex[-1] = alternation(regex[0], regex[2]); namedgroups[0] = namedgroups[-1]; namedgroups[2] = namedgroups[-1]; } ;
			Con
			 -> Star           { regex[-1] = regex[0]; namedgroups[0] = namedgroups[-1]; }
			  | Star Con       { regex[-1] = concatenation(regex[0], regex[1]); namedgroups[0] = namedgroups[-1]; namedgroups[1] = namedgroups[-1]; } ;
			Star
			 -> Atom           { regex[-1] = regex[0]; namedgroups[0] = namedgroups[-1]; }
			  | Atom '*'       { regex[-1] = star(regex[0]); namedgroups[0] = namedgroups[-1]; }
			  | Atom '+'       { regex[-1] = concatenation(duplicate(regex[0]), star(regex[0])); namedgroups[0] = namedgroups[-1]; } ;
			Atom
			 -> '(' Alt ')'    { regex[-1] = regex[1]; namedgroups[1] = namedgroups[-1]; }
			  | "[^" SAlt ']'  { regex[-1] = symbol(invgroup(group[2])); }
			  | '[' SAlt ']'   { regex[-1] = symbol(group[1]); }
			  | Sym            { regex[-1] = symbol(rangegroup(char[0], char[0])); }
			  | '-'            { regex[-1] = symbol(rangegroup([0], [0])); }
			  | '<' CGName '>' { regex[-1] = symbol(namedgroup(namedgroups[-1], name[1])); } ;
			SAlt
			 -> SAltAtom       { group[-1] = group[0]; }
			  | SAlt SAltAtom  { group[-1] = alternationgroup(group[0], group[1]); } ;
			SAltAtom
			 -> Sym            { group[-1] = rangegroup(char[0], char[0]); }
			  | Sym '-' Sym    { group[-1] = rangegroup(char[0], char[2]); } ;
			Sym
			 -> '\\\\' EscChar { char[-1] = unescape(char[1]); }
			  | 'c'            { char[-1] = [0]; }
			  | 'r'            { char[-1] = [0]; }
			  | 'n'            { char[-1] = [0]; };
			EscChar
			 -> 'r'            { char[-1] = [0]; }
			  | 'n'            { char[-1] = [0]; }
			  | '('            { char[-1] = [0]; }
			  | ')'            { char[-1] = [0]; }
			  | '['            { char[-1] = [0]; }
			  | ']'            { char[-1] = [0]; }
			  | '<'            { char[-1] = [0]; }
			  | '>'            { char[-1] = [0]; }
			  | '-'            { char[-1] = [0]; }
			  | '\\\\'         { char[-1] = [0]; }
			  | '*'            { char[-1] = [0]; }
			  | '+'            { char[-1] = [0]; }
			  | '|'            { char[-1] = [0]; }
			  | '^'            { char[-1] = [0]; } ;
			CGName
			 -> 'c'            { name[-1] = ctostr([0]); }
			  | 'r'            { name[-1] = ctostr([0]); }
			  | 'n'            { name[-1] = ctostr([0]); }
			  | CGName 'c'     { name[-1] = concat(name[0], ctostr([1])); }
			  | CGName 'r'     { name[-1] = concat(name[0], ctostr([1])); }
			  | CGName 'n'     { name[-1] = concat(name[0], ctostr([1])); } ;
			""";

	private static final Tokenizer<CharReader>	tokenizer;
	private static final Parser					parser;
	private static final LAttributedEvaluator	attributeEvaluator;

	private static final Attribute<RegexNode>				regexAttr;
	private static final Attribute<Map<String, CharGroup>>	namedCharGroupsAttr;

	static
	{
		tokenizer = CharGroupTokenizer.buildSimple("rn()[]<>-\\*+|^", "c");

		Map<String, TypedFunction> functions = new HashMap<>();
		functions.put("epsilon", TypedFunction.build(RegexNode::epsilon, RegexNode.class));
		functions.put("symbol", TypedFunction.build(RegexNode::symbol, RegexNode.class, CharGroup.class));
		functions.put("alternation", TypedFunction.build(RegexNode::alternation, RegexNode.class, RegexNode.class, RegexNode.class));
		functions.put("concatenation", TypedFunction.build(RegexNode::concatenation, RegexNode.class, RegexNode.class, RegexNode.class));
		functions.put("star", TypedFunction.build(RegexNode::star, RegexNode.class, RegexNode.class));
		functions.put("duplicate", TypedFunction.build(RegexNode::duplicate, RegexNode.class, RegexNode.class));
		functions.put("unescape", TypedFunction.build(c -> switch(c.isRepresentableAsPrimitiveChar() ? c.toPrimitiveChar() : (char) -1)
		{
			case 'r' -> Char.fromPrimitiveChar('\r');
			case 'n' -> Char.fromPrimitiveChar('\n');
			case '(', ')', '[', ']', '<', '>', '-', '\\', '*', '+', '|', '^' -> c;
			default -> throw new IllegalArgumentException("The character '" + c + "' has no unescaped variant");
		}, Char.class, Char.class));
		functions.put("rangegroup", TypedFunction.build((from, to) ->
		{
			if(from.compareTo(to) > 0)
				throw new IllegalArgumentException("The range " + (from + "-" + to) + " is not valid");
			return CharGroup.build("[" + from + "-" + to + "]", c -> c.compareTo(from) >= 0 && c.compareTo(to) <= 0);
		}, CharGroup.class, Char.class, Char.class));
		functions.put("namedgroup", TypedFunction.buildT((Map<String, CharGroup> map, String name) ->
		{
			CharGroup charGroup = map.get(name);
			if(charGroup == null)
				throw new IllegalArgumentException("No char group with name " + name);
			return charGroup;
		},
				CharGroup.class, new ParameterizedTypeImpl(null, Map.class, String.class, CharGroup.class), String.class));
		functions.put("alternationgroup", TypedFunction.build((cg1, cg2) -> cg1.alternation("[" + cg1 + cg2 + "]", cg2), CharGroup.class, CharGroup.class, CharGroup.class));
		functions.put("invgroup", TypedFunction.build(cg -> cg.invert("[^" + cg + "]"), CharGroup.class, CharGroup.class));
		functions.put("ctostr", TypedFunction.build(Char::toStringNoEscaping, String.class, Char.class));
		functions.put("concat", TypedFunction.build(String::concat, String.class, String.class, String.class));

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
		Attribute<RegexNode> regexL =
				(Attribute<RegexNode>) grammarParseResult.getAttributesByName().get("regex");
		regexAttr = regexL;
		@SuppressWarnings("unchecked")
		Attribute<Map<String, CharGroup>> namedCharGroupsAttrL =
				(Attribute<Map<String, CharGroup>>) grammarParseResult.getAttributesByName().get("namedgroups");
		namedCharGroupsAttr = namedCharGroupsAttrL;
	}

	private static final Map<String, CharGroup> standardNamedCharGroups;
	static
	{
		standardNamedCharGroups = Map.of(
				"Whitespace", (Predicate<Char>) Char::isWhitespace,
				"JavaIdentifierStart", Char::isJavaIdentifierStart,
				"JavaIdentifierPart", Char::isJavaIdentifierPart)
				.entrySet().stream().collect(Collectors.toMap(Entry::getKey,
						e -> CharGroup.build("<" + e.getKey() + ">", c -> e.getValue().test(c))));
	}

	public static Regex parse(String in) throws ParseException
	{
		return parse(in, standardNamedCharGroups);
	}
	public static Regex parse(String in, Map<String, CharGroup> namedCharGroups) throws ParseException
	{
		return parse(CharReader.readString(in), namedCharGroups);
	}
	public static Regex parse(CharReader in) throws ParseException
	{
		return parse(in, standardNamedCharGroups);
	}
	public static Regex parse(CharReader in, Map<String, CharGroup> namedCharGroups) throws ParseException
	{
		InnerNode parsedAST = parser.parse(tokenizer.tokenize(in));
		attributeEvaluator.evaluate(parsedAST, Set.of(new AttributeValue<>(namedCharGroupsAttr, namedCharGroups)));
		return new Regex(parsedAST.getValueForAttribute(regexAttr));
	}

	private RegexParser()
	{}
}
