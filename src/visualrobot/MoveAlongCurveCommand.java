package visualrobot;

import edu.wpi.first.wpilibj.GyroBase;

public class MoveAlongCurveCommand implements Command {
	private static final long serialVersionUID = 6241191079543041229L;

	private VisualRobot robot;
	private double radius, speed, angle;
	private final double WIDTH = 25.0;
	private GyroBase gyro;

	public void execute() throws NullPointerException {
		gyro.reset();
		System.out.println("Running");
		if(angle < 0)
			while(gyro.getAngle() > angle && robot.isAutonomous())
			{
				robot.setLeftSide(speed * (radius -WIDTH/2)/radius);
				robot.setRightSide(speed * (radius + WIDTH/2)/radius);
			}
		else if(angle > 0)
			while(gyro.getAngle() < angle && robot.isAutonomous())
			{
				robot.setLeftSide(speed * (radius + WIDTH/2)/radius);
				robot.setRightSide(speed * (radius -WIDTH/2)/radius);
			}
		
		robot.setLeftSide(0.0);
		robot.setRightSide(0.0);
		System.out.println("Done Running");

	}


	public void setRobot(VisualRobot r) {
		robot = (VisualRobot)r;
		gyro = (GyroBase)robot.getSensors().get("gyro");
	}
	
	public MoveAlongCurveCommand() {
	}

	/**
	 * 
	 * @param dir The direction in which to move.
	 * @param rad The radius of the robot.
	 * @param s The speed at which to move.
	 * @param ang The angle to move to.
	 * @param r The robot.
	 */
	public MoveAlongCurveCommand(double rad, double s, double ang, VisualRobot r) {
		radius = rad;
		speed = s;
		angle = ang;
		if(r!=null) setRobot(r);
		
		if(speed * (radius + WIDTH/2)/radius >= 1.0 ) {
			speed = 1/(radius + WIDTH/2)/radius;
		}

	}
}
