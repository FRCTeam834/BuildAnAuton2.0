import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import basicCommand.Condition;
import basicCommand.MotorCommand;
import basicCommand.WaitCommand;
import edu.wpi.first.wpilibj.GyroBase;
import visualrobot.Command;
import visualrobot.CommandSet;
import visualrobot.MoveAlongCurveCommand;
import visualrobot.TurnCommand;
import visualrobot.MoveStraightCommand;

public class Export {
	
	CommandSet program = new CommandSet(); //The program to be exported
	double inchPerPixel; //Conversion ratio from pixels to inches 
	double initialAngle;
	ArrayList<Double> speeds; //
	ArrayList<Double> turnSpeeds;
	boolean flatten; // Whether to approximate curves to lines, or use arcs
	boolean[] realBackwards;
	CommandSet[] realCommands;
	PathIterator realPath;
	
	int numInMain;
	
	public Export(PathIterator path, double iPP, boolean[] backwards, CommandSet[] commands, boolean f, ArrayList<Double> s, ArrayList<Double> ts, double iang) {
		realPath = path;
		inchPerPixel = iPP;
		realBackwards = backwards;
		realCommands = commands;
		flatten = f;
		speeds = s;
		turnSpeeds = ts;
		initialAngle = iang;
	}
	
	public void convertToCommands(PathIterator path, CommandSet[] commands, double initialAngle, boolean[] backwards) {
		double[] coords = new double[6];
		path.currentSegment(coords);
		
		double lastAngle = initialAngle;
		double lastX = coords[0];
		double lastY = coords[1];
		double currX = 0;
		double currY = 0;
		double currAngle = 0;

		if(commands!= null) {
			program.getMain().addAll(commands[0].getMain());
			for(int j = 1; j < commands[0].getSize(); j++ ){
				program.addThread(0, commands[0].getCommands().get(j));
			}
		}
		
		path.next();
		int i = 0;		

		
		for(; !path.isDone(); path.next()) {

			int type = path.currentSegment(coords);
			
			switch(type) {
			case 1:
				currX = coords[0];
				currY = coords[1];
				
				double dX = currX-lastX;
				double dY = currY-lastY;
				
				currAngle = getCurrAngle(dX, dY, lastAngle);
				
				if(backwards[i]) {
					currAngle +=180;
				}
				
				double dAngle = currAngle - lastAngle;
				
//				if(i==0) dAngle = 0;
	
				while(Math.abs(dAngle) > 180 ) {
					if(dAngle < 180) {
						dAngle += 360;
					}
					if(dAngle > 180) {
						dAngle -= 360;
					}
				}
				
				program.getMain().add(new TurnCommand(dAngle, turnSpeeds.get(i)));
				numInMain++;
				
				double distance = Math.sqrt(dX*dX+dY*dY) * inchPerPixel;
				program.getMain().add(new MoveStraightCommand(distance, backwards[i] ? -speeds.get(i) : speeds.get(i)));				
				numInMain++;

				System.out.println(dAngle+ " degrees, " + ((backwards[i] ? -1:1)*  distance) + " inches.");
				lastX = currX;
				lastY = currY;
				lastAngle = currAngle;
				break;
			case 2:
				if(flatten) {
					QuadCurve2D.Double q = new QuadCurve2D.Double(lastX, lastY, coords[0], coords[1], coords[2], coords[3]);
					if(backwards[i]) lastAngle += 180;
					convertCurve2(program.getMain(), q, lastAngle, speeds.get(i), backwards[i], inchPerPixel);
				}
				else {
					QuadCurveEquation q = new QuadCurveEquation(new QuadCurve2D.Double(lastX, lastY, coords[0], coords[1], coords[2], coords[3]));
					if(backwards[i]) lastAngle += 180;
					convertCurve(program.getMain(), 0, q, lastAngle, speeds.get(i), backwards[i], inchPerPixel);
				}
				break;
			case 3:
				break;
			}
			i++;

			if(commands!= null) {
				program.getMain().addAll(commands[i].getMain());
				for(int j = 1; j < commands[i].getSize(); j++ ){
					program.addThread(numInMain + commands[i].getThreadStarts()[j], 
							commands[i].getCommands().get(j));
				}
			}

			
			
		}
		
	}
	
