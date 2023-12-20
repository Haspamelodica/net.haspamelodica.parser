package net.haspamelodica.parser.parser.lrk;

import java.util.List;

import net.haspamelodica.parser.grammar.Production;
import net.haspamelodica.parser.grammar.Symbol;

public class Item
{
	private final Production	production;
	private final int			markerPosition;

	private final boolean	isFinished;
	private final Symbol	nextSymbol;

	public Item(Production production, int markerPosition)
	{
		List<Symbol> rhsSymbols = production.getRhs().getSymbols();
		if(markerPosition < 0 || markerPosition > rhsSymbols.size())
			throw new IllegalArgumentException("Marker out of range");
		this.production = production;
		this.markerPosition = markerPosition;

		this.isFinished = markerPosition == rhsSymbols.size();
		this.nextSymbol = isFinished ? null : rhsSymbols.get(markerPosition);
	}

	public Production getProduction()
	{
		return production;
	}
	public int getMarkerPosition()
	{
		return markerPosition;
	}
	public boolean isFinished()
	{
		return isFinished;
	}
	public Symbol getNextSymbol()
	{
		return nextSymbol;
	}

	public Item advanceMarker()
	{
		if(isFinished)
			throw new IllegalStateException("This item is already finished");
		return new Item(production, markerPosition + 1);
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append("[");
		result.append(production.getLhs());
		result.append(" -> ");
		List<Symbol> rhsSymbols = production.getRhs().getSymbols();
		for(int i = 0; i < rhsSymbols.size(); i ++)
		{
			if(i > 0)
				result.append(' ');
			if(markerPosition == i)
				result.append(".");//TODO find a better symbol
			result.append(rhsSymbols.get(i));
		}
		if(markerPosition == rhsSymbols.size())
			result.append(" .");//TODO find a better symbol
		result.append("]");
		return result.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + markerPosition;
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
		Item other = (Item) obj;
		if(markerPosition != other.markerPosition)
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
