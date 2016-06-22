package visualrobot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


/*
 * This class reads the auton file. The Thread stuff is a planned feature, and doesn't do anything.
 */
public class ChooseAuton {
	Thread[] threads = null;
	int[] threadStarts = null;
	ArrayList<Command> main = new ArrayList<Command>();
	VisualRobot robot;
	
	public ChooseAuton(VisualRobot r) {
		robot = r;
	}
	
	public void chooseAuton(String fName) {
		File file = new File("/home/lvuser/" + fName + ".autr"); //Select file

		try {
		
			ObjectInputStream ois;
			ois = new ObjectInputStream(new FileInputStream(file));
			int numThreads = ois.readInt();
			threadStarts = new int[numThreads];
			threads = new Thread[numThreads];

			
			threadStarts[0] = ois.readInt();
			main = (ArrayList<Command>) ois.readObject();

			
			for(int thread = 1; thread < numThreads; thread++ ) {
				threadStarts[thread] = ois.readInt();
				ArrayList<Command> commands= (ArrayList<Command>) ois.readObject();
				for(Command c: commands)
					c.setRobot(robot);
				threads[thread] = new Thread(new RunCommands(commands));
			}
			
			for(Command c: main)
				c.setRobot(robot);
			
			ois.close();
		}
		catch(IOException e){e.printStackTrace();} 
		catch (ClassNotFoundException e) {e.printStackTrace();}

				
		int i = 0;
		while(robot.isAutonomous() && !robot.isDisabled() && i < main.size()) {
			try {
				for(int start = 1; start < threadStarts.length; start++)
					if (threadStarts[start] == i)
						threads[start].start();
				main.get(i).execute();
			}
			catch(NullPointerException e) {SmartDashboard.putString("DB/String 5", e.getLocalizedMessage());}
			finally {
				i++;
			}
		}	

	}
	
}
