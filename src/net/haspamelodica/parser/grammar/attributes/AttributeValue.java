package net.haspamelodica.parser.grammar.attributes;

public class AttributeValue<V>
{
	private final Attribute<V>	attribute;
	private final V				value;

	public AttributeValue(Attribute<V> attribute, V value)
	{
		this.attribute = attribute;
		this.value = value;
	}

	public Attribute<V> getAttribute()
	{
		return attribute;
	}

	public V getValue()
	{
		return value;
	}
}
