package net.haspamelodica.parser.grammar;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.haspamelodica.parser.grammar.Symbol.SymbolType;

public class ContextFreeGrammar
{
	private final Nonterminal		startSymbol;
	private final Set<Production>	productions;

	private final Map<Nonterminal, Set<Production>>	productionsPerLhs;
	private final Set<Terminal<?>>					allTerminals;
	private final Set<Nonterminal>					allNonterminals;
	private final Set<Symbol>						allSymbols;
	private final boolean							isNormalized;

	private ContextFreeGrammar normalizedGrammar;

	public ContextFreeGrammar(Nonterminal startSymbol, Set<Production> productions)
	{
		this.startSymbol = startSymbol;
		this.productions = Set.copyOf(productions);

		this.productionsPerLhs = calculateProductionsPerLhs(this.productions);

		Set<Terminal<?>> allTerminalsModifiable = new HashSet<>();
		Set<Nonterminal> allNonterminalsModifiable = new HashSet<>();
		Set<Symbol> allSymbolsModifiable = new HashSet<>();
		calculateAllSymbols(this.productions, allTerminalsModifiable, allNonterminalsModifiable, allSymbolsModifiable);
		this.allTerminals = Collections.unmodifiableSet(allTerminalsModifiable);
		this.allNonterminals = Collections.unmodifiableSet(allNonterminalsModifiable);
		this.allSymbols = Collections.unmodifiableSet(allSymbolsModifiable);
		this.isNormalized = calculateIsNormalized(startSymbol, this.productions, productionsPerLhs);
		if(isNormalized)
			normalizedGrammar = this;
	}

	private static Map<Nonterminal, Set<Production>> calculateProductionsPerLhs(Set<Production> productions)
	{
		return productions.stream().collect(Collectors.groupingBy(
				Production::getLhs,
				Collectors.toUnmodifiableSet()));
	}

	private static void calculateAllSymbols(Set<Production> productions, Set<Terminal<?>> allTerminals, Set<Nonterminal> allNonterminals, Set<Symbol> allSymbolsModifiable)
	{
		for(Production production : productions)
		{
			allNonterminals.add(production.getLhs());
			allSymbolsModifiable.add(production.getLhs());
			for(Symbol s : production.getRhs().getSymbols())
			{
				allSymbolsModifiable.add(s);
				switch(s.getType())
				{
					case TERMINAL:
						allTerminals.add((Terminal<?>) s);
						break;
					case NONTERMINAL:
						allNonterminals.add((Nonterminal) s);
						break;
					default:
						throw new IllegalArgumentException("Unknown enum constant: " + s.getType());
				}
			}
		}
	}

	private static boolean calculateIsNormalized(Nonterminal startSymbol, Set<Production> productions, Map<Nonterminal, Set<Production>> productionsPerLhs)
	{
		if(productionsPerLhs.get(startSymbol).size() > 1)
			return false;
		return productions
				.stream()
				.map(Production::getRhs)
				.map(RightHandSide::getSymbols)
				.flatMap(List::stream)
				.anyMatch(startSymbol::equals);
	}

	public Nonterminal getStartSymbol()
	{
		return startSymbol;
	}
	public Set<Production> getProductions()
	{
		return productions;
	}
	public Set<Production> getProductionsForLhs(Nonterminal lhs)
	{
		return productionsPerLhs.get(lhs);
	}
	public Set<Terminal<?>> getAllTerminals()
	{
		return allTerminals;
	}
	public Set<Nonterminal> getAllNonterminals()
	{
		return allNonterminals;
	}
	public Set<Symbol> getAllSymbols()
	{
		return allSymbols;
	}
	public boolean isNormalized()
	{
		return isNormalized;
	}
	public ContextFreeGrammar normalize()
	{
		if(normalizedGrammar == null)
			normalizedGrammar = calculateNormalizedGrammar();
		return normalizedGrammar;
	}

	private ContextFreeGrammar calculateNormalizedGrammar()
	{
		Nonterminal newStartSymbol = new Nonterminal("S");
		while(allNonterminals.contains(newStartSymbol))
			newStartSymbol = new Nonterminal(newStartSymbol.getName() + "'");
		Set<Production> newProductions = new HashSet<>(productions);
		newProductions.add(Production.build(newStartSymbol, startSymbol));
		return new ContextFreeGrammar(newStartSymbol, newProductions);
	}

	public Set<Nonterminal> calculateReachableNonterminals()
	{
		//This algorithm probably is not optimal
		Set<Nonterminal> reachableNonterminals = Set.of(startSymbol);
		for(;;)
		{
			Set<Nonterminal> newReachableNonterminals = Stream.concat(reachableNonterminals.stream(), reachableNonterminals
					.stream()
					.map(this::getProductionsForLhs)
					.filter(Objects::nonNull) //happens for unproductive Nonterminals
					.flatMap(Set::stream)
					.map(Production::getRhs)
					.map(RightHandSide::getSymbols)
					.flatMap(List::stream)
					.filter(s -> s.getType() == SymbolType.NONTERMINAL)
					.map(s -> (Nonterminal) s))
					.collect(Collectors.toUnmodifiableSet());
			if(newReachableNonterminals.equals(reachableNonterminals))
				return newReachableNonterminals;
			reachableNonterminals = newReachableNonterminals;
		}
	}

	public Set<Nonterminal> calculateUnreachableNonterminals()
	{
		Set<Nonterminal> unreachableNonterminals = new HashSet<>(getAllNonterminals());
		unreachableNonterminals.removeAll(calculateReachableNonterminals());
		return Collections.unmodifiableSet(unreachableNonterminals);
	}

	@Override
	public String toString()
	{
		return productions
				.stream()
				.map(Production::toStringNoBrackets)
				.collect(Collectors.joining(System.lineSeparator(),
						"start symbol: " + startSymbol + System.lineSeparator(),
						""));
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((productions == null) ? 0 : productions.hashCode());
		result = prime * result + ((startSymbol == null) ? 0 : startSymbol.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		ContextFreeGrammar other = (ContextFreeGrammar) obj;
		if(productions == null)
		{
			if(other.productions != null)
				return false;
		} else if(!productions.equals(other.productions))
			return false;
		if(startSymbol == null)
		{
			if(other.startSymbol != null)
				return false;
		} else if(!startSymbol.equals(other.startSymbol))
			return false;
		return true;
	}
}
