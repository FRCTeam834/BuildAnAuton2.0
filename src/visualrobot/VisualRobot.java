package visualrobot;

import java.util.HashMap;

import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.SensorBase;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.SpeedController;

/**
 * The class that any robot using BuildAnAuton must implement.
 * 
 * The user must implement the methods for setting the right and left sides of the (assumed tank drive)
 * robot
 * 
 * The class contains similar code for the teleOperated period as WPILib's Iterative robot,
 * with <code>teleOpInit()</code> that runs at the beginning of the program and a <code>teleOpPeriodic</code>
 * that runs on a loop.
 * 
 */
public abstract class VisualRobot extends SampleRobot{
	
	/**
	 * Given a double between -1.0 and 1.0 inclusive, sets the motors corresponding to the left side of the robot to that amount. 
	 */
	public abstract void setLeftSide(double speed);	
	
	/**
	 * Given a double between -1.0 and 1.0 inclusive, sets the motors corresponding to the right side of the robot to that amount. 
	 */
	public abstract void setRightSide(double speed);
	
	/**
	 * Dictionary containing sensors used by WaitCommand
	 * Also, contains sensors for movement, specifically
	 * 		"gyro" - Gyro that indicates the angle of the robot, clockwise should be positive
	 * 		"rightEncoder" - Encoder on right side, forward is positive
	 * 	 	"leftEncoder" - Encoder on left side, forward is positive
	 */
	protected HashMap<String, SensorBase> sensors = new HashMap<>();
	
	/**
	 * Dictionary containing motors used by MotorCommand
	 * Do not pass the motors used in setLeftSide and setRightSide
	 */
	protected HashMap<String, SpeedController> motors = new HashMap<>();
	
	protected HashMap<String, Servo> servos = new HashMap<>();

	
	public VisualRobot() {
		super();
	}

	
	public final HashMap<String, SensorBase> getSensors() {
		return sensors;
	}
	

	public final HashMap<String, SpeedController> getMotors() {
		return motors;
	}
	
	public final HashMap<String, Servo> getServos() {
		return servos;
	}

	
	public abstract void autonomous();
	
	public void operatorControl() {
		teleOpInit();
		while(!isDisabled() && isOperatorControl()) {
			teleOpPeriodic();
		}
	}
	
	public abstract void teleOpInit();
	public abstract void teleOpPeriodic();
	
	
	
}

