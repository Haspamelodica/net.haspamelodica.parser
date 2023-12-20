package net.haspamelodica.parser.grammar.attributes.evaluating.lattributed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import net.haspamelodica.parser.ast.ASTNode;
import net.haspamelodica.parser.ast.InnerNode;
import net.haspamelodica.parser.grammar.Nonterminal;
import net.haspamelodica.parser.grammar.Production;
import net.haspamelodica.parser.grammar.Symbol;
import net.haspamelodica.parser.grammar.attributes.Attribute;
import net.haspamelodica.parser.grammar.attributes.AttributeEquation;
import net.haspamelodica.parser.grammar.attributes.AttributeSystem;
import net.haspamelodica.parser.grammar.attributes.AttributeValue;
import net.haspamelodica.parser.grammar.attributes.AttributeValueReference;
import net.haspamelodica.parser.grammar.attributes.SymbolValueReference;

public class LAttributedEvaluator
{
	private final AttributeSystem attributeSystem;

	private final Set<Attribute<?>>									inheritedAttributes;
	private final Map<Nonterminal, Set<Attribute<?>>>				inheritedAttributesPerNonterminal;
	private final Map<Production, List<Set<AttributeEquation<?>>>>	inheritedAttributeEquations;
	private final Set<Attribute<?>>									synthesizedAttributes;
	private final Map<Nonterminal, Set<Attribute<?>>>				synthesizedAttributesPerNonterminal;
	private final Map<Production, Set<AttributeEquation<?>>>		synthesizedAttributeEquations;

	public LAttributedEvaluator(AttributeSystem attributeSystem)
	{
		this.attributeSystem = attributeSystem;

		Map<Production, Set<AttributeEquation<?>>> inheritedAttributeEquationsUnsorted = new HashMap<>();
		this.synthesizedAttributeEquations = splitAttributeEquations(attributeSystem, inheritedAttributeEquationsUnsorted);
		this.synthesizedAttributes = extractAttributesFromSynthesized(synthesizedAttributeEquations);
		this.inheritedAttributeEquations = sortInheritedAttributeEquations(synthesizedAttributes, inheritedAttributeEquationsUnsorted);
		this.inheritedAttributes = extractAttributesFromInherited(inheritedAttributeEquations);

		Map<Nonterminal, Set<Attribute<?>>> inheritedAttributesPerNonterminalModifiable = new HashMap<>();
		Map<Nonterminal, Set<Attribute<?>>> synthesizedAttributesPerNonterminalModifiable = new HashMap<>();
		calculateAttributesPerNonterminal(attributeSystem, inheritedAttributes, inheritedAttributesPerNonterminalModifiable, synthesizedAttributesPerNonterminalModifiable);
		this.inheritedAttributesPerNonterminal = Collections.unmodifiableMap(inheritedAttributesPerNonterminalModifiable);
		this.synthesizedAttributesPerNonterminal = Collections.unmodifiableMap(synthesizedAttributesPerNonterminalModifiable);

		checkLAttributeSystem();
	}

