package net.haspamelodica.parser.parser.lrk.action;

public class ShiftAction implements Action
{
	public static final ShiftAction INSTANCE = new ShiftAction();

	private ShiftAction()
	{}

	@Override
	public ActionType getType()
	{
		return ActionType.SHIFT;
	}
}
