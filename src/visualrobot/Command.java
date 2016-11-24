package visualrobot;
import java.io.Serializable;

/**
 * Interface representing the actions that a robot can perform.
 */
public interface Command extends Serializable {
	public void execute() throws NullPointerException;
	public void setRobot(VisualRobot r);
}
