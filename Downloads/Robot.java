//Robot for using BuildAnAuton2.
package org.usfirst.frc.team834.robot;

//Importing the BuildAnAuton libraries and wpilib
import visualrobot.*; 
import edu.wpi.first.wpilibj.*;

/*
 * This is an example of how to use VisualRobot and BuildAnAuton
 */
public class Robot extends VisualRobot {
	
	//This is the gyro that attaches right to the roboRIO and 
	//It should be parallel to the floor facing up to measure the angle of the robot
	private ADXRS450_Gyro robotGyro = new ADXRS450_Gyro(); 

	//Encoders, that are plugged into DIO ports of roboRIO
	private Encoder rightEncoder = new Encoder(0, 1);
	private Encoder leftEncoder = new Encoder(2,3);
	
	//Four drive motors
	//I haven't actually initialized them
	CANTalon[] motors = new CANTalon[4];
	/* 0: Front Left
	 * 1: Rear Left
	 * 2: Front Right
	 * 3: Rear Right
	 */
	
	CANTalon arm = new CANTalon(4);
	
			
	public void robotInit() {
		
		//Here, I put the sensors into the HashMap sensors, which is part of the
		//VisualRobot parent class, hence super
		//store your gyro as "gyro"
		//store your encoders as "rightEncoder" and "leftEncoder"
		//These are necessary for the app to work
		super.sensors.put("rightEncoder", rightEncoder);
		super.sensors.put("leftEncoder", leftEncoder);
		super.sensors.put("gyro", robotGyro);
		
		/*
		Here, I calibrate the encoders to return a distance in inches when the method getDistance() is called
		This is necessary because when BuildAnAuton outputs an auton file, the info is in inches
		Generally, a good estimation can be achieved with gear ratios and wheel diameter
		In this case, the encoder was mounted right onto the output shaft, so the distance in inches
		per rotation was simply the circumference of the wheel
		Further calibration can be made by turning the wheels a known distance, seeing what the robot outputs
		(You'd have to code this output yourself), and multiplying the values below by a value of the known distance
		divided by the output.
		 */
		rightEncoder.setDistancePerPulse(1.0/(3.02*Math.PI * 4.0));
		leftEncoder.setDistancePerPulse(1.0/(3.02*Math.PI * 4.0));
		
		//Here, I put the arm into the extra motors map. It can be accessed through the
		// select screen on the app, with the name as "arm".
		super.motors.put("arm", arm);
		
		// Just in case
		rightEncoder.reset();
		leftEncoder.reset();
		robotGyro.calibrate();
	}	

	
	//Simple implementation of setLeftSide, assuming 0 and 1 are the correct motors
	public void setLeftSide(double speed) {
		if(speed < -1.0 || speed > 1.0)
			return;
		motors[0].set(speed);
		motors[1].set(speed);
	}
	
	//Simple implementation of setLeftSide, assuming 2 and 3 are the correct motors. The negative is because the motors are oriented backwards
	public void setRightSide(double speed) {
		if(speed < -1.0 || speed > 1.0)
			return;
		motors[2].set(-speed);
		motors[3].set(-speed);
	}

	//Runs the auton command 
	public void autonomous() {	
		
		robotGyro.reset();
		

		//This is how you run the command, if you upload multiple autons to your robot
		//you choose them by putting the filename (no extension) in here.
		ChooseAuton c = new ChooseAuton(this);
		c.chooseAuton("auton"); 

		
		
	}
	
	public void teleOpInit() {

	}

	public void teleOpPeriodic() {

	}
	
}
