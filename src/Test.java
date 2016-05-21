import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class Test extends JFrame implements ActionListener, MouseListener{
	boolean moving = false;
	int selected = 2;
	
	JScrollPane scrollPane = new JScrollPane();
	
	JComponent p = new JComponent() {
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.BLACK);

			g2.setStroke(new BasicStroke(3));
			g2.drawImage(field, 0, 0, null);
			g2.draw(path);
		
			PathIterator pi = path.getPathIterator(null);
			
			double[] coords = new double[6];
			int i = 0;
			boolean done = false;
			
			g2.setColor(Color.BLUE);
			for(; !pi.isDone() && !done; pi.next()) {
				if(i==selected) {
					done = true;
					int type = pi.currentSegment(coords);

					for(int j = 0; j < type * 2; j+=2) {
						g2.fill(new Ellipse2D.Double(coords[j]-5, coords[j+1]-5, 10, 10));
					}
				}
				i++;
			}

		}
	};
	
	BufferedImage field;
	Path2D.Double path = new Path2D.Double();
	
	JButton next = new JButton("Next");
	JButton previous = new JButton("Prev.");
	JTextField selection = new JTextField(1);
	
	public Test() {
		path.moveTo(200, 200);
		path.lineTo(100, 100);
		path.quadTo(400, 300, 300, 400);
		path.curveTo(400, 100, 600, 400, 800, 50);

		this.setLayout(new BorderLayout());

		try {
			field = ImageIO.read(new File("field.png"));
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		scrollPane.setViewportView(p);
		scrollPane.setPreferredSize(new Dimension(field.getWidth(), field.getHeight()));
		p.setPreferredSize(new Dimension(field.getWidth(), field.getHeight()));

		add(scrollPane, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel();
		
		previous.addActionListener(this);
		next.addActionListener(this);
		scrollPane.addMouseListener(this);
		
		
		selection.setEditable(false);
		
		buttons.add(selection);
		buttons.add(previous);
		buttons.add(next);

		add(buttons, BorderLayout.SOUTH);

	}
	
	public static void main(String[] args) {
		Test f = new Test();
		f.pack();
		f.setVisible(true);	
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == next) {
			selected++;
			path.moveTo(400, 400);
			p.repaint();
			selection.setText(Integer.toString(selected));
		}
		if(e.getSource() == previous) {
			selected--;
			p.repaint();
			selection.setText(Integer.toString(selected));
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		moving = true;
		System.out.println("SADFASDF");
		Thread t = new Thread(new Runnable() {
			public void run() {
				while(moving) {
					
					path = new Path2D.Double();
					path.moveTo(20, 20);
					path.lineTo(80, 80);
					path.quadTo(p.getMousePosition().x, p.getMousePosition().y, 300, 400);
					path.curveTo(400, 100, 600, 400, 800, 50);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					p.repaint();
				}
			}
		});
		t.start();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		moving = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}
}
