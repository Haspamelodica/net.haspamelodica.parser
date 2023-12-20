package net.haspamelodica.parser.ast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.haspamelodica.parser.grammar.Nonterminal;
import net.haspamelodica.parser.grammar.Production;
import net.haspamelodica.parser.grammar.attributes.Attribute;
import net.haspamelodica.parser.grammar.attributes.AttributeValue;

public class InnerNode implements ASTNode<Nonterminal>
{
	private final Production		production;
	private final List<ASTNode<?>>	children;

	private final Map<Attribute<?>, AttributeValue<?>> attributeValues;

	public InnerNode(Production production, List<ASTNode<?>> children)
	{
		this.production = production;
		this.children = List.copyOf(children);

		this.attributeValues = new HashMap<>();
	}

	public Production getProduction()
	{
		return production;
	}
	public List<ASTNode<?>> getChildren()
	{
		return children;
	}

	public <V> void setAttributeValue(Attribute<V> attribute, V value)
	{
		setAttributeValue(new AttributeValue<>(attribute, value));
	}

	public <V> void setAttributeValue(AttributeValue<V> value)
	{
		Attribute<V> attribute = value.getAttribute();
		if(attributeValues.containsKey(attribute))
			throw new IllegalStateException("This node already has a value for " + attribute);
		attributeValues.put(attribute, value);
	}
	@SuppressWarnings("unchecked")
	public <V> AttributeValue<V> getAttributeValue(Attribute<V> attribute)
	{
		if(!attributeValues.containsKey(attribute))
			throw new IllegalStateException("This node has no value for " + attribute);
		return (AttributeValue<V>) attributeValues.get(attribute);
	}
	public <V> V getValueForAttribute(Attribute<V> attribute)
	{
		return getAttributeValue(attribute).getValue();
	}

	@Override
	public Nonterminal getSymbol()
	{
		return production.getLhs();
	}

	@Override
	public ASTNodeType getType()
	{
		return ASTNodeType.INNER_NODE;
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		append(result, 0);
		return result.toString();
	}

	@Override
	public void append(StringBuilder result, int tabs)
	{
		result.append(" ".repeat(tabs));
		result.append(production);
		result.append(":");
		result.append(System.lineSeparator());
		for(ASTNode<?> child : children)
			child.append(result, tabs + 1);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((production == null) ? 0 : production.hashCode());
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
		InnerNode other = (InnerNode) obj;
		if(children == null)
		{
			if(other.children != null)
				return false;
		} else if(!children.equals(other.children))
			return false;
		if(production == null)
		{
			if(other.production != null)
				return false;
		} else if(!production.equals(other.production))
			return false;
		return true;
	}
}
