// Authors: 
// Last Edited: 1/15/2016
// Description: Command used for moving the robot straight forward.

package visualrobot;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.GyroBase;
import edu.wpi.first.wpilibj.Timer;

public class MoveStraightCommand implements Command {

	private static final long serialVersionUID = -9199930939935475592L;
	
	private final double TIME_INTERVAL = .05;
	
	//Variable to represent the robot.
	private VisualRobot robot;
	//Left and right encoders used to get distance travelled by the wheels.
	private Encoder LEncoder, REncoder;
	//Gyro variable used for getting the rotation of the robot.
	private GyroBase gyro;
	
	//The speed the robot should move, the distance it should travel, and a c factor value.
	public double speed, distance;
	private double kP, kI, kD; //PID Constants

	public void execute() throws NullPointerException {
		//Reset encoders and gyro.
		REncoder.reset();
		LEncoder.reset();
		gyro.reset();
		
		kP = Math.abs(speed / 25.0);
		kI = 0;
		kD = 0;
		
		double accumulatedError = 0;
		double lastAngle = gyro.getAngle();
			
		//While loop to end once the desired distance is travelled.
		while(Math.abs(REncoder.getDistance() + LEncoder.getDistance()) / 2 < Math.abs(distance) && robot.isAutonomous() && !robot.isDisabled()) {
			//Speed of left and right wheels.
			double lspeed = speed, rspeed = speed;
			
			double angle = gyro.getAngle();
			double changeInError = angle - lastAngle;

			accumulatedError += angle; 
			lastAngle = angle;
						
			//If the gyro's angle is less than zero, change the right wheel's speed.
			if(gyro.getAngle() < 0){
				rspeed -= Math.abs(gyro.getAngle()) * kP + accumulatedError * kI + changeInError * kD;
			}
			//If the gyro's angle is more than zero, change the left wheel's speed
			else if(gyro.getAngle() > 0) {
				lspeed -= Math.abs(gyro.getAngle()) * kP + accumulatedError * kI + changeInError * kD;
			}
			//Set the left and right wheel speeds.
			robot.setLeftSide(lspeed);
			robot.setRightSide(rspeed);
			Timer.delay(TIME_INTERVAL);

		}
		robot.setLeftSide(0.0);
		robot.setRightSide(0.0);
		System.out.println("Error = " + (Math.abs(REncoder.getDistance() + LEncoder.getDistance()) / 2-distance) + " inches");

	}
	
	public void setRobot(VisualRobot r) {
		//Initialize robot variable, gyro variable, and encoder variables.
		robot = r;
		gyro = (GyroBase) robot.getSensors().get("gyro");
		LEncoder = (Encoder) robot.getSensors().get("leftEncoder");
		REncoder = (Encoder) robot.getSensors().get("rightEncoder");
	}
	
	public MoveStraightCommand() {
	}
	
	/**
	 * 
	 * @param dist The distance to move.
	 * @param s The speed at which to move.
	 */
	public MoveStraightCommand(double dist, double s) {
		distance = dist;
		speed = s;
	}
	
	public String toString() {
		return speed + "/" + distance + "inches";
	}

}
