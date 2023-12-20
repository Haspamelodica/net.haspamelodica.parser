package net.haspamelodica.parser.grammar.parser;

import java.util.Map;

import net.haspamelodica.parser.grammar.ContextFreeGrammar;
import net.haspamelodica.parser.grammar.attributes.Attribute;
import net.haspamelodica.parser.grammar.attributes.AttributeSystem;

public class AttributeGrammarParseResult
{
	private final ContextFreeGrammar		grammar;
	private final AttributeSystem			attributeSystem;
	private final Map<String, Attribute<?>>	attributesByName;

	public AttributeGrammarParseResult(AttributeSystem attributeSystem, Map<String, Attribute<?>> attributesByName)
	{
		this.grammar = attributeSystem.getGrammar();
		this.attributeSystem = attributeSystem;
		this.attributesByName = attributesByName;
	}

	public ContextFreeGrammar getGrammar()
	{
		return grammar;
	}
	public AttributeSystem getAttributeSystem()
	{
		return attributeSystem;
	}
	public Map<String, Attribute<?>> getAttributesByName()
	{
		return attributesByName;
	}
}
