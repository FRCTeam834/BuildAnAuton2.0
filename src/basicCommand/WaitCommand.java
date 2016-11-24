package basicCommand;

import visualrobot.Command;
import visualrobot.EditableCommand;
import visualrobot.VisualRobot;

public class WaitCommand implements EditableCommand{
	private static final long serialVersionUID = -7715889005108327668L;
	Condition<?> condition;
	VisualRobot robot;
	
	public void execute() throws NullPointerException {
		while(condition.check() && robot.isAutonomous()) {
			
		}
	}

	public void setRobot(VisualRobot r) {
		condition.setRobot(r);
		robot = r;
	}
	
	public WaitCommand() {

	}
	
	public WaitCommand(Condition<?> cond) {
		condition = cond;

		
	}

	@Override
	public String[] getVals() {
		if(condition!=null)
			return condition.getVals();
		else 
			return null;
	}

}
