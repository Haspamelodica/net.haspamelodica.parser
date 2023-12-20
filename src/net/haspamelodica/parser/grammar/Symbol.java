package net.haspamelodica.parser.grammar;

public interface Symbol
{
	public SymbolType getType();

	public enum SymbolType
	{
		TERMINAL,
		NONTERMINAL;
	}
}
