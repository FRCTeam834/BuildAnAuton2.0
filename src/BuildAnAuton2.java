import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.lang.Math;

import visualrobot.CommandSet;

import java.net.URL;

/**
 * This is the main GUI for BuildAnAuton2.0. It displays the field, has save and load functionality etc.
 * 
 * @author Daniel Qian
 * @author Ben Zalatan
 */
public class BuildAnAuton2 extends JFrame implements MouseListener {
	
	//Tools that allow you to manipulate the path/actions
	JToolBar toolbar = new JToolBar(); 
		JButton add = new JButton("Add");
		JButton add2 = new JButton("Add Curve");
		JButton edit = new JButton("Edit");
		JButton select = new JButton("Select");
		JButton delete = new JButton("Delete");
		JButton restart = new JButton("Restart");
		JButton speed = new JButton("Speed");
	
	//Array of tools, allows program to disable/enable all
	JButton[] tools = {add, add2, edit, select, delete, restart, speed};
		
	//Allows user to type commands into field, liek CAD program (not yet implemented)
	JTextField prompt = new JTextField();
	
	//Dialog for choosing save/load location
	JFileChooser fs = new JFileChooser();
	
	//Menu options for file actions and settings
	JMenuBar menu = new JMenuBar();
	JMenu file = new JMenu("File");
		public JMenuItem save = new JMenuItem("Save");
		public JMenuItem load = new JMenuItem("Load");
		public JMenuItem export = new JMenuItem("Export");
	JMenu settings = new JMenu("Settings");
		public JMenuItem setDefaultSpeed = new JMenuItem("Set Default Speed");
		public JCheckBoxMenuItem snapToPoints = new JCheckBoxMenuItem("Snap to Existing Points");
		public JMenuItem setInitialAngle = new JMenuItem("Set Initial Angle");

	//Dialog to edit secondary actions
	CommandEditor cmdEditor;
	
	//Helps with code readability (?) and keeps track of selected tool. I'm not sure I'm using this right
	public enum SelectedTool {
		NONE,
		ADD,
		ADD2,
		SELECT,
		EDIT,
		DEL,
		SPEED;
	}
	SelectedTool tool = SelectedTool.NONE;
	
	
	double defaultSpeed = 0.5; //Default default speed. Can be changed in Settings -> Set Default Speed
	
	boolean dragging = false; //Edit: whether a point is being moved
	int addStep = 0; //Add Curve/Add2, which point is being added (endpoint or control point)
	Point endPoint; //Add Curve/Add2, temporary point before control points are added
	
	int curveSelected = -1; //Edit, Delete, Select: Which part of the path is being selected
	int pointSelected = -1; //Edit: Which point is being selected (curved paths have multiple points)
	
	//Map of keys that affect program when pressed (B and Shift). See ToggleListener
	//The integer key is the key ID, the boolean value is whether it is being pressed
	HashMap<Integer, Boolean> keys = new HashMap<>();

	double initialAngle; //The starting angle of the robot, in degrees (right is 0, goes counter clockwise)
	
	boolean[] backwards = new boolean[0]; //Whether the robot travels backwards along each sub path 
	ArrayList<Double> speeds = new ArrayList<Double>(); //The speed the robot travels along each sub path
	CommandSet[] commands = new CommandSet[1]; //A set of secondary Commands to run when the robot reaches each point (includes start)
	
	double inchPerPixel; //Conversion ratio from diagram to real field
	
	JScrollPane scrollPane = new JScrollPane(); //Allows the user to scroll to see entire field

	public double zoom = 1; //Zoom scale, adjusted using - and +/= keys
	
	int selectedLineIndex = -1, linesSelected = 0;//Speed
	
	JComponent p = new JComponent() {
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			
			g2.setRenderingHint(
					
			        RenderingHints.KEY_ANTIALIASING,
			        RenderingHints.VALUE_ANTIALIAS_ON);
			
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(3));
			