	private static void calculateAttributesPerNonterminal(AttributeSystem attributeSystem, Set<Attribute<?>> inheritedAttributes, Map<Nonterminal, Set<Attribute<?>>> inheritedAttributesPerNonterminal, Map<Nonterminal, Set<Attribute<?>>> synthesizedAttributesPerNonterminal)
	{
		Map<Nonterminal, Set<Attribute<?>>> inheritedAttributesPerNonterminalModifiableSets = new HashMap<>();
		Map<Nonterminal, Set<Attribute<?>>> synthesizedAttributesPerNonterminalModifiableSets = new HashMap<>();
		for(Nonterminal nonterminal : attributeSystem.getGrammar().getAllNonterminals())
		{
			inheritedAttributesPerNonterminalModifiableSets.put(nonterminal, new HashSet<>());
			synthesizedAttributesPerNonterminalModifiableSets.put(nonterminal, new HashSet<>());
		}

		for(Set<AttributeEquation<?>> attrEqs : attributeSystem.getAttributeEquations().values())
			for(AttributeEquation<?> attrEq : attrEqs)
			{
				AttributeValueReference<?> returnValue = attrEq.getReturnValue();
				Attribute<?> targetAttribute = returnValue.getAttribute();
				Nonterminal targetNonterminal = returnValue.getTargetSymbol();
				(inheritedAttributes.contains(targetAttribute) ? inheritedAttributesPerNonterminalModifiableSets : synthesizedAttributesPerNonterminalModifiableSets)
						.get(targetNonterminal).add(targetAttribute);
				for(SymbolValueReference<?, ?, ?> param : attrEq.getParameters())
					switch(param.getType())
					{
						case ATTRIBUTE_VALUE:
							AttributeValueReference<?> paramCasted = (AttributeValueReference<?>) param;
							Attribute<?> paramTargetAttribute = paramCasted.getAttribute();
							Nonterminal paramTargetNonterminal = paramCasted.getTargetSymbol();
							(inheritedAttributes.contains(paramTargetAttribute) ? inheritedAttributesPerNonterminalModifiableSets : synthesizedAttributesPerNonterminalModifiableSets)
									.get(paramTargetNonterminal).add(paramTargetAttribute);
							break;
						case TERMINAL_VALUE:
							break;
						default:
							throw new IllegalStateException("Unknown enum constant: " + param.getType());
					}
			}

		inheritedAttributesPerNonterminalModifiableSets.forEach((k, v) -> inheritedAttributesPerNonterminal.put(k, Collections.unmodifiableSet(v)));
		synthesizedAttributesPerNonterminalModifiableSets.forEach((k, v) -> synthesizedAttributesPerNonterminal.put(k, Collections.unmodifiableSet(v)));
	}

	private static Map<Production, Set<AttributeEquation<?>>> splitAttributeEquations(AttributeSystem attributeSystem, Map<Production, Set<AttributeEquation<?>>> inheritedAttributeEquationsUnsorted)
	{
		Map<Production, Set<AttributeEquation<?>>> result = new HashMap<>();
		for(Entry<Production, Set<AttributeEquation<?>>> e : attributeSystem.getAttributeEquations().entrySet())
		{
			Set<AttributeEquation<?>> inheritedEquations = new HashSet<>();
			Set<AttributeEquation<?>> synthesizedEquations = new HashSet<>();

			for(AttributeEquation<?> attrEq : e.getValue())
				(attrEq.getReturnValue().isRoot() ? synthesizedEquations : inheritedEquations).add(attrEq);

			if(!synthesizedEquations.isEmpty())
				result.put(e.getKey(), Collections.unmodifiableSet(synthesizedEquations));
			if(!inheritedEquations.isEmpty())
				inheritedAttributeEquationsUnsorted.put(e.getKey(), inheritedEquations);
		}
		return Collections.unmodifiableMap(result);
	}
	private static Set<Attribute<?>> extractAttributesFromInherited(Map<Production, List<Set<AttributeEquation<?>>>> attributeEquationsPerProduction)
	{
		return attributeEquationsPerProduction
				.values()
				.stream()
				.flatMap(List::stream)
				.flatMap(Set::stream)
				.map(AttributeEquation::getReturnValue)
				.map(AttributeValueReference::getAttribute)
				.collect(Collectors.toUnmodifiableSet());
	}
	private static Map<Production, List<Set<AttributeEquation<?>>>> sortInheritedAttributeEquations(Set<Attribute<?>> synthesizedAttributes, Map<Production, Set<AttributeEquation<?>>> inheritedAttributeEquationsUnsorted)
	{
		Map<Production, List<Set<AttributeEquation<?>>>> result = new HashMap<>();
		for(Entry<Production, Set<AttributeEquation<?>>> e : inheritedAttributeEquationsUnsorted.entrySet())
		{
			List<Set<AttributeEquation<?>>> inheritedEquations = new ArrayList<>();
			for(int i = 0; i < e.getKey().getRhs().getSymbols().size(); i ++)
				inheritedEquations.add(new HashSet<>());

			for(AttributeEquation<?> attrEq : e.getValue())
			{
				int returnPos = attrEq.getReturnValue().getPosition();
				for(SymbolValueReference<?, ?, ?> param : attrEq.getParameters())
					if(param.getPosition() >= returnPos)
						throw new IllegalArgumentException("The equation at " + attrEq.getRootProduction() + " for the inherited attribute " + attrEq.getReturnValue().getAttribute() + " of child #" + param.getPosition() + " depends on attributes of child nodes after child #" + param.getPosition());

				inheritedEquations.get(returnPos).add(attrEq);
			}

			result.put(e.getKey(), inheritedEquations.stream().map(Collections::unmodifiableSet).collect(Collectors.toUnmodifiableList()));
		}
		return Collections.unmodifiableMap(result);
	}
	private static Set<Attribute<?>> extractAttributesFromSynthesized(Map<Production, Set<AttributeEquation<?>>> attributeEquationsPerProduction)
	{
		return attributeEquationsPerProduction
				.values()
				.stream()
				.flatMap(Set::stream)
				.map(AttributeEquation::getReturnValue)
				.map(AttributeValueReference::getAttribute)
				.collect(Collectors.toUnmodifiableSet());
	}

