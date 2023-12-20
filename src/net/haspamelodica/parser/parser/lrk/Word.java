package net.haspamelodica.parser.parser.lrk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.haspamelodica.parser.grammar.Terminal;

public class Word
{
	public static final Word EPSILON = new Word();

	private final List<Terminal<?>> terminals;

	private final int length;

	public Word(Terminal<?>... terminals)
	{
		this(List.of(terminals));
	}
	public Word(List<Terminal<?>> terminals)
	{
		this.terminals = List.copyOf(terminals);
		this.length = this.terminals.size();
	}
	private Word(List<Terminal<?>> terminals, int length)
	{
		this.terminals = terminals;
		this.length = length;
	}

	public List<Terminal<?>> getTerminals()
	{
		return terminals;
	}
	public int getLength()
	{
		return length;
	}

	public static Word repeat(Terminal<?> t, int n)
	{
		return new Word(Collections.nCopies(n, t), n);
	}
	public Word prefix(int length)
	{
		return new Word(terminals.subList(0, length), length);
	}
	public Word suffix(int length)
	{
		return new Word(terminals.subList(this.length - length, this.length), length);
	}
	public Word infix(int startIndex, int endIndex)
	{
		return new Word(terminals.subList(startIndex, endIndex), endIndex - startIndex);
	}
	public Word concat(Word suffix)
	{
		List<Terminal<?>> concatList = new ArrayList<>(terminals);
		concatList.addAll(suffix.terminals);
		return new Word(Collections.unmodifiableList(concatList), length + suffix.length);
	}
	public Word append(Terminal<?> suffix)
	{
		List<Terminal<?>> concatList = new ArrayList<>(terminals);
		concatList.add(suffix);
		return new Word(Collections.unmodifiableList(concatList), length + 1);
	}

	@Override
	public String toString()
	{
		return terminals.stream().map(Terminal::toString).collect(Collectors.joining());
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + length;
		result = prime * result + ((terminals == null) ? 0 : terminals.hashCode());
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
		Word other = (Word) obj;
		if(length != other.length)
			return false;
		if(terminals == null)
		{
			if(other.terminals != null)
				return false;
		} else if(!terminals.equals(other.terminals))
			return false;
		return true;
	}
}
