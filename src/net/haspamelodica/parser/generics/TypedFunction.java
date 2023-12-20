package net.haspamelodica.parser.generics;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface TypedFunction
{
	public Type getResultType();
	public List<Type> getParameterTypes();
	public Object execute(Object... args);

	public static TypedFunction constant(Object value)
	{
		@SuppressWarnings("rawtypes")
		Class clazz = value.getClass();
		@SuppressWarnings("unchecked")
		TypedFunction build = build(() -> value, clazz);
		return build;
	}
	public static <R> TypedFunction build(Supplier<R> function, Class<R> r)
	{
		return buildT(function, r);
	}
	public static <R> TypedFunction buildT(Supplier<R> function, Type r)
	{
		List<Type> parameterTypes = List.of();
		return new TypedFunction()
		{
			@Override
			public Type getResultType()
			{
				return r;
			}
			@Override
			public List<Type> getParameterTypes()
			{
				return parameterTypes;
			}

			@Override
			public Object execute(Object... args)
			{
				return function.get();
			}
		};
	}
	public static <T, R> TypedFunction build(Function<T, R> function, Class<R> r, Class<T> t)
	{
		return buildT(function, r, t);
	}
	public static <T, R> TypedFunction buildT(Function<T, R> function, Type r, Type t)
	{
		List<Type> parameterTypes = List.of(t);
		return new TypedFunction()
		{
			@Override
			public Type getResultType()
			{
				return r;
			}
			@Override
			public List<Type> getParameterTypes()
			{
				return parameterTypes;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object execute(Object... args)
			{
				return function.apply((T) args[0]);
			}
		};
	}
	public static <T, U, R> TypedFunction build(BiFunction<T, U, R> function, Class<R> r, Class<T> t, Class<U> u)
	{
		return buildT(function, r, t, u);
	}
	public static <T, U, R> TypedFunction buildT(BiFunction<T, U, R> function, Type r, Type t, Type u)
	{
		List<Type> parameterTypes = List.of(t, u);
		return new TypedFunction()
		{
			@Override
			public Type getResultType()
			{
				return r;
			}
			@Override
			public List<Type> getParameterTypes()
			{
				return parameterTypes;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object execute(Object... args)
			{
				return function.apply((T) args[0], (U) args[1]);
			}
		};
	}
	public static <T, U, V, R> TypedFunction build(TriFunction<T, U, V, R> function, Class<R> r, Class<T> t, Class<U> u, Class<V> v)
	{
		return buildT(function, r, t, u, v);
	}
	public static <T, U, V, R> TypedFunction buildT(TriFunction<T, U, V, R> function, Type r, Type t, Type u, Type v)
	{
		List<Type> parameterTypes = List.of(t, u, v);
		return new TypedFunction()
		{
			@Override
			public Type getResultType()
			{
				return r;
			}
			@Override
			public List<Type> getParameterTypes()
			{
				return parameterTypes;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object execute(Object... args)
			{
				return function.apply((T) args[0], (U) args[1], (V) args[2]);
			}
		};
	}
	public static <T, U, V, W, R> TypedFunction build(QuadFunction<T, U, V, W, R> function, Class<R> r, Class<T> t, Class<U> u, Class<V> v, Class<W> w)
	{
		return buildT(function, r, t, u, v, w);
	}
	public static <T, U, V, W, R> TypedFunction buildT(QuadFunction<T, U, V, W, R> function, Type r, Type t, Type u, Type v, Type w)
	{
		List<Type> parameterTypes = List.of(t, u, v, w);
		return new TypedFunction()
		{
			@Override
			public Type getResultType()
			{
				return r;
			}
			@Override
			public List<Type> getParameterTypes()
			{
				return parameterTypes;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object execute(Object... args)
			{
				return function.apply((T) args[0], (U) args[1], (V) args[2], (W) args[3]);
			}
		};
	}
	public static <T, U, V, W, X, R> TypedFunction build(QuinFunction<T, U, V, W, X, R> function, Class<R> r, Class<T> t, Class<U> u, Class<V> v, Class<W> w, Class<X> x)
	{
		return buildT(function, r, t, u, v, w, x);
	}
	public static <T, U, V, W, X, R> TypedFunction buildT(QuinFunction<T, U, V, W, X, R> function, Type r, Type t, Type u, Type v, Type w, Type x)
	{
		List<Type> parameterTypes = List.of(t, u, v, w, x);
		return new TypedFunction()
		{
			@Override
			public Type getResultType()
			{
				return r;
			}
			@Override
			public List<Type> getParameterTypes()
			{
				return parameterTypes;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object execute(Object... args)
			{
				return function.apply((T) args[0], (U) args[1], (V) args[2], (W) args[3], (X) args[4]);
			}
		};
	}
	public static <S, T, U, V, W, X, R> TypedFunction build(HexFunction<S, T, U, V, W, X, R> function, Class<S> s, Class<R> r, Class<T> t, Class<U> u, Class<V> v, Class<W> w, Class<X> x)
	{
		return buildT(function, s, r, t, u, v, w, x);
	}
	public static <S, T, U, V, W, X, R> TypedFunction buildT(HexFunction<S, T, U, V, W, X, R> function, Type s, Type r, Type t, Type u, Type v, Type w, Type x)
	{
		List<Type> parameterTypes = List.of(s, t, u, v, w, x);
		return new TypedFunction()
		{
			@Override
			public Type getResultType()
			{
				return r;
			}
			@Override
			public List<Type> getParameterTypes()
			{
				return parameterTypes;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object execute(Object... args)
			{
				return function.apply((S) args[0], (T) args[1], (U) args[2], (V) args[3], (W) args[4], (X) args[5]);
			}
		};
	}
}