	private void checkLAttributeSystem()
	{
		if(!Collections.disjoint(inheritedAttributes, synthesizedAttributes))
		{
			Set<Attribute<?>> inheritedAndSynthesized = new HashSet<>(inheritedAttributes);
			inheritedAndSynthesized.retainAll(synthesizedAttributes);
			throw new IllegalArgumentException("The following attributes are neither inherited nor synthesized: " + inheritedAndSynthesized);
		}
		Set<Attribute<?>> missingAttributes = new HashSet<>(attributeSystem.getAllAttributes());
		missingAttributes.removeAll(inheritedAttributes);
		missingAttributes.removeAll(synthesizedAttributes);
		if(!missingAttributes.isEmpty())
			throw new IllegalArgumentException("The following attributes have no attribute equation: " + missingAttributes);

		for(Set<AttributeEquation<?>> attrEqs : synthesizedAttributeEquations.values())
			for(AttributeEquation<?> attrEq : attrEqs)
				for(SymbolValueReference<?, ?, ?> param : attrEq.getParameters())
					if(param.isRoot() && synthesizedAttributes.contains(((AttributeValueReference<?>) param).getAttribute()))
						throw new IllegalArgumentException("The equation at " + attrEq.getRootProduction() + " for the synthesized attribute " + attrEq.getReturnValue().getAttribute() + " depends on synthesized attributes of the root node");


		for(Production production : attributeSystem.getGrammar().getProductions())
		{
			Set<Attribute<?>> calculatedSynthesizedAttributes = synthesizedAttributeEquations
					.getOrDefault(production, Set.of())
					.stream()
					.map(AttributeEquation::getReturnValue)
					.map(AttributeValueReference::getAttribute)
					.collect(Collectors.toSet());
			if(!calculatedSynthesizedAttributes.equals(synthesizedAttributesPerNonterminal.get(production.getLhs())))
			{
				Set<Attribute<?>> missing = new HashSet<>(synthesizedAttributesPerNonterminal.get(production.getLhs()));
				missing.removeAll(calculatedSynthesizedAttributes);
				throw new IllegalArgumentException("The following synthesized attributes are not calculated in " + production + ": " + missing);
			}

			List<Symbol> rhsSymbols = production.getRhs().getSymbols();
			int rhsSize = rhsSymbols.size();
			for(int i = 0; i < rhsSize; i ++)
			{
				Symbol symbol = rhsSymbols.get(i);
				switch(symbol.getType())
				{
					case NONTERMINAL:
						Set<Attribute<?>> calculatedInheritedAttributes = inheritedAttributeEquations
								.getOrDefault(production, nEmptySets(rhsSize))
								.get(i)
								.stream()
								.map(AttributeEquation::getReturnValue)
								.map(AttributeValueReference::getAttribute)
								.collect(Collectors.toSet());
						if(!calculatedInheritedAttributes.equals(inheritedAttributesPerNonterminal.get(symbol)))
						{
							Set<Attribute<?>> missing = new HashSet<>(inheritedAttributesPerNonterminal.get(symbol));
							missing.removeAll(calculatedInheritedAttributes);
							throw new IllegalArgumentException("The following inherited attributes for child #" + i + " are not calculated in " + production + ": " + missing);
						}
						break;
					case TERMINAL:
						break;
					default:
						throw new IllegalArgumentException("Unknown enum constant: " + symbol.getType());
				}
			}
		}
	}

