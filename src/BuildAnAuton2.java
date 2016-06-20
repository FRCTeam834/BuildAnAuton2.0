import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.net.URL;

public class BuildAnAuton2 extends JFrame implements MouseListener{
	
	JToolBar toolbar = new JToolBar();
		JButton add = new JButton("Add");
		JButton add2 = new JButton("Add Curve");
		JButton edit = new JButton("Edit");
		JButton delete = new JButton("Delete");
		JButton restart = new JButton("Restart");

	JButton[] tools = {add, add2, edit, delete, restart};
		
	JFileChooser fs = new JFileChooser();
	
	JMenuBar menu = new JMenuBar();
	JMenu file = new JMenu("File");
		public JMenuItem save = new JMenuItem("Save");
		public JMenuItem load = new JMenuItem("Load");
		public JMenuItem export = new JMenuItem("Export");

	public enum SelectedTool {
		NONE,
		ADD,
		ADD2,
		EDIT,
		DEL;
	}
	SelectedTool tool = SelectedTool.NONE;
	
	boolean dragging = false;
	
	int addStep = 0;
	Point temp;
	
	int curveSelected = -1;
	int pointSelected = -1;
	
	HashMap<Integer, Boolean> keys = new HashMap<>();
	
	boolean[] backwards = new boolean[0];
	
	double inchPerPixel = 0;
	JScrollPane scrollPane = new JScrollPane();

	public double zoom = 1;
	
	JComponent p = new JComponent() {
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
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
			

				if(keys.get(KeyEvent.VK_SHIFT)) {
					int mouseX = getScaledMousePosition().x;
					int mouseY = getScaledMousePosition().y;
					int pX = (int) path.getCurrentPoint().getX();
					int pY = (int) path.getCurrentPoint().getY();
					double angle = Math.atan2(mouseY-pY, mouseX-pX)*180.0/Math.PI;
					if(angle < 45 && angle > -45 || angle >135 ||angle < -135) {
						g2.drawLine(pX, pY, mouseX, pY);
					}
					else if( (angle >= 45 && angle <= 135) ||(angle >= -135 && angle <=-45)) {
						g2.drawLine(pX, pY, pX, mouseY);

					}
				}
				else {					
					g2.drawLine((int) path.getCurrentPoint().getX(), (int) path.getCurrentPoint().getY(), getScaledMousePosition().x, getScaledMousePosition().y);			
				}
			}
			
			if(tool == SelectedTool.ADD2 && p.getMousePosition() != null) {
				if(addStep == 2) {
					g2.drawLine((int) path.getCurrentPoint().getX(), (int) path.getCurrentPoint().getY(), getScaledMousePosition().x, getScaledMousePosition().y);			
				}

				if(addStep == 1) {
					QuadCurve2D.Double curve = new QuadCurve2D.Double((int) path.getCurrentPoint().getX(), (int) path.getCurrentPoint().getY(),
																	   getScaledMousePosition().x, getScaledMousePosition().y, 
																	   temp.x, temp.y);
					g2.draw(curve);
					
					g2.setColor(Color.BLUE);
					g2.fill(new Ellipse2D.Double(temp.x-5, temp.y-5, 10, 10));

				}
				
			}
			PathIterator pi = path.getPathIterator(null);
			
			double[] coords = new double[6];
			int i = 0;
			int j = 0;
			boolean done = false;
			
			
			double minDistance = 20;
			Point selected = new Point(0,0);
			
			
						
