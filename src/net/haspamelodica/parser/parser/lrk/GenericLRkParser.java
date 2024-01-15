package net.haspamelodica.parser.parser.lrk;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.haspamelodica.parser.ast.InnerNode;
import net.haspamelodica.parser.grammar.Symbol;
import net.haspamelodica.parser.parser.ParseException;
import net.haspamelodica.parser.parser.Parser;
import net.haspamelodica.parser.parser.lrk.action.Action;
import net.haspamelodica.parser.tokenizer.TokenStream;

public class GenericLRkParser<STATE> implements Parser
{
	private final STATE								initialState;
	private final Map<STATE, Map<Symbol, STATE>>	gotoTable;
	private final Map<STATE, Map<Word, Action>>		actionTable;
	private final int								lookaheadSize;

	public GenericLRkParser(STATE initialState, Map<STATE, Map<Symbol, STATE>> gotoTable, Map<STATE, Map<Word, Action>> actionTable, int lookaheadSize)
	{
		this.initialState = initialState;
		this.gotoTable = deepCopy(gotoTable);
		this.actionTable = deepCopy(actionTable);
		this.lookaheadSize = lookaheadSize;
	}

	private <A, B, C> Map<A, Map<B, C>> deepCopy(Map<A, Map<B, C>> actionTable)
	{
		return actionTable.entrySet().stream().collect(Collectors.toUnmodifiableMap(Entry::getKey, e -> Map.copyOf(e.getValue())));
	}

	@Override
	public InnerNode parse(TokenStream tokens) throws ParseException
	{
		return new LRkParserExecution<>(this, tokens).parse();
	}

	public STATE getInitialState()
	{
		return initialState;
	}
	public Map<STATE, Map<Symbol, STATE>> getGotoTable()
	{
		return gotoTable;
	}
	public Map<STATE, Map<Word, Action>> getActionTable()
	{
		return actionTable;
	}
	public int getLookaheadSize()
	{
		return lookaheadSize;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actionTable == null) ? 0 : actionTable.hashCode());
		result = prime * result + ((gotoTable == null) ? 0 : gotoTable.hashCode());
		result = prime * result + ((initialState == null) ? 0 : initialState.hashCode());
		result = prime * result + lookaheadSize;
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
		GenericLRkParser<?> other = (GenericLRkParser<?>) obj;
		if(actionTable == null)
		{
			if(other.actionTable != null)
				return false;
		} else if(!actionTable.equals(other.actionTable))
			return false;
		if(gotoTable == null)
		{
			if(other.gotoTable != null)
				return false;
		} else if(!gotoTable.equals(other.gotoTable))
			return false;
		if(initialState == null)
		{
			if(other.initialState != null)
				return false;
		} else if(!initialState.equals(other.initialState))
			return false;
		if(lookaheadSize != other.lookaheadSize)
			return false;
		return true;
	}
}
