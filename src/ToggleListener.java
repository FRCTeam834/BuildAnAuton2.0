import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

public class ToggleListener implements KeyListener{
	private HashMap<Integer, Boolean> keysBound;
	public BuildAnAuton2 frame;
	
	public ToggleListener(BuildAnAuton2 b, HashMap<Integer, Boolean> keys) {
		frame = b;
		keysBound = keys;
	}
	
	public void keyTyped(KeyEvent e) {
		
	}

	public void keyPressed(KeyEvent e) {
		keysBound.replace(e.getKeyCode(), true);
	}

	public void keyReleased(KeyEvent e) {
		keysBound.replace(e.getKeyCode(), false);
	}
	
	
	
}