			if(field != null) {
				if(zoom < 1)
					g2.translate(-((zoom - 1) * field.getWidth())/2, -((zoom - 1) * field.getHeight())/2);
				g2.scale(zoom, zoom);
			}
			g2.drawImage(field, 0, 0, null);
			g2.draw(path);
			
			
			
			if(tool == SelectedTool.ADD && p.getMousePosition() != null) {
				double addAngle, addDistance; //Displays values for angle (degrees) and distance (in) for line being added


				if(keys.get(KeyEvent.VK_SHIFT)) {
					int mouseX = getScaledMousePosition().x;
					int mouseY = getScaledMousePosition().y;
					int pX = (int) path.getCurrentPoint().getX();
					int pY = (int) path.getCurrentPoint().getY();
					double angle = Math.atan2(mouseY-pY, mouseX-pX)*180.0/Math.PI;
					if(angle < 0) angle += 360;
					addAngle = Math.round(angle/45.0) * 45.0;

					if(addAngle == 180 || addAngle == 0) {
						g2.drawLine(pX, pY, mouseX, pY);
						addDistance = Math.abs(mouseX-pX) * inchPerPixel;
					}
					else if (addAngle == 90 || addAngle == 270) {
						g2.drawLine(pX, pY, pX, mouseY);
						addDistance = Math.abs(mouseY-pY) * inchPerPixel;

					}
					else {
						double magnitude = Math.sqrt(Math.pow(pX - mouseX, 2) + Math.pow(pY - mouseY, 2));
						addDistance = magnitude * inchPerPixel;
						double angleRadians = addAngle * Math.PI/180.0;
						g2.drawLine(pX, pY, (int) (pX + Math.cos(angleRadians) * magnitude), (int) (pY + Math.sin(angleRadians) * magnitude));	
					}
					
				}
				else
				{		
					if(snapToPoints.getState())
					{
						double[] cyeet = new double[6];
						ArrayList<Point> pathPts = new ArrayList<Point>();
						int idx = -1;
						double dist = 20;
						for(PathIterator glagla = path.getPathIterator(null); !glagla.isDone(); glagla.next())
						{
							int t = glagla.currentSegment(cyeet);
							if(cyeet[0] == 0 && cyeet[1] == 0) continue;
							pathPts.add(new Point((int)cyeet[0], (int)cyeet[1]));
							if(new Point((int)cyeet[0], (int)cyeet[1]).distance(getScaledMousePosition()) <= dist)
							{
								idx = pathPts.size() - 1;
								dist = new Point((int)cyeet[0], (int)cyeet[1]).distance(getScaledMousePosition());
							}
						}
						if(idx == -1)
						{
							g2.drawLine((int) path.getCurrentPoint().getX(), (int) path.getCurrentPoint().getY(), getScaledMousePosition().x, getScaledMousePosition().y);	
							addAngle = Math.atan2(getScaledMousePosition().y - path.getCurrentPoint().getY(),  getScaledMousePosition().x-path.getCurrentPoint().getX()) * 180.0/Math.PI;
							addDistance = new Point((int) path.getCurrentPoint().getX(), (int) path.getCurrentPoint().getY()).distance(new Point(getScaledMousePosition().x, getScaledMousePosition().y)) * inchPerPixel;
						}
						else
						{
							g2.drawLine((int) path.getCurrentPoint().getX(), (int) path.getCurrentPoint().getY(), pathPts.get(idx).x, pathPts.get(idx).y);
							addAngle = Math.atan2(getScaledMousePosition().y - path.getCurrentPoint().getY(),  getScaledMousePosition().x-path.getCurrentPoint().getX()) * 180.0/Math.PI;
							addDistance = new Point((int) path.getCurrentPoint().getX(), (int) path.getCurrentPoint().getY()).distance(pathPts.get(idx)) * inchPerPixel;
						}
					}
					else
					{
						g2.drawLine((int) path.getCurrentPoint().getX(), (int) path.getCurrentPoint().getY(), getScaledMousePosition().x, getScaledMousePosition().y);	
						addAngle = Math.atan2(getScaledMousePosition().y - path.getCurrentPoint().getY(),  getScaledMousePosition().x-path.getCurrentPoint().getX()) * 180.0/Math.PI;
						 addDistance = new Point((int) path.getCurrentPoint().getX(), (int) path.getCurrentPoint().getY()).distance(new Point(getScaledMousePosition().x, getScaledMousePosition().y)) * inchPerPixel;
					}
				}
				
				addAngle = addAngle == 0 ? 0 :360-addAngle; //Cause y is inverted on screen
				addAngle = Math.round(addAngle * 100.0) / 100.0; 

				if(addAngle < 0) addAngle += 360;
				addDistance = Math.round(addDistance * 100.0) / 100.0;
				
				g2.drawString("Angle: " + addAngle + " degrees", 10, this.getHeight() - 25);
				g2.drawString("Distance: " + addDistance + " in", 10, this.getHeight() - 10);
			}
			
