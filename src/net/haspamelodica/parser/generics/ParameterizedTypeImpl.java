package net.haspamelodica.parser.generics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ParameterizedTypeImpl implements ParameterizedType
{
	private final Type[]	typeArguments;
	private final Type		rawType;
	private final Type		ownerType;

	public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments)
	{
		this.typeArguments = typeArguments;
		this.rawType = rawType;
		this.ownerType = ownerType;
	}

	@Override
	public Type[] getActualTypeArguments()
	{
		return typeArguments;
	}

	@Override
	public Type getRawType()
	{
		return rawType;
	}

	@Override
	public Type getOwnerType()
	{
		return ownerType;
	}
}
