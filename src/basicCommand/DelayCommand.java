 package basicCommand;


import edu.wpi.first.wpilibj.Timer;
import visualrobot.Command;
import visualrobot.VisualRobot;

public class DelayCommand implements Command {	
	private static final long serialVersionUID = 8254171449712480757L;
	private double time;

	public void execute() throws NullPointerException {
		Timer.delay(time);
	}

	public void setRobot(VisualRobot r) {
	}
	
	public void set(double t) {
		
	}
	
	public DelayCommand() {
		
	}
	
	/**
	 * 
	 * @param dir The direction in which to move.
	 * @param r The robot.
	 */
	public DelayCommand(double time) {
		this.time = time;
	}
}
