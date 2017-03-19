package basicCommand;

import edu.wpi.first.wpilibj.Servo;

import visualrobot.Command;
import visualrobot.EditableCommand;
import visualrobot.VisualRobot;

public class ServoCommand implements EditableCommand{
	
	private static final long serialVersionUID = -8508288502233986757L;
	
	private VisualRobot robot;
	private Servo servo;
	private double angle;
	private String name;
	
	public void execute() throws NullPointerException {
		System.out.println(name + " " + (robot == null) + " " +( servo == null));

		servo.setAngle(angle);
	}

	public ServoCommand() {
		
	}
	
	public ServoCommand(String name, Double angle) {
		this.name = name;
		this.angle = angle;
	}

	public void setRobot(VisualRobot r) {
		robot = r;
		servo = robot.getServos().get(name);
	}

	public String[] getVals() {
		return new String[] {name, Double.toString(angle)};
	}
}
