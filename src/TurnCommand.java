import edu.wpi.first.wpilibj.GyroBase;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class TurnCommand implements Command {

	private static final long serialVersionUID = -9196407551785663521L;
	
	private double angle, speed;
	private VisualRobot robot;
	private GyroBase gyro;
	
	public void setRobot(VisualRobot r) {
		robot = r;
		gyro = (GyroBase) robot.getSensors().get("gyro");
	}
	
	public void edit() {
		String[] labels = {"Angle", "Speed"};
		String[] values = {Double.toString(angle), Double.toString(speed)};
 		angle = Double.parseDouble(values[0]);
		
		if(Math.abs(Double.parseDouble(values[1])) <= 1.0) {
			speed = Double.parseDouble(values[1]);
		}
		
	}
	public void execute() throws NullPointerException {
		
		gyro.reset();
		
		if(angle != 0) {
			while (angle > 0 ? gyro.getAngle() < angle : gyro.getAngle() > angle && !robot.isDisabled() && robot.isAutonomous()) {
				robot.setRightSide(angle > 0 ? -speed : speed);
				robot.setLeftSide(angle > 0 ? speed : -speed);
			}
			robot.setRightSide(0.0);
			robot.setLeftSide(0.0);
		}
	}
	
	public TurnCommand() {
	}
	
	/**
	 * 
	 * @param ang The angle of which to turn.
	 * @param s The speed at which to turn.
	 * @param r The robot.
	 */
	public TurnCommand(double ang, double s, VisualRobot r) {
		angle = ang;
		if(s > 1.0) 
			speed = 1.0;
		else if(s < -1.0) 
			speed = -1.0;
		else
			speed = s;
		if(r!=null) setRobot(r);
	}

	
}
