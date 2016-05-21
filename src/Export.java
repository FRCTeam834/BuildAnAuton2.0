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

public class Export {
	
	
	public static ArrayList<Command> convertToCommands(Path2D path, double initialAngle, double inchPerPixel) {
		ArrayList<Command> toExport = new ArrayList<Command>();
		PathIterator pi = path.getPathIterator(null);
		double[] coords = new double[6];
		pi.currentSegment(coords);
		
		double lastAngle = initialAngle;
		double lastX = coords[0];
		double lastY = coords[1];
		double currX = 0;
		double currY = 0;
		double currAngle = 0;

		pi.next();
		
		for(; !pi.isDone(); pi.next()) {
			pi.currentSegment(coords);
			currX = coords[0];
			currY = coords[1];
			
			double dX = currX-lastX;
			double dY = currY-lastY;
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
			
			double dAngle = currAngle - lastAngle;
			
			while(Math.abs(dAngle) > 180 ) {
				if(dAngle < 180) {
					dAngle += 360;
				}
				if(dAngle > 180) {
					dAngle -= 360;
				}
			}
			
			toExport.add(new TurnCommand(-dAngle, .5, null));

			double distance = Math.sqrt(dX*dX+dY*dY) * inchPerPixel;
			toExport.add(new MoveStraightCommand(distance, .5, null));
			
			System.out.println(dAngle+ " degrees, " + distance + " inches.");
			lastX = currX;
			lastY = currY;
			lastAngle = currAngle;
			
		}
		
		return toExport;
	}
	
	public static void export(ArrayList<Command> commands) {
		try {
			File file = new File("temp.autr");
			ObjectOutputStream oos = new ObjectOutputStream(
									 new BufferedOutputStream(
									 new FileOutputStream(file)));
			oos.writeInt(0);
			oos.writeInt(0);
			oos.writeObject(commands);
			oos.close();
			FileInputStream inputStream = new FileInputStream(file);
			byte[] buffer = new byte[(int)file.length()];
			inputStream.read(buffer);

			URL url = new URL("ftp://anonymous@roborio-" + 
					JOptionPane.showInputDialog("Enter Team Number")
					+ "-frc.local/home/lvuser");
			URLConnection conn = url.openConnection();
			
			conn.getOutputStream().write(buffer);
			conn.getOutputStream().close();
			inputStream.close();
			
			
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Failed to Export");
		}
	}

}
