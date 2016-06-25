package visualrobot;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class CommandSet implements Serializable {

	private static final long serialVersionUID = 9001257732052415805L;
	
	private ArrayList<ArrayList<Command>> commands;
	private int[] threadStarts;
	
	public CommandSet() {
		threadStarts = new int[1];
		commands = new ArrayList<ArrayList<Command>>();
		commands.add(new ArrayList<Command>());
	}
	
	public ArrayList<ArrayList<Command>> getCommands() {
		return commands;
	}
	
	public int[] getThreadStarts() {
		return threadStarts;
	}
	
	public boolean isEmpty() {
		return getSize() == 1 && getMain().isEmpty();
	}
	
	public int getSize() {
		return threadStarts.length;
	}
	
	public void addThread(int start, ArrayList<Command> toAdd) {
		threadStarts = Arrays.copyOf(threadStarts, threadStarts.length+1);
		threadStarts[threadStarts.length-1] = start;
		commands.add(toAdd);
	}
	
	public void addToMain(Command toAdd) {
		commands.get(0).add(toAdd);
	}

	public ArrayList<Command> getMain() {
		return commands.get(0);
	}
	
	public void set(ArrayList<ArrayList<Command>> commands, int[] threadStarts) {
		this.commands = commands;
		this.threadStarts = threadStarts;
	}
	
	public String toString() {
		String str = "";
		
		for(int i = 0; i < getSize(); i++) {
			str += (threadStarts[i] + ": ");
			for(Command c: commands.get(i)) {
				str += (c.getClass().getName() + " ") ;
			}
			str += '\n';
		}
		return str;
	}
}
