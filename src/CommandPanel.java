import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import basicCommand.DelayCommand;

public class CommandPanel extends JPanel implements ActionListener {
	
	private JLabel[] labels;
	private JTextField[] fields;
	
	private Class[] types;
	private Class<?> command;
	
	
	public CommandPanel() {
		
	}
	
	public void changeType(Class<?> commandType) {
		try {
			this.removeAll();
			
			Constructor<?>[] constructors = commandType.getConstructors();
			
			Constructor<?> noArguments = commandType.getConstructor();
			Constructor<?> cmdCont = null;
			
			for(Constructor c: constructors) {
				if(!c.equals(noArguments)) {
					cmdCont = c;
					c.getAnnotatedExceptionTypes();
				}
			}
			
			Parameter[] params = cmdCont.getParameters();
			labels = new JLabel[params.length];
			types = new Class[params.length];
			fields = new JTextField[params.length];
			
			this.setLayout(new GridLayout(params.length, 2));
			
			int i = 0;
			for(Parameter p: params) {
				labels[i] = new JLabel(p.getName());
				labels[i].setHorizontalAlignment(SwingConstants.RIGHT);
				
				types[i] = p.getType();
				fields[i] = new JTextField(10);
				
				add(labels[i]);
				add(fields[i]);
				i++;
			}
			
			this.revalidate();
		} 
		catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}


}
