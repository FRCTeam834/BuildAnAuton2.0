package basicCommand;

import edu.wpi.first.wpilibj.SpeedController;

import visualrobot.Command;
import visualrobot.VisualRobot;

public class MotorCommand implements Command{
	private static final long serialVersionUID = -3786831076020284885L;
	
	private VisualRobot robot;
	private SpeedController motor;
	private double speed;
	private String name;
	
	public void execute() throws NullPointerException {
		motor.set(speed);
	}

	public MotorCommand() {
		
	}
	
	public MotorCommand(String name, double speed) {
		this.speed = speed;
		this.name = name;
	}

	public void setRobot(VisualRobot r) {
		robot = r;
		motor = r.getMotors().get(name);
	}
}