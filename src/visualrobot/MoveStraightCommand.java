// Authors: 
// Last Edited: 1/15/2016
// Description: Command used for moving the robot straight forward.

package visualrobot;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.GyroBase;

public class MoveStraightCommand implements Command {

	private static final long serialVersionUID = -9199930939935475592L;
	
	//Variable to represent the robot.
	private VisualRobot robot;
	//Left and right encoders used to get distance travelled by the wheels.
	private Encoder LEncoder, REncoder;
	//Gyro variable used for getting the rotation of the robot.
	private GyroBase gyro;
	
	//The speed the robot should move, the distance it should travel, and a c factor value.
	private double speed, distance, cFactor;

	public void execute() throws NullPointerException {
		//Reset encoders and gyro.
		REncoder.reset();
		LEncoder.reset();
		gyro.reset();
		cFactor = Math.abs(speed / 25.0);

		//While loop to end once the desired distance is travelled.
		while(Math.abs(REncoder.getDistance() + LEncoder.getDistance()) / 2 < Math.abs(distance) && robot.isAutonomous() && !robot.isDisabled()) {
			//Speed of left and right wheels.
			double lspeed = speed, rspeed = speed;
			//If the gyro's angle is less than zero, change the right wheel's speed.
			if(gyro.getAngle() < 0){
				rspeed -= Math.abs(gyro.getAngle()) * cFactor;
			}
			//If the gyro's angle is more than zero, change the left wheel's speed
			else if(gyro.getAngle() > 0) {
				lspeed -= Math.abs(gyro.getAngle()) * cFactor;
			}
			//Set the left and right wheel speeds.
			robot.setLeftSide(lspeed);
			robot.setRightSide(rspeed);


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
}
