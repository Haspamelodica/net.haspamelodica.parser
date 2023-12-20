package net.haspamelodica.parser.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionsUtils
{
	private CollectionsUtils()
	{}

	public static <E> List<E> concat(List<E> a, List<E> b)
	{
		List<E> result = new ArrayList<>(a);
		result.addAll(b);
		return Collections.unmodifiableList(result);
	}
	@SafeVarargs
	public static <E> Set<E> union(Set<E>... sets)
	{
		Set<E> result = new HashSet<>();
		for(Set<E> set : sets)
			result.addAll(set);
		return Collections.unmodifiableSet(result);
	}
	public static <E> List<E> pseudoAdd(List<E> oldList, E appended)
	{
		List<E> newList = new ArrayList<>(oldList);
		newList.add(appended);
		return Collections.unmodifiableList(newList);
	}
	public static <E> Set<E> pseudoAdd(Set<E> oldSet, E appended)
	{
		if(oldSet.contains(appended))
			return oldSet;
		Set<E> newSet = new HashSet<>(oldSet);
		newSet.add(appended);
		return Collections.unmodifiableSet(newSet);
	}
}
