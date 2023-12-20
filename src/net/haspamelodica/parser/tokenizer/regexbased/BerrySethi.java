package net.haspamelodica.parser.tokenizer.regexbased;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.haspamelodica.parser.tokenizer.regexbased.nfa.NFA;
import net.haspamelodica.parser.tokenizer.regexbased.nfa.NFABuilder;
import net.haspamelodica.parser.tokenizer.regexbased.nfa.State;
import net.haspamelodica.parser.tokenizer.regexbased.regex.Regex;
import net.haspamelodica.parser.tokenizer.regexbased.regex.RegexNode;
import net.haspamelodica.parser.tokenizer.regexbased.regex.Symbol;

public class BerrySethi
{
	private BerrySethi()
	{}

	public static NFA toNFA(Regex regex)
	{
		RegexNode root = regex.getRoot();
		List<Symbol> allSymbolsLeftToRight = root.getSymbolsLeftToRight();
		Map<Symbol, State> states = new HashMap<>();
		State startState = new State("start");
		for(int i = 0; i < allSymbolsLeftToRight.size(); i ++)
		{
			Symbol symbol = allSymbolsLeftToRight.get(i);
			states.put(symbol, new State(symbol + "(" + i + ")"));
		}

		NFABuilder nfa = NFA.builder();

		nfa.startState(startState);
		for(Symbol first : root.getFirstSet())
			nfa.addTransition(startState, first.getSymbol(), states.get(first));

		for(Symbol letter : allSymbolsLeftToRight)
			for(Symbol next : letter.getNextSet())
				nfa.addTransition(states.get(letter), next.getSymbol(), states.get(next));

		for(Symbol last : root.getLastSet())
			nfa.addFinalState(states.get(last));
		if(root.isEmpty())
			nfa.addFinalState(startState);
		return nfa.build();
	}
}
