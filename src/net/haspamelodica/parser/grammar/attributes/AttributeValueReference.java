package net.haspamelodica.parser.grammar.attributes;

import net.haspamelodica.parser.ast.InnerNode;
import net.haspamelodica.parser.grammar.Nonterminal;
import net.haspamelodica.parser.grammar.Production;
import net.haspamelodica.parser.grammar.Symbol.SymbolType;

public class AttributeValueReference<V> extends SymbolValueReference<V, Nonterminal, InnerNode>
{
	private final Attribute<V> attribute;

	public AttributeValueReference(Production rootProduction, int position, Attribute<V> attribute)
	{
		super(NeighboringValuePositionType.ATTRIBUTE_VALUE, rootProduction, position);
		this.attribute = attribute;
		if(getParameterSymbol(rootProduction, position).getType() != SymbolType.NONTERMINAL)
			throw new IllegalArgumentException("Symbol #" + position + " in " + rootProduction + " is not a nonterminal");
	}

	public Attribute<V> getAttribute()
	{
		return attribute;
	}

	@Override
	public V getValueForNode(InnerNode node)
	{
		return node.getValueForAttribute(attribute);
	}
	@Override
	public void setValueForNode(InnerNode node, V value)
	{
		node.setAttributeValue(attribute, value);
	}

	@Override
	public String toString()
	{
		return attribute + "[" + getPosition() + "]";
	}
}