			if(tool == SelectedTool.ADD2 && p.getMousePosition() != null) {
				if(addStep == 2) {
					g2.drawLine((int) path.getCurrentPoint().getX(), (int) path.getCurrentPoint().getY(), getScaledMousePosition().x, getScaledMousePosition().y);			
				}

				if(addStep == 1) {
					QuadCurve2D.Double curve = new QuadCurve2D.Double((int) path.getCurrentPoint().getX(), (int) path.getCurrentPoint().getY(),
																	   getScaledMousePosition().x, getScaledMousePosition().y, 
																	   endPoint.x, endPoint.y);
					g2.draw(curve);
					
					g2.setColor(Color.BLUE);
					g2.fill(new Ellipse2D.Double(endPoint.x-5, endPoint.y-5, 10, 10));

				}
				
			}
			PathIterator pi = path.getPathIterator(null);
			
			double[] coords = new double[6];
			int i = 0;
			int j = 0;
			boolean done = false;
			
			double minDistance = 20;
			Point selected = new Point(0,0), lineEndSelected = new Point(0, 0);
			
			linesSelected = 0;
			for(; !pi.isDone() && !done; pi.next()) {
				int type = pi.currentSegment(coords);

				if(type == 0) {
					
					g2.setColor(Color.GREEN);
					if((tool == SelectedTool.EDIT || tool == SelectedTool.DEL || tool == SelectedTool.SELECT) && p.getMousePosition() != null) {
						double temp = getScaledMousePosition().distance(coords[0], coords[1]);
						
						if(temp < minDistance){
							minDistance = temp;
							
							if(!dragging)
							curveSelected = i;

							selected = new Point((int) coords[0], (int)coords[1]);
						}
					
					}
					g2.fill(new Ellipse2D.Double(coords[0]-5, coords[1]-5, 10, 10));
					j++;
				}
				
				for(int k = 0; k < type * 2; k+=2) {
					if((tool == SelectedTool.EDIT || tool == SelectedTool.DEL || tool == SelectedTool.SELECT) && p.getMousePosition() != null) {

						
						double temp = getScaledMousePosition().distance(coords[k], coords[k+1]);
						
						if(temp < minDistance){
							minDistance = temp;
							
							if(!dragging) {
								curveSelected = i;
								pointSelected = j;
							}
							selected = new Point((int) coords[k], (int)coords[k+1]);
						}
					
					}
					else if(tool == SelectedTool.SPEED && p.getMousePosition() != null)
					{
						double[] cyeet = new double[6];
						ArrayList<Point> pathPts = new ArrayList<Point>();
						for(PathIterator glagla = path.getPathIterator(null); !glagla.isDone(); glagla.next())
						{
							int t = glagla.currentSegment(cyeet);
							if(cyeet[0] == 0 && cyeet[1] == 0) continue;
							pathPts.add(new Point((int)cyeet[0], (int)cyeet[1]));
						}
						
						double ldist = Integer.MAX_VALUE;
						Point[] lpts = new Point[0];
						int sidx = -1;
					    for(int l = 0; l < pathPts.size() - 1; l++)
					    {
					    	
					    	double d = Line2D.ptSegDist(pathPts.get(l).x, pathPts.get(l).y, pathPts.get(l + 1).x, pathPts.get(l + 1).y, getScaledMousePosition().x, getScaledMousePosition().y);;
					    	if(d < ldist)
					    	{
					    		ldist = d;
					    		lpts = new Point[]{pathPts.get(l), pathPts.get(l + 1)};
					    		sidx = l;
					    	}
					    }
					    
					    if(ldist < minDistance)
					    {
					    	minDistance = ldist;
					    	selected = lpts[0];
					    	lineEndSelected = lpts[1];
					    	g2.setColor(Color.ORANGE);
							g2.drawLine(selected.x, selected.y, lineEndSelected.x, lineEndSelected.y);
							selectedLineIndex = sidx;
							linesSelected++;
					    }
					}
					g2.setColor(Color.BLUE);
					g2.fill(new Ellipse2D.Double(coords[k]-5, coords[k+1]-5, 10, 10));
					j++;
				}
				
				if(i != backwards.length && backwards[i] ) {
					g2.setColor(Color.RED);
					
					int k = type == 0 ? 0 : type * 2 - 2;
					
					int[] xcoords = {(int)coords[k] -1, (int) coords[k], (int)coords[k] + 1};
					int[] ycoords = {(int) (coords[k+1]-1), (int) (coords[k+1] +1), (int) (coords[k+1]-1)};
					g2.drawPolygon(xcoords, ycoords, 3);
				}
				
				if(!commands[i].isEmpty()) {
					g2.setColor(Color.ORANGE);

					int k = type == 0 ? 0 : type * 2 - 2;

					int[] xcoords = {(int)coords[k] -1, (int) coords[k], (int)coords[k] + 1};
					int[] ycoords = {(int) (coords[k+1]-8), (int) (coords[k+1] -10), (int) (coords[k+1]-8)};
					g2.drawPolygon(xcoords, ycoords, 3);


				}
				
				i++;
			}
			
