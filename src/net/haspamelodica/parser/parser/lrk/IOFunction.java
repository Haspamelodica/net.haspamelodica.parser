package net.haspamelodica.parser.parser.lrk;

import java.io.IOException;

public interface IOFunction<T, R>
{
	public R apply(T t) throws IOException;
}
