package visualrobot;

import java.util.ArrayList;

public class RunCommands implements Runnable {
	private ArrayList<Command> commands = new ArrayList<Command>();
	
	public RunCommands(ArrayList<Command> c) {
		commands = c;
	}
	
	public void run() {
		for(Command cmd:commands) {
			
			cmd.execute();

		}
	}

}
