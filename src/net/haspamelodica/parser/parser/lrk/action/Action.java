package net.haspamelodica.parser.parser.lrk.action;

public interface Action
{
	public ActionType getType();

	public enum ActionType
	{
		SHIFT,
		REDUCE,
		FINISH,
		ERROR;
	}
}
