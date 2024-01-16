package net.haspamelodica.parser.caching;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public class DigestingInputStream extends InputStream
{
	private final InputStream	in;
	private final MessageDigest	digest;

	public DigestingInputStream(InputStream in, MessageDigest digest)
	{
		this.in = in;
		this.digest = digest;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int n = in.read(b, off, len);
		if(n > 0)
			digest.update(b, off, n);
		return n;
	}

	@Override
	public int read() throws IOException
	{
		int b = in.read();
		if(b >= 0)
			digest.update((byte) b);
		return b;
	}

	public byte[] digest()
	{
		return digest.digest();
	}
}
