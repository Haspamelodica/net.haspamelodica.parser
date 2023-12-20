package net.haspamelodica.parser.ast;

import net.haspamelodica.parser.grammar.Symbol;

public interface ASTNode<S extends Symbol>
{
	public ASTNodeType getType();

	public S getSymbol();

	public enum ASTNodeType
	{
		INNER_NODE,
		TOKEN;
	}

	public void append(StringBuilder result, int tabs);
}
