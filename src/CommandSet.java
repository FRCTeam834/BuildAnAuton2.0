import java.io.Serializable;
import java.util.ArrayList;

import visualrobot.Command;

public class CommandSet implements Serializable {

	private static final long serialVersionUID = 9001257732052415805L;
	
	private ArrayList<ArrayList<Command>> commands;
	private int[] threadStarts;
	
	public CommandSet() {
		threadStarts = new int[1];
		commands = new ArrayList<ArrayList<Command>>();
		commands.add(new ArrayList<Command>());
	}
	
	public boolean isEmpty() {
		return commands.size() == 0;
	}
	
	public ArrayList<ArrayList<Command>> getCommands() {
		return commands;
	}
	
	public int[] getThreadStarts() {
		return threadStarts;
	}
	
	public ArrayList<Command> getMain() {
		return commands.get(0);
	}
	
	public void set(ArrayList<ArrayList<Command>> commands, int[] threadStarts) {
		this.commands = commands;
		this.threadStarts = threadStarts;
	}
	
}
