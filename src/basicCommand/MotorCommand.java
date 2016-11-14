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
		System.out.println(name + " " + (robot == null) + " " +( motor == null));

		motor.set(speed);
	}

	public MotorCommand() {
		
	}
	
	public MotorCommand(String name, Double s) {
		speed = s;
		if(speed > 1) {
			speed = 1;
		}
		if(speed < -1) {
			speed = -1;
		}

		this.name = name;
	}

	public void setRobot(VisualRobot r) {
		robot = r;
		motor = robot.getMotors().get(name);
	}

	public String[] getVals() {
		return new String[] {name, Double.toString(speed)};
	}
}
