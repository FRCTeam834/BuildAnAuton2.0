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
import java.awt.geom.PathIterator;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import java.net.URL;

public class BuildAnAuton2 extends JFrame implements MouseListener, KeyListener{
	
	JToolBar toolbar = new JToolBar();
		JButton add = new JButton("Add");
		JButton edit = new JButton("Edit");
		JButton delete = new JButton("Delete");
		JButton restart = new JButton("Restart");
	
	JButton[] tools = {add, edit, delete, restart};
		
	
	JButton export = new JButton("Export");
	
	private enum SelectedTool {
		NONE,
		ADD,
		EDIT,
		DEL;
	}
	SelectedTool tool = SelectedTool.NONE;
	
	boolean dragging = false;
	int indexSelected = -1;
	
	boolean locked = false;
	
	double inchPerPixel = 0;
	JScrollPane scrollPane = new JScrollPane();
	
	public boolean[] backwards = new boolean[0];
	boolean toggleBackwards = false;
	
	JComponent p = new JComponent() {
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.BLACK);

			g2.setStroke(new BasicStroke(3));
			g2.drawImage(field, 0, 0, null);
			g2.draw(path);
			if(tool == SelectedTool.ADD && p.getMousePosition() != null) {
				if(locked) {
					int mouseX = p.getMousePosition().x;
					int mouseY = p.getMousePosition().y;
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
					g2.drawLine((int) path.getCurrentPoint().getX(), (int) path.getCurrentPoint().getY(), p.getMousePosition().x, p.getMousePosition().y);
				}
				
				
			}
			
			
			PathIterator pi = path.getPathIterator(null);
			
			double[] coords = new double[6];
			int i = 0;
			boolean done = false;
			
			
			double minDistance = 20;
			Point selected = new Point(0,0);
			

			
			for(; !pi.isDone() && !done; pi.next()) {
				int type = pi.currentSegment(coords);
				if(tool == SelectedTool.EDIT || tool == SelectedTool.DEL) {
					if(p.getMousePosition() != null) {
						double temp = p.getMousePosition().distance(coords[0], coords[1]);
						
						if(temp < minDistance){
							minDistance = temp;
							
							if(!dragging)
							indexSelected = i;

							selected = new Point((int) coords[0], (int)coords[1]);
						}
					}
				}
				if(type == 0) {
					
					g2.setColor(Color.GREEN);
					
					g2.fill(new Ellipse2D.Double(coords[0]-5, coords[1]-5, 10, 10));

				}
				
				for(int j = 0; j < type * 2; j+=2) {
					g2.setColor(Color.BLUE);
					g2.fill(new Ellipse2D.Double(coords[j]-5, coords[j+1]-5, 10, 10));
				}
				
				if(i != backwards.length && backwards[i] ) {
					g2.setColor(Color.RED);
					
					int[] xcoords = {(int)coords[0] -1, (int) coords[0], (int)coords[0] + 1};
					int[] ycoords = {(int) (coords[1]-1), (int) (coords[1] +1), (int) (coords[1]-1)};
					g2.drawPolygon(xcoords, ycoords, 3);
				}
				
				
				i++;
			}
			
			if(toggleBackwards && tool == SelectedTool.ADD) {
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
				indexSelected = -1;
			}

		}
	};
	
	BufferedImage field;
	Path2D.Double path = new Path2D.Double();
		
	public BuildAnAuton2() {

		URL ImageURL = BuildAnAuton2.class.getResource("field.png");
		if(ImageURL != null) {
			try {
				field = ImageIO.read(ImageURL);
			} 		
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		path.moveTo(field.getWidth()/2, field.getHeight()/2);

		this.setLayout(new BorderLayout());

		
		inchPerPixel = 650.22/field.getWidth();
		
		scrollPane.setViewportView(p);
		scrollPane.setPreferredSize(new Dimension(field.getWidth()+4, field.getHeight()+4));
		p.setPreferredSize(new Dimension(field.getWidth(), field.getHeight()));
		scrollPane.addMouseListener(this);
		
		toolbar.add(add);
		toolbar.add(edit);
		toolbar.add(delete);
		toolbar.add(restart);
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
						double[] coords = new double[6];
						Path2D.Double temp = new Path2D.Double();
						PathIterator pi = path.getPathIterator(null);
						
						pi.currentSegment(coords);
						if(indexSelected == 0) {
							temp.moveTo(p.getMousePosition().x, p.getMousePosition().y);
						}
						else {
							temp.moveTo(coords[0], coords[1]);
						}
						pi.next();
						for(; !pi.isDone(); pi.next()) {
							pi.currentSegment(coords);
							if(indexSelected == i) {
								temp.lineTo(p.getMousePosition().x, p.getMousePosition().y);
							}
							else {
								temp.lineTo(coords[0], coords[1]);
								
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

		for(JButton b: tools) {
			b.setFocusPainted(false);
			b.setFocusable(false);

		}
		export.setFocusable(false);
		export.setFocusPainted(false);

		export.addActionListener((ActionEvent e) -> {			
			Export.export(Export.convertToCommands(path, inchPerPixel, backwards));
		});
		
		this.addKeyListener(this);
		scrollPane.requestFocusInWindow();
		
		add(toolbar, BorderLayout.PAGE_START);
		add(scrollPane, BorderLayout.CENTER);
		add(export, BorderLayout.SOUTH);

	}

	public void mousePressed(MouseEvent e) {
		if(tool == SelectedTool.ADD) {
			if(locked) {
				int mouseX = p.getMousePosition().x;
				int mouseY = p.getMousePosition().y;
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
				path.lineTo(p.getMousePosition().x, p.getMousePosition().y);
			}		
			
			backwards = Arrays.copyOf(backwards, backwards.length+1);
			backwards[backwards.length-1] = toggleBackwards;
			p.repaint();
		}
		if(tool == SelectedTool.EDIT) {
			
			if(toggleBackwards && indexSelected != backwards.length && indexSelected != -1) {
				backwards[indexSelected] = !backwards[indexSelected];
			}
			else {
				dragging = true;

			}


		}
		if(tool == SelectedTool.DEL && indexSelected > 0) {
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
				if(indexSelected == i) {
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
			indexSelected = -1;
			

			
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


	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			for(JButton b: tools) {
				b.setEnabled(true);
			}
			tool = SelectedTool.NONE;
			p.repaint();
		}
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
			if(tool == SelectedTool.ADD) 
				locked = true;
		}
		if(e.getKeyCode() == KeyEvent.VK_B) {
			if(tool == SelectedTool.ADD || tool == SelectedTool.EDIT) 
				toggleBackwards = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e .getKeyCode() == KeyEvent.VK_SHIFT) {
			locked = false;
		}
		if(e.getKeyCode() == KeyEvent.VK_B) {
			toggleBackwards = false;
		}
		
	}
}
