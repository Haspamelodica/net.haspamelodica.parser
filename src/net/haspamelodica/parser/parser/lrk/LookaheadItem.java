package net.haspamelodica.parser.parser.lrk;

import java.util.Set;

public class LookaheadItem
{
	private final Item		item;
	private final Set<Word>	admissibleLookaheads;

	private final int lookaheadSize;

	public LookaheadItem(Item item, Set<Word> admissibleLookaheads)
	{
		this.item = item;

		this.admissibleLookaheads = Set.copyOf(admissibleLookaheads);

		if(this.admissibleLookaheads.size() == 0)
			throw new IllegalArgumentException("Lookahead items only make sense when admissible with at least one lookahead");
		this.lookaheadSize = this.admissibleLookaheads.stream().findAny().get().getLength();
		for(Word l : admissibleLookaheads)
			if(l.getLength() != lookaheadSize)
				throw new IllegalArgumentException("Not all lookaheads have the same length");
	}

	public Item getItem()
	{
		return item;
	}
	public Set<Word> getAdmissibleLookaheads()
	{
		return admissibleLookaheads;
	}
	public int getLookaheadSize()
	{
		return lookaheadSize;
	}

	public LookaheadItem advanceMarker()
	{
		return new LookaheadItem(item.advanceMarker(), admissibleLookaheads);
	}

	@Override
	public String toString()
	{
		return item + ": " + admissibleLookaheads;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((admissibleLookaheads == null) ? 0 : admissibleLookaheads.hashCode());
		result = prime * result + ((item == null) ? 0 : item.hashCode());
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
		LookaheadItem other = (LookaheadItem) obj;
		if(admissibleLookaheads == null)
		{
			if(other.admissibleLookaheads != null)
				return false;
		} else if(!admissibleLookaheads.equals(other.admissibleLookaheads))
			return false;
		if(item == null)
		{
			if(other.item != null)
				return false;
		} else if(!item.equals(other.item))
			return false;
		if(lookaheadSize != other.lookaheadSize)
			return false;
		return true;
	}
}
