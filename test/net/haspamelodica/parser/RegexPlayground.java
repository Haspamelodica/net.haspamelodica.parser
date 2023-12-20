package net.haspamelodica.parser;

import net.haspamelodica.parser.parser.ParseException;
import net.haspamelodica.parser.tokenizer.regexbased.BerrySethi;
import net.haspamelodica.parser.tokenizer.regexbased.RegexParser;
import net.haspamelodica.parser.tokenizer.regexbased.nfa.NFA;
import net.haspamelodica.parser.tokenizer.regexbased.regex.Regex;

public class RegexPlayground
{
	public static void main(String[] args) throws ParseException
	{
		System.out.println(RegexParser.parse(""));
		System.out.println(RegexParser.parse("a*"));
		System.out.println(RegexParser.parse("a()*"));
		System.out.println(RegexParser.parse("a(|c|c)|a*"));

		Regex regex = RegexParser.parse("a(b|bc)*(cbd|)");
		NFA nfa = BerrySethi.toNFA(regex);
		testAccepts(nfa, true, "a");
		testAccepts(nfa, true, "ab");
		testAccepts(nfa, false, "ac");
		testAccepts(nfa, true, "abc");
		testAccepts(nfa, true, "acbd");
		testAccepts(nfa, true, "abcbcbccbd");
		testAccepts(nfa, true, "acbd");
		testAccepts(nfa, true, "abcbcbcbcbcbcbcbd");
		testAccepts(nfa, true, "abcbcbcbcbcbcbcb");
		testAccepts(nfa, false, "abcbcbcbcbcbcbcd");
	}

	private static void testAccepts(NFA nfa, boolean expected, String input)
	{
		boolean actual = nfa.accepts(input);
		if(actual != expected)
			System.err.println("NFA did " + (actual ? "" : "not ") + "accept " + input + ", but should " + (expected ? "" : "not ") + "have");
	}
}