			for(; !pi.isDone() && !done; pi.next()) {
				int type = pi.currentSegment(coords);

				if(type == 0) {
					
					g2.setColor(Color.GREEN);
					if((tool == SelectedTool.EDIT || tool == SelectedTool.DEL) && p.getMousePosition() != null) {
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
					g2.setColor(Color.BLUE);
					g2.fill(new Ellipse2D.Double(coords[k]-5, coords[k+1]-5, 10, 10));
					if((tool == SelectedTool.EDIT || tool == SelectedTool.DEL) && p.getMousePosition() != null) {
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
					j++;
				}
				
				if(i != backwards.length && backwards[i] ) {
					g2.setColor(Color.RED);
					
					int k = type == 0 ? 0 : type * 2 - 2;
					
					int[] xcoords = {(int)coords[k] -1, (int) coords[k], (int)coords[k] + 1};
					int[] ycoords = {(int) (coords[k+1]-1), (int) (coords[k+1] +1), (int) (coords[k+1]-1)};
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
		toolbar.add(add2);
		toolbar.add(edit);
		toolbar.add(delete);
		toolbar.add(restart);
				
		file.add(save);
		file.add(load);
		file.add(export);
		menu.add(file);
		
		fs.setFileFilter(new FileNameExtensionFilter("Auton", "aut"));
		
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
			tool = SelectedTool.NONE;
			path.reset();
			path.moveTo(field.getWidth()/2, field.getHeight()/2);
			p.repaint();
			
		});


		export.addActionListener((ActionEvent e) -> {			
			Export.export(Export.convertToCommands(path, inchPerPixel, backwards));
		});
		save.addActionListener((ActionEvent e)  -> {
			fs.showSaveDialog(this);
			File file = fs.getSelectedFile();
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
				oos.writeObject(backwards);
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
				path = (Path2D.Double) ois.readObject();
				ois.close();
				p.repaint();
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		
		for(JButton b: tools) {
			b.setFocusPainted(false);
			b.setFocusable(false);

		}

		this.addKeyListener(new ToggleListener(this, keys));
		scrollPane.requestFocusInWindow();
		
		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());
		
		top.add(toolbar, BorderLayout.SOUTH);
		top.add(menu, BorderLayout.NORTH);
		
		add(top, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);

	}

	public void mousePressed(MouseEvent e) {
		if(tool == SelectedTool.ADD) {

			if(keys.get(KeyEvent.VK_SHIFT).booleanValue()) {
				int mouseX = getScaledMousePosition().x;
				int mouseY = getScaledMousePosition().y;
				int pX = (int) path.getCurrentPoint().getX();
				int pY = (int) path.getCurrentPoint().getY();
				double angle = Math.atan2(mouseY-pY, mouseX-pX)*180.0/Math.PI;
				if(angle < 45 && angle > -45 || angle > 135 || angle < -135) {
					path.lineTo(mouseX, pY);
				}
				else if( (angle >= 45 && angle <= 135) ||(angle >= -135 && angle <=-45)) {
					path.lineTo(pX, mouseY);

				}

			}
			else {					
				path.lineTo(getScaledMousePosition().x, getScaledMousePosition().y);
			}		
			
			backwards = Arrays.copyOf(backwards, backwards.length+1);
			backwards[backwards.length-1] = keys.get(KeyEvent.VK_B);
			p.repaint();
		}
		
		if(tool == SelectedTool.ADD2) {
			if(addStep == 2) {
				temp = getScaledMousePosition();
				
				addStep--;
			}
			else if(addStep == 1) {
				
				path.quadTo(getScaledMousePosition().x, getScaledMousePosition().y, temp.x, temp.y);
				backwards = Arrays.copyOf(backwards, backwards.length+1);
				backwards[backwards.length-1] = keys.get(KeyEvent.VK_B);

				addStep++;
//				tool = SelectedTool.NONE;
//				add2.setEnabled(true);
				
			}
			p.repaint();
		}
		if(tool == SelectedTool.EDIT) {
			
			if(keys.get(KeyEvent.VK_B) && curveSelected != backwards.length && curveSelected != -1) {
				backwards[curveSelected] = !backwards[curveSelected];
			}
			else {
				dragging = true;

			}


		}
		if(tool == SelectedTool.DEL && curveSelected > 0) {
			double[] coords = new double[6];
			
			Path2D.Double tempPath = new Path2D.Double();
			boolean[] tempBackwards = new boolean[backwards.length-1];
			
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

					count++;
				}

				i++;
			}
			curveSelected = -1;
			

			
			backwards = tempBackwards;
			path = tempPath;
			p.repaint();
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
		
	}
	public void mouseExited(MouseEvent e) {
		
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
		int x = p.getMousePosition().x;
		int y = p.getMousePosition().y;
		
		if(zoom < 1) {
		x += ((zoom - 1) * field.getWidth())/2;
		y += ((zoom - 1) * field.getHeight())/2;
		}
		x /= zoom;
		y /= zoom;
		return new Point(x, y);
		
	}
}