			if(keys.get(KeyEvent.VK_B) && (tool == SelectedTool.ADD || tool == SelectedTool.ADD2)) {
				g2.setColor(Color.RED);
				int[] xcoords = {(int)path.getCurrentPoint().getX() -1, (int) path.getCurrentPoint().getX(), (int)path.getCurrentPoint().getX() + 1};
				int[] ycoords = {(int) (path.getCurrentPoint().getY()-1), (int) (path.getCurrentPoint().getY() +1), (int) (path.getCurrentPoint().getY()-1)};
				g2.drawPolygon(xcoords, ycoords, 3);
			}
			
			if(minDistance < 20) {
				g2.setColor(tool == SelectedTool.EDIT ? Color.CYAN: Color.RED);
				
				if (tool == SelectedTool.SELECT) {
					g2.setColor(Color.ORANGE);
				}
				
				g2.draw(new Ellipse2D.Double(selected.x-6, selected.y-6, 12, 12));
			}
			else if (!dragging){
				curveSelected = -1;
				pointSelected = -1;
			}
		}
		
		public Dimension getPreferredSize() {
			return new Dimension((int) (field.getWidth() * zoom), (int) (field.getHeight() * zoom));
		}
		
		
	};
	
	BufferedImage field;
	Path2D.Double path = new Path2D.Double();
	
	public BuildAnAuton2() {
		
		try {

		URL ImageURL = BuildAnAuton2.class.getResource("field.png");
		if(ImageURL != null) {
				field = ImageIO.read(ImageURL);
			

		}
		else{
			field = ImageIO.read(new File("field.png"));

		}

		}
		catch (IOException e) {
			e.printStackTrace();

		}

		
		path.moveTo(field.getWidth()/2, field.getHeight()/2);

		this.setLayout(new BorderLayout());

		keys.put(KeyEvent.VK_B, false);
		keys.put(KeyEvent.VK_SHIFT, false);
		
		inchPerPixel = 650.22/field.getWidth(); //216 for garage
		
		scrollPane.setViewportView(p);
		scrollPane.setPreferredSize(new Dimension(field.getWidth()+4, field.getHeight()+4));
//		p.setPreferredSize(new Dimension(field.getWidth(), field.getHeight()));
		
		scrollPane.addMouseListener(this);
		
		toolbar.add(add);
		//Disabled because curves aren't accurate.
		toolbar.add(add2);
		toolbar.add(edit);
		toolbar.add(select);
		toolbar.add(delete);
		toolbar.add(restart);
		toolbar.add(speed);
				
		file.add(save);
		file.add(load);
		file.add(export);
		menu.add(file);
		
		settings.add(setDefaultSpeed);
		
		snapToPoints.setSelected(true);
		settings.add(snapToPoints);
		settings.add(setInitialAngle);
		menu.add(settings);
		
		this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		
		fs.setFileFilter(new FileNameExtensionFilter("Auton", "aut"));
		
		speeds.add(defaultSpeed);
		
		add.addActionListener((ActionEvent e) -> {
			for(JButton b: tools) {
				b.setEnabled(true);
			}
			add.setEnabled(false);
			tool = SelectedTool.ADD;
			Thread t = new Thread(() ->{
				while(tool == SelectedTool.ADD) {
					p.repaint();
					try {
						Thread.sleep(20);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			t.start();

		});
		
		add2.addActionListener((ActionEvent e) -> {
			for(JButton b: tools) {
				b.setEnabled(true);
			}
			add2.setEnabled(false);
			tool = SelectedTool.ADD2;
			addStep = 2;
			Thread t = new Thread(() ->{
				while(tool == SelectedTool.ADD2) {
					p.repaint();
					try {
						Thread.sleep(20);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			t.start();
		});
		
		edit.addActionListener((ActionEvent e) -> {
			for(JButton b: tools) {
				b.setEnabled(true);
			}
			edit.setEnabled(false);
			tool = SelectedTool.EDIT;
			Thread t = new Thread(() ->{
				while(tool == SelectedTool.EDIT) {
					if(dragging && p.getMousePosition() != null) {
						int i = 1;
						int j = 1;
						double[] coords = new double[6];
						Path2D.Double temp = new Path2D.Double();
						PathIterator pi = path.getPathIterator(null);
						
						pi.currentSegment(coords);
						if(curveSelected == 0) {
							temp.moveTo(getScaledMousePosition().x, getScaledMousePosition().y);
						}
						else {
							temp.moveTo(coords[0], coords[1]);
						}
						pi.next();
						for(; !pi.isDone(); pi.next()) {
							int type = pi.currentSegment(coords);
							for(int k = 0; k < type * 2; k+=2) {
								if(pointSelected == j)  {
									coords[k] = getScaledMousePosition().x;
									coords[k+1] = getScaledMousePosition().y;
								}
								j++;
							}
							switch(type) {
							case 1:
								temp.lineTo(coords[0], coords[1]);
								break;
							case 2:
								temp.quadTo(coords[0], coords[1], coords[2], coords[3]);
								break;
							case 3:
								temp.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
								break;
							}
							
							i++;
						}
						path = temp;
						
					}
					p.repaint();

					try {
						Thread.sleep(20);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			t.start();
		});
		
		select.addActionListener((ActionEvent e) -> {
			for(JButton b: tools) {
				b.setEnabled(true);
			}
			select.setEnabled(false);
			Thread t = new Thread(() ->{
				while(tool == SelectedTool.SELECT) {
					p.repaint();
					try {
						Thread.sleep(20);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			t.start();
			tool = SelectedTool.SELECT;

		});
		
		delete.addActionListener((ActionEvent e) -> {
			for(JButton b: tools) {
				b.setEnabled(true);
			}
			delete.setEnabled(false);
			Thread t = new Thread(() ->{
				while(tool == SelectedTool.DEL) {
					p.repaint();
					try {
						Thread.sleep(20);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			t.start();
			tool = SelectedTool.DEL;
		});
		restart.addActionListener((ActionEvent e) -> {
			for(JButton b: tools) {
				b.setEnabled(true);
			}
			backwards = new boolean[0];
			speeds = new ArrayList<Double>();
			commands = new CommandSet[1];
			commands[0] = new CommandSet();

			tool = SelectedTool.NONE;
			path.reset();
			path.moveTo(field.getWidth()/2, field.getHeight()/2);
			p.repaint();
			
		});
		speed.addActionListener((ActionEvent e) -> {
			for(JButton b: tools) {
				b.setEnabled(true);
			}
			speed.setEnabled(false);
			Thread t = new Thread(() ->{
				while(tool == SelectedTool.SPEED) {
					p.repaint();
					try {
						Thread.sleep(20);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			t.start();
			tool = SelectedTool.SPEED;
		});


		export.addActionListener((ActionEvent e) -> {		
			/*double speed = java.lang.Double.parseDouble(JOptionPane.showInputDialog("Enter a speed between 0.0 and 1.0"));
			
			for(int i = 0; i < speeds.size(); i++) {
				speeds.set(i, speed > 1 ? 1.0 : speed); 
				speeds.set(i, speed <= 0 ? 0 : speed); 
			}*/
			
			Export exporter = new Export(path.getPathIterator(null), inchPerPixel, backwards, commands, true, speeds.toArray(new Double[0]), initialAngle);
			exporter.export();

		});
		save.addActionListener((ActionEvent e)  -> {
			fs.showSaveDialog(this);
			File file = fs.getSelectedFile();
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
				oos.writeObject(backwards);
				oos.writeObject(speeds);
				oos.writeObject(commands);
				oos.writeObject(path);
				oos.close();
			} catch (Exception e1) {
				e1.printStackTrace();

			}
		});
		
		load.addActionListener((ActionEvent e)  -> {
			fs.showOpenDialog(this);
			File file = fs.getSelectedFile();
			try {
				System.out.println(file.getName());
				ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
				backwards = (boolean[]) ois.readObject();
				speeds = (ArrayList<Double>) ois.readObject();
				commands = (CommandSet[]) ois.readObject();
				path = (Path2D.Double) ois.readObject();
				ois.close();
				p.repaint();
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		
		setDefaultSpeed.addActionListener((ActionEvent e)  -> {
			String input = JOptionPane.showInputDialog(null, "Default Speed (-1 to 1): ", defaultSpeed);
			if(input == null || input == "") return;
			double val = Double.parseDouble(input);
			if(val < -1.0) val = -1.0;
			else if(val > 1.0) val = 1.0;
			
			for(int i = 0; i < speeds.size(); i++)
				if(speeds.get(i) == defaultSpeed)
					speeds.set(i, val);
			
			defaultSpeed = val;
		});
		
		setInitialAngle.addActionListener((ActionEvent e)  -> {
			String input = JOptionPane.showInputDialog(null, "Default Initial Angle: ", initialAngle);
			if(input == null || input == "") return;
			initialAngle = 360 - Double.parseDouble(input);
		});
		
		for(JButton b: tools) {
			b.setFocusPainted(false);
			b.setFocusable(false);

		}
		
		
		prompt.addActionListener((ActionEvent e) -> {
			prompt.setText("Prompt not supported yet");
		});
		
		//Should probably change to bindings
		KeyListener k = new ToggleListener(this, keys);
		this.addKeyListener(k); 
		
		
		
		scrollPane.requestFocusInWindow();
		
		commands[0] = new CommandSet();
		
		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());
		
		top.add(toolbar, BorderLayout.SOUTH);
		top.add(menu, BorderLayout.NORTH);
		
		add(top, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
//		add(prompt, BorderLayout.SOUTH);
	}
	
	public void mousePressed(MouseEvent e) {
		if(tool == SelectedTool.ADD) {

			if(keys.get(KeyEvent.VK_SHIFT).booleanValue()) {
				int mouseX = getScaledMousePosition().x;
				int mouseY = getScaledMousePosition().y;
				int pX = (int) path.getCurrentPoint().getX();
				int pY = (int) path.getCurrentPoint().getY();
				double angle = Math.atan2(mouseY-pY, mouseX-pX)*180.0/Math.PI;
				if(angle < 0) angle += 360;

				double snappedAngle = Math.round(angle/45.0)*45.0;
				if(snappedAngle == 0 || snappedAngle == 180) 
					path.lineTo(mouseX, pY);
				else if(snappedAngle == 90 || snappedAngle == 270)
				{
					path.lineTo(pX, mouseY);
				}
				else { 
					double magnitude = Math.sqrt((pX-mouseX) * (pX-mouseX) + (pY - mouseY) * (pY - mouseY)); //Find distance from end of current path to mouse
					double angleRad = snappedAngle * Math.PI/180.0;
					path.lineTo(pX + Math.cos(angleRad)*magnitude, pY + Math.sin(angleRad) * magnitude);
				}
				speeds.add(defaultSpeed);

			}
			else {
				if(snapToPoints.getState())
				{
					double[] cyeet = new double[6];
					ArrayList<Point> pathPts = new ArrayList<Point>();
					int idx = -1;
					double dist = 20;
					for(PathIterator glagla = path.getPathIterator(null); !glagla.isDone(); glagla.next())
					{
						int t = glagla.currentSegment(cyeet);
						if(cyeet[0] == 0 && cyeet[1] == 0) continue;
						pathPts.add(new Point((int)cyeet[0], (int)cyeet[1]));
						if(new Point((int)cyeet[0], (int)cyeet[1]).distance(getScaledMousePosition()) <= dist)
						{
							idx = pathPts.size() - 1;
							dist = new Point((int)cyeet[0], (int)cyeet[1]).distance(getScaledMousePosition());
						}
					}
					if(idx == -1)
						path.lineTo(getScaledMousePosition().x, getScaledMousePosition().y);
					else
						path.lineTo(pathPts.get(idx).x, pathPts.get(idx).y);
					}
				else
					path.lineTo(getScaledMousePosition().x, getScaledMousePosition().y);
				speeds.add(defaultSpeed);
			}		
			
			backwards = Arrays.copyOf(backwards, backwards.length+1);
			//speeds = Arrays.copyOf(speeds, speeds.size()+1);
			commands = Arrays.copyOf(commands, commands.length+1);
			
			backwards[backwards.length-1] = keys.get(KeyEvent.VK_B);
			commands[commands.length-1] = new CommandSet();
			
			p.repaint();
		}
		
		else if(tool == SelectedTool.ADD2) {
			if(addStep == 2) {
				endPoint = getScaledMousePosition();
				
				addStep--;
			}
			else if(addStep == 1) {
				
				int ctrlX = getScaledMousePosition().x;
				int ctrlY = getScaledMousePosition().y;
				
				
				//Later
//				java.awt.Robot r;
//				
//				try {
//					r = new java.awt.Robot();
//					r.mouseMove();
//
//				}
//				catch (AWTException e1) {
//					e1.printStackTrace();
//				}
				
				path.quadTo(ctrlX, ctrlY, endPoint.x, endPoint.y);
				backwards = Arrays.copyOf(backwards, backwards.length+1);
				//speeds = Arrays.copyOf(speeds, speeds.length+1);
				commands = Arrays.copyOf(commands, backwards.length+1);

				backwards[backwards.length-1] = keys.get(KeyEvent.VK_B);
				commands[commands.length-1] = new CommandSet();
				
				addStep--;
				tool = SelectedTool.NONE;
				add2.setEnabled(true);
				
			}
			p.repaint();
		}
		else if(tool == SelectedTool.EDIT) {
			
			if(keys.get(KeyEvent.VK_B) && curveSelected != backwards.length && curveSelected != -1) {
				backwards[curveSelected] = !backwards[curveSelected];
			}
			else {
				dragging = true;

			}


		}
		
		else if(tool == SelectedTool.SELECT && curveSelected >= 0) {
			if(cmdEditor != null)
				cmdEditor.dispose();
			cmdEditor = new CommandEditor();
			cmdEditor.setVisible(true);
			cmdEditor.load(commands[curveSelected]);
		}
		
		else if(tool == SelectedTool.DEL && curveSelected > 0) {
			double[] coords = new double[6];
			
			Path2D.Double tempPath = new Path2D.Double();
			boolean[] tempBackwards = new boolean[backwards.length-1];
			double[] tempSpeeds = new double[speeds.size()-1];

			PathIterator pi = path.getPathIterator(null);
			pi.currentSegment(coords);
			tempPath.moveTo(coords[0], coords[1]);
			pi.next();

			int i = 1;
			int count = 0;

						
			for(; !pi.isDone(); pi.next()) {
				pi.currentSegment(coords);
				if(curveSelected == i) {
					if(i!=backwards.length) {
						backwards[i] = false;
					}
				}
				else {
					tempPath.lineTo(coords[0], coords[1]);
					tempBackwards[count] = backwards[i-1];
					tempSpeeds[count] = speeds.get(i - 1);
					count++;
				}

				i++;
			}
			curveSelected = -1;
			

			
			backwards = tempBackwards;
			speeds = new ArrayList<Double>();
			for(int l = 0; l < tempSpeeds.length; l++)
				speeds.add(tempSpeeds[l]);
			path = tempPath;
			p.repaint();
		}
		else if(tool == SelectedTool.SPEED && selectedLineIndex != -1 && linesSelected > 0)
		{
			String input = JOptionPane.showInputDialog(null, "Speed (-1 to 1): ", speeds.get(selectedLineIndex));
			if(input == null || input == "") return;
			double val = Double.parseDouble(input);
			if(val < -1.0) val = -1.0;
			else if(val > 1.0) val = 1.0;
			
			speeds.set(selectedLineIndex, val);
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		
	}
	public void mouseReleased(MouseEvent e) {
		if(tool == SelectedTool.EDIT) {
			dragging = false;
			
		}
	}
	public void mouseEntered(MouseEvent e) {
		prompt.setFocusable(false);
		this.requestFocus();
		prompt.revalidate();
	}
	public void mouseExited(MouseEvent e) {
		prompt.setFocusable(true);
		prompt.revalidate();
	}
		
	public static void main(String[] args) {
		BuildAnAuton2 f = new BuildAnAuton2();
		f.pack();
		f.setVisible(true);	
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void zoom(double deltaZ) {
		if(zoom + deltaZ >= .25 && zoom + deltaZ <= 4.0) {
			zoom += deltaZ;
		}

//		if(zoom > 1)
//			scrollPane.getViewport().setViewPosition(new Point((int)((zoom - 1) * field.getWidth())/2, (int) ((zoom - 1) * field.getHeight())/2));
//		
		p.repaint();
		p.revalidate();
		scrollPane.revalidate();
		scrollPane.repaint();
	}
	
	private Point getScaledMousePosition() {
		int x = 0, y = 0;
		try
		{
			x = p.getMousePosition().x;
			y = p.getMousePosition().y;
		}
		catch (Exception e) { return new Point(x, y); }
		
		if(zoom < 1) {
		x += ((zoom - 1) * field.getWidth())/2;
		y += ((zoom - 1) * field.getHeight())/2;
		}
		x /= zoom;
		y /= zoom;
		return new Point(x, y);
		
	}
}