	/**
	 * Sends the program to the robot to be used
	 */
	public  void export() {
		try {
			
			convertToCommands(realPath, realCommands, initialAngle, realBackwards);

			File file = new File("auton.autr");
			ObjectOutputStream oos = new ObjectOutputStream(
									 new BufferedOutputStream(
									 new FileOutputStream(file)));

			for(Command c : program.getMain()) {
				System.out.println(c);
			}

			oos.writeObject(program);
			System.out.println(program);
			
			oos.close();

			
			FileInputStream inputStream = new FileInputStream(file);

			byte[] buffer = new byte[(int)file.length()];
			inputStream.read(buffer);

			String teamNumber = JOptionPane.showInputDialog(null,"Enter Team Number");
			if(teamNumber == null) {
				throw new Exception();
			}
			
			URL url = new URL("ftp://anonymous@roborio-" + teamNumber
					+ "-frc.local/home/lvuser/auton.autr");
			URLConnection conn = url.openConnection();
			
			
			conn.getOutputStream().write(buffer);
			conn.getOutputStream().close();
			inputStream.close();
			
			file.delete();
			JOptionPane.showMessageDialog(null, "Exported");

			
		} 
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Failed to Export");

			e.printStackTrace();
		}
	}

	/**
	 * Calculates the angle based on change in x and y
	 */
	private  double getCurrAngle(double dX, double dY, double lastAngle) {
		double currAngle;
		if(dX == 0) {
			if(dY == 0){
				currAngle = lastAngle;
			}
			else {
				currAngle = dY < 0 ? 270 : 90;
			}
		}
		else if(dX > 0) {
			currAngle = Math.atan(dY/dX)*180.0/Math.PI;
		}
		else {
			currAngle = Math.atan(dY/dX)*180.0/Math.PI + 180.0;
		}
		
		return currAngle;
	}

	/**
	 * Converts to sectors
	 */
	private  void convertCurve(ArrayList<Command> commands, double start, CurveEquation curve, double lastAngle, double speed, boolean backwards, double inchPerPixel) {
		double x = curve.getX(0);
		double y = curve.getY(0);
		double goal = 1.0;
		double error = 0;
		boolean lastError = true;
		boolean done = false;
		double radius = 0;
		Point2D.Double center = null;
		
		//Check if points are colinear
		
		
		double floor = start, ceil = 2.0;
		do {
		
			
//			if(prevCenter == null) 
				center = findCenter(x, y, curve.getX((start + goal)/2), curve.getY((start + goal)/2), curve.getX(goal), curve.getY(goal));
//			else 
//				center = findCenter2(x, y, prevCenter.x, prevCenter.y, curve.getX(goal), curve.getY(goal));

			if(center == null) return;
			radius = center.distance(x, y);
			
			error = Math.abs(center.distance(curve.getX(start+ (goal-start)/4), curve.getY(start+ (goal-start)/4)) - radius);
			error += Math.abs(center.distance(curve.getX(start+ 3*(goal-start)/4), curve.getY(start + 3*(goal-start)/4)) - radius);
			
			
			if(error > .5) {
				if(!lastError) {
					done = true;
				}
				else {
					ceil = goal;
					goal = (floor + ceil)/2;
				}
				lastError = true;
			}
			else if(goal == 1) {
				done = true;
			}
			else {
				floor = goal;
				goal = (floor + ceil)/2;				
				lastError = false;
			}
		}
		while(!done);
//		System.out.println(error);
 		
		double iAngle = Math.atan2(-curve.getY(start)+center.y, curve.getX(start)-center.x) * 180/Math.PI;
		double fAngle = Math.atan2(-curve.getY(goal)+center.y, curve.getX(goal)-center.x) * 180/Math.PI;
		double mAngle = Math.atan2(-curve.getY((goal + start)/2)+center.y, curve.getX((goal + start)/2)-center.x) * 180/Math.PI;
		
		
		if(iAngle < 0) iAngle +=360;
		if(fAngle < 0) fAngle +=360;
		if(mAngle < 0) mAngle +=360;

		double dTheta = fAngle-iAngle;
		
		if(!(mAngle < iAngle && mAngle>fAngle || mAngle >iAngle && mAngle < fAngle)) {
			if(dTheta > 0) {
				dTheta -= 360;
			}
			else if(dTheta < -0) {
				dTheta += 360;
			}
		}

		while(Math.abs(dTheta) > 180 ) {
			if(dTheta < 180) {
				dTheta += 360;
			}
			if(dTheta > 180) {
				dTheta -= 360;
			}
		}

		
		double AngleError = iAngle + (dTheta < 0 ? -90 : 90)  - lastAngle;

		commands.add(new TurnCommand(-AngleError, speed));
		commands.add(new MoveAlongCurveCommand(radius * inchPerPixel,  backwards ? -speed : speed, -dTheta)); 
		numInMain+=2;

//		System.out.println("Turning " + -AngleError + " Degrees");
//		System.out.println("Extent: " + dTheta + "\tRadius: " + radius * inchPerPixel);
		
		if(goal < 1) {
			convertCurve(commands, goal, curve, fAngle + (dTheta < 0 ? -90 : 90), speed, backwards, inchPerPixel);
		}
		

	}

	/**
	 * Converts to lines
	 */
	private void convertCurve2(ArrayList<Command> commands, Shape curve, double lastAngle, double speed, boolean backwards, double inchPerPixel) {
		PathIterator pi = curve.getPathIterator(null, 2);
		
		int count = -1;
		for(; pi.isDone(); pi.next()) {
			count++;
		}
		boolean[] expandedBackwards = new boolean[count];
		pi = curve.getPathIterator(null, 2);
		
		Arrays.fill(expandedBackwards, backwards);
		
		convertToCommands(pi, null, lastAngle, expandedBackwards);
		
	}
	
	private Point2D.Double findCenter(double x1, double y1, double x2, double y2, double x3, double y3) {
		double midPoint1x = (x1+x2)/2;
		double midPoint1y = (y1+y2)/2;
		double midPoint2x = (x2+x3)/2;
		double midPoint2y = (y2+y3)/2;


		if(y2-y1 == 0 || y3-y2 == 0) {
			return null;
		}
		
		double slope1 = -1*(x2-x1)/(y2-y1);
		double slope2 = -1*(x3-x2)/(y3-y2);

		if(slope1 != slope2) {
		
			double x = ((midPoint2y-midPoint1y) + (slope1*midPoint1x-slope2*midPoint2x))/(slope1-slope2);
			double y = slope1*(x-midPoint1x) + midPoint1y;
			return new Point2D.Double(x, y);
		}
		return null;
	}

}
