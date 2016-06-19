package visualrobot;

import edu.wpi.first.wpilibj.GyroBase;

public class MoveAlongCurveCommand implements Command {
	private static final long serialVersionUID = 6241191079543041229L;

	private VisualRobot robot;
	private double radius, speed, angle;
	private final double WIDTH = 28.0;
	private GyroBase gyro;

	public void execute() throws NullPointerException {
		gyro.reset();
		
		double fastSpeed = speed * (radius + WIDTH/2)/radius;
		double slowSpeed = speed * (radius - WIDTH/2)/radius;

		while(condition() && robot.isAutonomous())
		{
			if((angle > 0 && speed > 0) || (angle < 0 && speed < 0)) {
				robot.setLeftSide(fastSpeed);
				robot.setRightSide(slowSpeed);
			}
			else if((angle > 0 && speed < 0) || (angle < 0 && speed > 0)) {
				robot.setRightSide(fastSpeed);
				robot.setLeftSide(slowSpeed);
			}
			
		}		
		
		robot.setLeftSide(0.0);
		robot.setRightSide(0.0);

	}


	public void setRobot(VisualRobot r) {
		robot = (VisualRobot)r;
		gyro = (GyroBase)robot.getSensors().get("gyro");
	}
	

	/**
	 * 
	 * @param rad The radius of the robot movement.
	 * @param s The speed at which to move if negative, moves backwards.
	 * @param ang The angle to move to, positive is cw, negative is cc
	 * @param r The robot.
	 */
	public MoveAlongCurveCommand(double rad, double s, double ang, VisualRobot r) {
		radius = rad;
		speed = s;
		angle = ang;
		if(r!=null) setRobot(r);
		
		if(Math.abs(speed * (radius + WIDTH/2)/radius) >= 1.0 ) {
			speed = Math.signum(speed) * 1/(radius + WIDTH/2)/radius;
		}

	}
	
	private boolean condition() {
		if(angle  < 0)
			return gyro.getAngle() > angle && robot.isAutonomous();
		else if(angle > 0) 
			return gyro.getAngle() < angle && robot.isAutonomous();
					
		return false;
	}
}
