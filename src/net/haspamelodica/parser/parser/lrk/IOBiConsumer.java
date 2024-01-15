package net.haspamelodica.parser.parser.lrk;

import java.io.IOException;

public interface IOBiConsumer<T, U>
{
	public void accept(T t, U u) throws IOException;
}
