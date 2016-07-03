package basicCommand;

import visualrobot.Command;
import visualrobot.VisualRobot;

public class WaitCommand implements Command{
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

	public String[] getVals() {
		if(condition!=null)
			return condition.getVals();
		else 
			return null;
	}

}
