import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;

import basicCommand.Condition;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.GyroBase;
import visualrobot.Command;

public class CommandPanel extends JPanel implements ActionListener {
	
	private JLabel[] labels;
	private ArrayList<JComponent> fields = new ArrayList<>();
	
	private Class[] types;
	private Class<? extends Command> command;
	private Constructor<? extends Command> constructor;
	
	private String[] sensorsLabels = new String[]{"Gyro", "Encoder", "Digital"};
	private Class[] sensorTypes = new Class[]{GyroBase.class, Encoder.class, DigitalInput.class};
	
	
	public CommandPanel() {
		
	}
	
	public void changeType(Class<? extends Command> commandType) {
		try {
			this.removeAll();
			
			Constructor<?>[] constructors = commandType.getConstructors();
			
			Constructor<?> noArguments = commandType.getConstructor();
			
			for(Constructor c: constructors) {
				if(!c.equals(noArguments)) {
					constructor = c;
					c.getAnnotatedExceptionTypes();
				}
			}
			
			Parameter[] params = constructor.getParameters();
			fields.clear();
			labels = new JLabel[params.length];
			types = new Class[params.length];
			
			this.setLayout(new GridLayout(params.length, 2));
			
			int i = 0;
			for(Parameter p: params) {
				
				types[i] = p.getType();
				
				if(p.getType().equals(basicCommand.Condition.class)) {
					labels = Arrays.copyOf(labels, labels.length + 3);
					
					this.setLayout(new GridLayout(params.length+3, 2));
					
					labels[i] = new JLabel("Type of sensor");
					fields.add(new JComboBox(sensorsLabels));
					
					labels[i+1] = new JLabel("Name of Sensor");
					fields.add(new JTextField(10));
					
					labels[i+2] = new JLabel("Sign");
					fields.add(new JComboBox(new String[]{"<", ">"}));

					labels[i+3] = new JLabel("Extent");
					fields.add(new JTextField(10));

					for(int j=0; j < 4; j++){
						add(labels[i+j]);
						add(fields.get(i+j));
					}
					
					i+= 3;
				}
				
				else {
					fields.add(new JTextField(10));
					labels[i] = new JLabel(p.getName());
					labels[i].setHorizontalAlignment(SwingConstants.RIGHT);

					add(labels[i]);

					add(fields.get(i));
				}
				i++;
			}

//			for(JComponent t: fields ) {
//				t.addActionListener(this);
//			}
//			
			this.revalidate();
		} 
		catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();

		}
		
	}
	
	public void load(Command c) {
		String[] vals = c.getVals();
		
		if(vals != null) {
			int i = 0;
			for(Class<?> cl: types) {
				if(cl.equals(Condition.class)) {
					
					((JComboBox<String>) fields.get(i)).setSelectedItem(vals[i]);
					((JTextField)fields.get(i+1)).setText(vals[i+1]);
					((JComboBox<String>)fields.get(i+2)).setSelectedItem(vals[i+2]);
					((JTextField)fields.get(i+3)).setText(vals[i+3]);
	
					i += 4;
				}
				else {
					((JTextField)fields.get(i)).setText(vals[i]);
					
					i++;
				}
				
			}
		}
	}
	
	public Command getCommand() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object[] parameters = new Object[types.length];
		
		int i = 0;
		int j = 0;
		for(Class<?> cl: types) {
			if(cl.equals(Condition.class)) {
				JComboBox<String> sensor = (JComboBox<String>) fields.get(i);
				
				String name = ((JTextField)fields.get(i+1)).getText();
				int sign = ((JComboBox<String>)fields.get(i+2)).getSelectedIndex();
				double extent = Integer.parseInt(((JTextField)fields.get(i+3)).getText());
				switch(sensor.getSelectedIndex()) {
				case 0:
					Condition<GyroBase> b0 = new Condition<>(name, sign, extent);
					parameters[j] = b0;
					break;
				case 1:
					Condition<Encoder> b1 = new Condition<>(name, sign, extent);
					parameters[j] = b1;
					break;

				case 2:
					Condition<DigitalInput> b2 = new Condition<>(name, sign, extent);
					parameters[j] = b2;
					break;

				}
				i+=4;
			}
			else {
				if(cl.equals(Double.class)) {
					
					parameters[j] = Double.parseDouble( ((JTextField)fields.get(i)).getText() );
				}
				else if(cl.equals(Integer.class)) {
					parameters[j] = Integer.parseInt( ((JTextField)fields.get(i)).getText() );
				}
				else {
					parameters[j] = ((JTextField)fields.get(i)).getText() ;

				}
				
				i++;
			}
			j++;

		}
		
		return constructor.newInstance(parameters);
	}

	public void actionPerformed(ActionEvent e) {
		
	}


}
