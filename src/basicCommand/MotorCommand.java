package basicCommand;

import edu.wpi.first.wpilibj.SpeedController;
import visualrobot.Command;
import visualrobot.VisualRobot;

public class MotorCommand implements Command{

	private SpeedController motor;
	private VisualRobot robot;
	private double speed;
	private String name;
	
	public void execute() throws NullPointerException {
		motor.set(speed);
	}

	public MotorCommand(String n, double s) {
		speed = s;
		name = n;
	}

	public void setRobot(VisualRobot r) {
		robot = r;
		motor = r.getMotors().get(name);
	}
}
