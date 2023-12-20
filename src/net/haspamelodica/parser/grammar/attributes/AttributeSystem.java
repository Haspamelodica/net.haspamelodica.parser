package net.haspamelodica.parser.grammar.attributes;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.haspamelodica.parser.grammar.ContextFreeGrammar;
import net.haspamelodica.parser.grammar.Production;

public class AttributeSystem
{
	private final ContextFreeGrammar							grammar;
	private final Map<Production, Set<AttributeEquation<?>>>	attributeEquations;

	private final Set<Attribute<?>> allAttributes;

	public AttributeSystem(ContextFreeGrammar grammar, Set<AttributeEquation<?>> attributeEquations)
	{
		this.grammar = grammar;
		this.attributeEquations = calculateAttributeEquations(attributeEquations);
		this.allAttributes = calculateAllAttributes(this.attributeEquations);
		checkAttributeSystem();
	}

	private static Map<Production, Set<AttributeEquation<?>>> calculateAttributeEquations(Set<AttributeEquation<?>> attributeEquations)
	{
		return Collections.unmodifiableMap(attributeEquations
				.stream()
				.collect(Collectors.groupingBy(AttributeEquation::getRootProduction,
						Collectors.toUnmodifiableSet())));
	}
	private static Set<Attribute<?>> calculateAllAttributes(Map<Production, Set<AttributeEquation<?>>> attributeEquations)
	{
		return attributeEquations
				.values()
				.stream()
				.flatMap(Set::stream)
				.flatMap(eq -> Stream.concat(Stream.of(eq.getReturnValue()), eq
						.getParameters()
						.stream()
						.filter(p -> switch(p.getType())
						{
							case ATTRIBUTE_VALUE -> true;
							case TERMINAL_VALUE -> false;
							default -> throw new IllegalArgumentException("Unknown enum constant: " + p.getType());
						})
						.map(p -> (AttributeValueReference<?>) p)))
				.map(AttributeValueReference::getAttribute)
				.collect(Collectors.toUnmodifiableSet());
	}
	private void checkAttributeSystem()
	{
		for(Production p : attributeEquations.keySet())
			if(!grammar.getProductions().contains(p))
				throw new IllegalArgumentException("Production " + p + " is not part of the grammar");
	}

	public ContextFreeGrammar getGrammar()
	{
		return grammar;
	}
	public Map<Production, Set<AttributeEquation<?>>> getAttributeEquations()
	{
		return attributeEquations;
	}
	public Set<Attribute<?>> getAllAttributes()
	{
		return allAttributes;
	}
}