	public void evaluate(InnerNode root, Set<AttributeValue<?>> rootInheritedValues)
	{
		checkRootInheritedValues(root.getSymbol(), rootInheritedValues);
		for(AttributeValue<?> rootInheritedValue : rootInheritedValues)
			setRootInheritedValue(root, rootInheritedValue);
		recurseInnerNode(root);
	}

	private void checkRootInheritedValues(Nonterminal root, Set<AttributeValue<?>> rootInheritedValues)
	{
		Set<Attribute<?>> inheritedNonterminalsOfRoot = inheritedAttributesPerNonterminal.get(root);
		Set<Attribute<?>> suppliedValues = rootInheritedValues.stream().map(AttributeValue::getAttribute).collect(Collectors.toSet());
		if(!inheritedNonterminalsOfRoot.equals(suppliedValues))
		{
			Set<Attribute<?>> missing = new HashSet<>(inheritedNonterminalsOfRoot);
			missing.removeAll(suppliedValues);
			if(!missing.isEmpty())
				throw new IllegalArgumentException("No values were supplied for the following inherited attributes of root: " + missing);
			suppliedValues = new HashSet<>(suppliedValues);
			suppliedValues.removeAll(inheritedNonterminalsOfRoot);
			throw new IllegalArgumentException("Values were supplied for the following synthesized or undefined attributes of root: " + suppliedValues);
		}
	}

	private void recurseSubtree(ASTNode<?> root)
	{
		switch(root.getType())
		{
			case TOKEN:
				return;
			case INNER_NODE:
				recurseInnerNode((InnerNode) root);
				break;
			default:
				throw new IllegalArgumentException("Unknown enum constant: " + root.getType());
		}
	}

	private void recurseInnerNode(InnerNode root)
	{
		List<ASTNode<?>> children = root.getChildren();
		Production production = root.getProduction();
		int rhsSize = production.getRhs().getSymbols().size();
		List<Set<AttributeEquation<?>>> inheritedAttributeEquationsThisNode = inheritedAttributeEquations.getOrDefault(production, nEmptySets(rhsSize));
		for(int i = 0; i < rhsSize; i ++)
		{
			inheritedAttributeEquationsThisNode.get(i).forEach(eq -> eq.evaluateAndSaveResult(root));
			recurseSubtree(children.get(i));
		}
		synthesizedAttributeEquations.getOrDefault(production, Set.of()).forEach(eq -> eq.evaluateAndSaveResult(root));
	}

	private List<Set<AttributeEquation<?>>> nEmptySets(int n)
	{
		return Collections.nCopies(n, Set.of());
	}

	private <V> void setRootInheritedValue(InnerNode root, AttributeValue<V> rootInheritedValue)
	{
		if(!inheritedAttributes.contains(rootInheritedValue.getAttribute()))
			throw new IllegalArgumentException("The attribute " + rootInheritedValue.getAttribute() + " is not inherited");
		root.setAttributeValue(rootInheritedValue);
	}
}
