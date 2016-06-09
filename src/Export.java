 import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import visualrobot.Command;

import visualrobot.TurnCommand;
import visualrobot.MoveStraightCommand;

public class Export {
	public static double SPEED = .3;
	
	public static ArrayList<Command> convertToCommands(Path2D path, double inchPerPixel, boolean[] backwards) {
		ArrayList<Command> toExport = new ArrayList<Command>();
		PathIterator pi = path.getPathIterator(null);
		double[] coords = new double[6];
		pi.currentSegment(coords);
		
		double lastAngle = 0;
		double lastX = coords[0];
		double lastY = coords[1];
		double currX = 0;
		double currY = 0;
		double currAngle = 0;

		pi.next();
		int i = 0;		
		SPEED = Double.parseDouble(JOptionPane.showInputDialog("Enter a speed between 0.0 and 1.0"));
		
		SPEED = SPEED > 1 ? 1.0 : SPEED; 
		SPEED = SPEED <= 0 ? 0 : SPEED; 

		
		for(; !pi.isDone(); pi.next()) {
			int type = pi.currentSegment(coords);
			
			
			
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
	
				
				if(i==0) dAngle = 0;
	
				while(Math.abs(dAngle) > 180 ) {
					if(dAngle < 180) {
						dAngle += 360;
					}
					if(dAngle > 180) {
						dAngle -= 360;
					}
				}
				
				toExport.add(new TurnCommand(dAngle, SPEED, null));
	
				double distance = Math.sqrt(dX*dX+dY*dY) * inchPerPixel;
				toExport.add(new MoveStraightCommand(distance, backwards[i] ? -SPEED : SPEED, null));
				
				System.out.println(dAngle+ " degrees, " + ((backwards[i] ? -1:1)*  distance) + " inches.");
				lastX = currX;
				lastY = currY;
				lastAngle = currAngle;
				break;
			case 2:
				
			}
			
			i++;
		}
		
		return toExport;
	}
	
	public static void export(ArrayList<Command> commands) {
		try {
			File file = new File("auton.autr");
			ObjectOutputStream oos = new ObjectOutputStream(
									 new BufferedOutputStream(
									 new FileOutputStream(file)));
			oos.writeInt(1);
			oos.writeInt(0);
			oos.writeObject(commands);
			oos.close();
			FileInputStream inputStream = new FileInputStream(file);
			byte[] buffer = new byte[(int)file.length()];
			inputStream.read(buffer);

			URL url = new URL("ftp://anonymous@roborio-" + 
					JOptionPane.showInputDialog("Enter Team Number")
					+ "-frc.local/home/lvuser/auton.autr");
			URLConnection conn = url.openConnection();
			
			
			conn.getOutputStream().write(buffer);
			conn.getOutputStream().close();
			inputStream.close();
			
			file.delete();
			JOptionPane.showMessageDialog(null, "Exported");

			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Failed to Export");
		}
	}

	private static double getCurrAngle(double dX, double dY, double lastAngle) {
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

	private static void convertQuad(ArrayList<Command> commands, double lastX, double lastY, double lastAngle, boolean backwards){}
}
