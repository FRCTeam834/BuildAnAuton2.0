package visualrobot;
import java.io.Serializable;

/**
 * Interface representing the actions that a robot can perform.
 */
public interface Command extends Serializable {
	public abstract void execute() throws NullPointerException;
	public abstract String[] getVals();
	public void setRobot(VisualRobot r);
}
