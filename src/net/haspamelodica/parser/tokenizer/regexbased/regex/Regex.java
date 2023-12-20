package net.haspamelodica.parser.tokenizer.regexbased.regex;

import java.util.Set;

public class Regex
{
	private final RegexNode root;

	public Regex(RegexNode root)
	{
		this.root = root;
		root.calculateSymbolIndices(0);
		root.calculateNextSets(Set.of());
	}

	public RegexNode getRoot()
	{
		return root;
	}

	@Override
	public String toString()
	{
		return root.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((root == null) ? 0 : root.hashCode());
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
		Regex other = (Regex) obj;
		if(root == null)
		{
			if(other.root != null)
				return false;
		} else if(!root.equals(other.root))
			return false;
		return true;
	}
}
