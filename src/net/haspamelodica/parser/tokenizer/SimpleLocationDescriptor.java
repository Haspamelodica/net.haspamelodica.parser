package net.haspamelodica.parser.tokenizer;

public class SimpleLocationDescriptor
{
	private int		line;
	private int		charInLine;
	private boolean	lastCharWasCR;

	public SimpleLocationDescriptor()
	{
		this.line = 1;
		this.charInLine = 1;
	}

	public void advance(Char readChar)
	{
		if(readChar != null)
			if(readChar.equalsChar('\r'))
			{
				line ++;
				charInLine = 1;
				lastCharWasCR = true;
			} else if(readChar.equalsChar('\n'))
			{
				if(!lastCharWasCR)
				{
					line ++;
					charInLine = 1;
				}
				lastCharWasCR = false;
			} else
			{
				charInLine ++;
				lastCharWasCR = false;
			}
	}

	public String getLocationDescription()
	{
		return "line " + line + "|" + charInLine;
	}
}
