package net.haspamelodica.parser.parser.lrk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import net.haspamelodica.parser.grammar.ContextFreeGrammar;
import net.haspamelodica.parser.grammar.Nonterminal;
import net.haspamelodica.parser.grammar.Production;
import net.haspamelodica.parser.grammar.RightHandSide;
import net.haspamelodica.parser.grammar.Symbol;
import net.haspamelodica.parser.grammar.Terminal;

public class ConcatKHelper
{
	private final int k;

	private final Map<Symbol, Set<Word>> symbolFirstKs;

	public ConcatKHelper(ContextFreeGrammar grammar, int k)
	{
		this.k = k;

		this.symbolFirstKs = calculateFirstK(grammar, k);
	}

	public static Map<Symbol, Set<Word>> calculateFirstK(ContextFreeGrammar grammar, int k)
	{
		Map<Symbol, Set<Word>> firstKPerSymbol = new HashMap<>();
		for(Nonterminal n : grammar.getAllNonterminals())
			firstKPerSymbol.put(n, new HashSet<>());
		for(Terminal<?> t : grammar.getAllTerminals())
			firstKPerSymbol.put(t, new HashSet<>(Set.of(new Word(t))));

		//TODO is this correct?
		//TODO is this possible faster?
		//TODO almost a duplicate of CanonicalAutomatonGenerator.expandLookaheads
		Queue<Symbol> changedSymbols = new LinkedList<>(grammar.getAllTerminals());
		for(Production prod : grammar.getProductions())
			if(prod.getRhs().getSymbols().isEmpty())
			{
				firstKPerSymbol.get(prod.getLhs()).add(Word.EPSILON);
				changedSymbols.add(prod.getLhs());
			}
		for(Symbol changedSymbol = changedSymbols.poll(); changedSymbol != null; changedSymbol = changedSymbols.poll())
			for(Nonterminal lhs : grammar.getAllNonterminals())
			{
				Set<Production> productionsForLhs = grammar.getProductionsForLhs(lhs);
				if(productionsForLhs == null)
					throw new IllegalArgumentException(lhs + " is unproductive");
				boolean containsChangedSymbol = productionsForLhs
						.stream()
						.map(Production::getRhs)
						.map(RightHandSide::getSymbols)
						.flatMap(List::stream)
						.anyMatch(changedSymbol::equals);
				if(containsChangedSymbol)
				{
					Set<Word> newFirstK = productionsForLhs
							.stream()
							.map(Production::getRhs)
							.map(RightHandSide::getSymbols)
							.map(List::stream)
							.map(l -> l.map(firstKPerSymbol::get).reduce(Set.of(Word.EPSILON), (a, b) -> concatK(a, b, k)))
							.flatMap(Set::stream)
							.collect(Collectors.toSet());
					if(!newFirstK.equals(firstKPerSymbol.put(lhs, newFirstK)))
						changedSymbols.add(lhs);
				}
			}
		return firstKPerSymbol;
	}

	public Set<Word> concatK(Set<Word> a, Set<Word> b)
	{
		return concatK(a, b, k);
	}
	public static Set<Word> concatK(Set<Word> a, Set<Word> b, int k)
	{
		Set<Word> result = new HashSet<>();
		Map<Integer, Set<Word>> bPrefixCache = new HashMap<>();
		for(Word wa : a)
		{
			if(wa.getLength() == k)
				result.add(wa);
			else if(wa.getLength() > k)
				result.add(wa.prefix(k));
			else
				bPrefixCache.computeIfAbsent(k - wa.getLength(), n -> firstK(b, n)).stream().map(wa::concat).forEach(result::add);
		}
		return result;
	}

	public Set<Word> firstK(Set<Word> a)
	{
		return firstK(a, k);
	}
	public static Set<Word> firstK(Set<Word> a, int k)
	{
		return a.stream().map(w -> w.getLength() > k ? w.prefix(k) : w).collect(Collectors.toSet());
	}

	public Set<Word> concatKSymbols(List<Symbol> symbols)
	{
		return concatKSymbols(symbols, symbolFirstKs, k);
	}
	public static Set<Word> concatKSymbols(List<Symbol> symbols, Map<Symbol, Set<Word>> symbolFirstKs, int k)
	{
		return symbols
				.stream()
				.map(symbolFirstKs::get)
				.reduce((a, b) -> ConcatKHelper.concatK(a, b, k))
				.orElse(Set.of(Word.EPSILON));
	}

}
