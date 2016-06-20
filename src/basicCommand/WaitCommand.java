package basicCommand;

import visualrobot.Command;
import visualrobot.VisualRobot;

public class WaitCommand implements Command{
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
	
	public WaitCommand(Condition<?> cond) {
		condition = cond;
		
	}

}
