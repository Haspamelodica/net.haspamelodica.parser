package net.haspamelodica.parser.generics;

@FunctionalInterface
public interface HexFunction<S, T, U, V, W, X, R>
{
	R apply(S s, T t, U u, V v, W w, X x);
}
