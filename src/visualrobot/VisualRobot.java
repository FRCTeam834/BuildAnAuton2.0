package visualrobot;

import java.util.HashMap;

import edu.wpi.first.wpilibj.*;

public abstract class VisualRobot extends SampleRobot{
	public abstract void setLeftSide(double speed);
	public abstract void setRightSide(double speed);
	
	
	public VisualRobot() {
		super();
	}
	/**
	 * IMPORTANT: Definitions for necessary sensor keys
	 * 	"gyro"
	 * 	"rightEncoder"
	 * 	"leftEncoder"
	 * For other sensors, define by lower case name of class, followed by number of port(s), separated by underscores
	 * ex.
	 * 	"encoder_1_2" <- Extra encoder (perhaps for a lift), with ports on 1 and 2.
	 *  "ultrasonic_1"
	 */
	public abstract HashMap<String, SensorBase> getSensors();
	
	
	public abstract void autonomous();
	
	public void operatorControl() {
		teleOpInit();
		while(!isDisabled()) {
			teleOpPeriodic();
		}
	}

	public abstract void getWidth();
	
	public abstract void teleOpInit();
	public abstract void teleOpPeriodic();
	
}
