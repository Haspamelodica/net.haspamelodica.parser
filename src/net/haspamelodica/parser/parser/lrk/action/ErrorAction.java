package net.haspamelodica.parser.parser.lrk.action;

public class ErrorAction implements Action
{
	public static final ErrorAction INSTANCE = new ErrorAction();

	private ErrorAction()
	{}

	@Override
	public ActionType getType()
	{
		return ActionType.ERROR;
	}
}
