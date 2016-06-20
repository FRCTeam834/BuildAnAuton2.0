package basicCommand;

import edu.wpi.first.wpilibj.GyroBase;
import edu.wpi.first.wpilibj.SensorBase;
import visualrobot.VisualRobot;

import java.util.HashMap;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;

public class Condition<T extends SensorBase> {
	
	private T sensor;
	GyroBase gyro;		
	double amount;
	int sign;

	
	public static final int LESS_THAN = 0;
	public static final int GREATER_THAN = 1;

	public boolean check() {
		if(GyroBase.class.isInstance(sensor)) {
			System.out.println("ASDFASD");
			GyroBase gyro = (GyroBase) sensor;
			System.out.println(gyro.getAngle());
			System.out.println(amount);
			if(sign == 0) {
				return gyro.getAngle() < amount;
			}
			else if(sign == 1) {
				return gyro.getAngle() > amount;
			}
		}
	
		
		
		return false;
	}

	public Condition(String name, int sign, double amount, VisualRobot r) {
		 Object o = r.getSensors().get(name);
		 if(o != null && SensorBase.class.isInstance(o)) 
			 sensor = (T) o;

		 this.sign = sign;
		 this.amount = amount;

	}

	public String getUnit() {
		if(Encoder.class.isInstance(sensor)) {
			return "Inches";
		}
		else if(GyroBase.class.isInstance(sensor)) {
			return "Degrees";
		}
		else if(DigitalInput.class.isInstance(sensor)) {
			return "off: 0";
		}
		else {
			return "Idk";
		}
	}
	
}
