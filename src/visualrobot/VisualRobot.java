package visualrobot;

import java.util.HashMap;

import edu.wpi.first.wpilibj.*;

public abstract class VisualRobot extends SampleRobot{
	public abstract void setLeftSide(double speed);
	public abstract void setRightSide(double speed);
	
	protected HashMap<String, SensorBase> sensors = new HashMap<>();
	protected HashMap<String, SpeedController> motors = new HashMap<>();
	
	public VisualRobot() {
		super();
	}
	/**
	 * IMPORTANT: Definitions for necessary sensor keys
	 * 	"gyro"
	 * 	"rightEncoder"
	 * 	"leftEncoder"
	 */
	public final HashMap<String, SensorBase> getSensors() {
		return sensors;
	}
	
	
	/**
	 * Do not pass the motors used in setLeftSide and setRightSide
	 * 
	 */
	public final HashMap<String, SpeedController> getMotors() {
		return motors;
	}
	
	public abstract void autonomous();
	
	public void operatorControl() {
		teleOpInit();
		while(!isDisabled()) {
			teleOpPeriodic();
		}
	}
	
	public abstract void teleOpInit();
	public abstract void teleOpPeriodic();
	
	
	
}

