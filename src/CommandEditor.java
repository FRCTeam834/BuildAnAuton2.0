import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import basicCommand.DelayCommand;
import basicCommand.MotorCommand;
import basicCommand.WaitCommand;
import visualrobot.Command;

public class CommandEditor extends JFrame implements ActionListener {
	private ArrayList<CommandBlock> commands = new ArrayList<CommandBlock>();
	
	JComponent workArea = new JComponent() {
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.draw(new Line2D.Double(0, this.getHeight()/(numThreads + 1), this.getWidth(), this.getHeight()/(numThreads + 1)));
			for(int j = 0; j < this.getWidth(); j+= 50)
				g2.draw(new Line2D.Double(j, this.getHeight()/(numThreads + 1) -10, j, this.getHeight()/(numThreads+1) + 10));
			
			for(int i = 1; i < numThreads; i++){
				CommandBlock reference = getFromMain(threadStarts[i]);
				
				int start = 0;
				if(reference == null) {
					CommandBlock last = getLastFromMain();
					if(last != null)
						start = last.getHitBox().x + last.WIDTH;
				}
				else
					start = reference.getHitBox().x;
				
				
				g2.draw(new Line2D.Double(start ,(i+1)*this.getHeight()/(numThreads + 1), this.getWidth(), (i+1)*this.getHeight()/(numThreads + 1)));
				
				for(int j = start; j < this.getWidth(); j+= 50)
					g2.draw(new Line2D.Double(j, (i+1)*this.getHeight()/(numThreads + 1) -10, j, (i+1)*this.getHeight()/(numThreads+1) + 10));
			}

			int i = 0;
			for(CommandBlock c:commands) {
				c.paint(g2, i == selected);
				i++;
			}

		}
		
	};
	
	private JScrollPane workAreaPane = new JScrollPane();
	
	private JPanel buttons = new JPanel();
	private JLabel add = new JLabel("Add:");
	private JButton newCommand = new JButton("Command");
	private JButton newThread = new JButton("Thread");
	private JLabel delete = new JLabel("Delete:");
	private JButton delThread = new JButton("Thread");

	private JPanel threadPanel = new JPanel();
	private JTextField[] txtThreadStarts = new JTextField[1];
	
	private int xOffset;
	private int yOffset;
	private int focus = -1;
	private int selected = -1;
	
	private CommandPanel cmdPanel = new CommandPanel();
	private CommandSet cmdSet;
	
	private int snapGap = 30;
	private int numThreads = 1;
	private int[] threadStarts = {0};
	
	private HandleThreadChange threadChangeList = new HandleThreadChange();
	
	public CommandEditor(CommandSet toLoad) {
		
		setLayout(new BorderLayout());
		workAreaPane.setBackground(new Color(240, 240, 240));
		workAreaPane.setViewportView(workArea);
		workAreaPane.getViewport().setBackground(new Color(240, 240, 240));
		workAreaPane.setBackground(new Color(240, 240, 240));
		workAreaPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		workAreaPane.setPreferredSize(new Dimension(500, 350));
		workArea.setPreferredSize(new Dimension(1000, 0));

		buttons.add(add);
		buttons.add(newCommand);
		buttons.add(newThread);
		buttons.add(delete);
		buttons.add(delThread);
		
		newCommand.addActionListener(this);
		newThread.addActionListener(this);
		delThread.addActionListener(this);
		
		threadPanel.setLayout(new GridLayout(1, 1));
		txtThreadStarts[0] = new JTextField(3);
		txtThreadStarts[0].setVisible(false);
		threadPanel.add(txtThreadStarts[0]);
		
		workArea.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				for(int i = commands.size() - 1; i >= 0; i--) {
					if(commands.get(i).getEditPortion().contains(e.getPoint())) {
//						commands.get(i).edit();
						workArea.repaint();	
						workArea.requestFocus();
						selected = i;
						return;
					}
					if(commands.get(i).getDelPortion().contains(e.getPoint())) {
						if(JOptionPane.OK_OPTION==JOptionPane.showConfirmDialog(null, "Delete Command?", "Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE))
							commands.remove(i);
						workArea.repaint();	
						return;
					}
				
				}
			}
			public void mouseEntered(MouseEvent e) {
				
			}
			public void mouseExited(MouseEvent e) {
				
			}
			public void mouseReleased(MouseEvent e) {
				if(focus != -1) {
					int temp = focus;
					focus = -1;

					if(commands.get(temp).getHitBox().x < 0){
						commands.get(temp).setX(0);
					}
					place(temp);
				}
			}
			public void mousePressed(MouseEvent e) {		
				for(int i = commands.size() - 1; i >= 0; i--) {
					Rectangle r = commands.get(i).getDragPortion();
					if(r.contains(e.getPoint())) {
						CommandBlock c = commands.get(i);
						c.unsnap();
						focus = i;
						selected = i;
						cmdPanel.changeType(c.getCommand().getClass());
						
						xOffset = e.getX() - r.x;
						yOffset = e.getY() - r.y -1;//No idea why I had to add a -1
						new Thread(new Move(c)).start();
						break;
					}
				}
			}
			
		}); 

		add(workAreaPane, BorderLayout.CENTER);
		add(threadPanel, BorderLayout.WEST);
		
		JPanel bottom = new JPanel();
		JScrollPane fields = new JScrollPane(cmdPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		bottom.setPreferredSize(new Dimension(0, 80));
		
		
		bottom.setLayout(new BorderLayout());
		bottom.add(buttons, BorderLayout.WEST);
		bottom.add(fields, BorderLayout.EAST);
		
		
		add(bottom, BorderLayout.SOUTH);
		
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		if(toLoad != null) {
			cmdSet = toLoad;
			load(toLoad);
		}
		else {
			cmdSet = new CommandSet();
		}
			
		
		validate();
	}

	public void place(int f) {
		if(f != -1) {
			CommandBlock temp = commands.get(f);
			int xtoswap = temp.getHitBox().x;
			int indexToPlace = 0;
			commands.remove(temp);
			for (int i = 0; i < commands.size(); i++)
				if(xtoswap > commands.get(i).getHitBox().x)
					indexToPlace += 1;
			commands.add(indexToPlace, temp);
			if(f== selected)
			selected = indexToPlace;
		}
		
		
		workArea.repaint();	

	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == newCommand) {
			
			
			//Probably should happen only when a new class is loaded
			Class<?>[] classes = { DelayCommand.class, WaitCommand.class,MotorCommand.class};
			//add custom
			
			
			
			String[] options = { 
				"Choose a Command", 
			};
			
			options = Arrays.copyOf(options, 1 +classes.length);
			for(int i = 1; i < options.length; i++) {
				options[i] = classes[i-1].getName();
			}
			
			
			String choice = (String) JOptionPane.showInputDialog(null, "Choose a command to add", "Choose a command to add", 1, null, options, options[0]);
			
			int index = 0;
			for(String str: options) {
				if(choice.equals(str)) {
					break;
				}
				index++;
			}
			
			
			if(index != 0)
			try {
				commands.add(new CommandBlock((Command) classes[index-1].newInstance(), workAreaPane.getHorizontalScrollBar().getValue(), 0, Color.WHITE, Color.BLACK));
			}
			catch (InstantiationException e1) { e1.printStackTrace();}
			catch (IllegalAccessException e1) { e1.printStackTrace();}
			
			workArea.repaint();	
			this.revalidate();
		}

		else if(e.getSource() == newThread) {
			if(numThreads < 3) {
				int tempStart = Integer.parseInt(JOptionPane.showInputDialog("Enter which command(Integer) to run with"));
				
				numThreads += 1;
				threadStarts = Arrays.copyOf(threadStarts, numThreads);
				txtThreadStarts = Arrays.copyOf(txtThreadStarts, numThreads);
				threadPanel.setLayout(new GridLayout(numThreads , 1));
				txtThreadStarts[numThreads-1] = new JTextField(3);				
				txtThreadStarts[numThreads-1].addActionListener(threadChangeList);
				txtThreadStarts[numThreads-1].setText(Integer.toString(tempStart));
				threadStarts[numThreads-1] = tempStart - 1;
				threadPanel.add(txtThreadStarts[numThreads-1]);
				
				this.revalidate(); 
				this.repaint();
			}
		}
		else if(e.getSource() == delThread) {
			if(numThreads > 1) {
				numThreads -= 1;
				threadPanel.remove(txtThreadStarts[numThreads]);
				threadStarts = Arrays.copyOf(threadStarts, numThreads);
				txtThreadStarts = Arrays.copyOf(txtThreadStarts, numThreads);
				threadPanel.setLayout(new GridLayout(numThreads , 1));
				
				for(CommandBlock c: commands) {
					if(c.getSnapped()== numThreads) {
						c.unsnap();
					}
				}
				
				this.revalidate();
				this.repaint();
			}
			
		}
	}

	public class Move implements Runnable {
		CommandBlock block;
		public Move (CommandBlock c ) {
			block = c;
		}
		
		public void run() {

			while(focus != -1) {
			try{
				Thread.sleep(10);
				int mousex = MouseInfo.getPointerInfo().getLocation().x - workArea.getLocationOnScreen().x;//workArea.getMousePosition().x;
				int mousey = MouseInfo.getPointerInfo().getLocation().y - workArea.getLocationOnScreen().y;//workArea.getMousePosition().y;
				//System.out.println(workArea.getMousePosition().x + ", " + workArea.getMousePosition().y);
				
				
				block.setX(mousex - xOffset);
				
				int y = mousey-yOffset;
				
				if(Math.abs(y + 60 - (workArea.getHeight())/(numThreads + 1))< snapGap) {
					block.snap(0);
					y = workArea.getHeight()/(numThreads+1) - 60;
				}

				for(int i = 1; i < numThreads; i++) {
					CommandBlock reference = getFromMain(threadStarts[i]);
					int start = reference == null ? 0 : reference.getHitBox().x;

					if(Math.abs(y + 60 - ((i+1)* workArea.getHeight())/(numThreads + 1))< snapGap && mousex - xOffset >= start) {
						block.snap(i);
						y = (i+1)* workArea.getHeight()/(numThreads+1) - 60;
					}
				}
				block.setY(y);
				
				
				if(workAreaPane.getViewport().getViewPosition().x +workAreaPane.getViewport().getExtentSize().width - 100  < block.getHitBox().x) {
					if(workAreaPane.getHorizontalScrollBar().getValue() + workAreaPane.getHorizontalScrollBar().getWidth() >= workArea.getPreferredSize().width - 1
							|| workAreaPane.getWidth() > workArea.getPreferredSize().width) {
						workArea.setPreferredSize(new Dimension(workArea.getPreferredSize().width + 1, workArea.getPreferredSize().height) );
					}
					workArea.revalidate();
					workAreaPane.getHorizontalScrollBar().setValue(workAreaPane.getHorizontalScrollBar().getValue() + 1);
					block.setX(commands.get(focus).getHitBox().x + 1);
				}
				else if(workAreaPane.getViewport().getViewPosition().x > block.getHitBox().x) {
					workArea.revalidate();
					workAreaPane.getHorizontalScrollBar().setValue(workAreaPane.getHorizontalScrollBar().getValue() - 1);
					block.setX(commands.get(focus).getHitBox().x - 1);
				}

				workArea.repaint();
				

			}
			catch(Exception e){e.printStackTrace();}
			
			}

		}
	
	}

	public class HandleThreadChange implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			for(int i = 1; i < numThreads; i++) {
				JTextField temp = txtThreadStarts[i];
				if(e.getSource().equals(temp)) {
					int newStart = Integer.parseInt(temp.getText()) - 1;
					threadStarts[i] = newStart;
					workArea.repaint();
				}
			}
		}
		
	}
	
	private CommandBlock getFromMain(int i) {
		int counter = 0;
		for(CommandBlock c: commands)
			if(c.getSnapped() == 0) {
				if(counter == i)
					return c;
				counter++;
			}
		return null;
	}
	
	private CommandBlock getLastFromMain() {
		CommandBlock last = null;
		for(CommandBlock c: commands)
			if(c.getSnapped() == 0)
				last = c;
		return last;
	}
	
	public void load(CommandSet c) {
		ArrayList<ArrayList<Command>> toLoad = c.getCommands();
		
		int snapNum = 0;
		threadStarts = c.getThreadStarts();
		numThreads = threadStarts.length;
		txtThreadStarts = new JTextField[threadStarts.length];

		
		for(ArrayList<Command> thread : toLoad) {
			
			int i = 0;
			for(Command command : thread) {
				int y = (int) ((snapNum+1)* workArea.getHeight()/(numThreads+1) - 60);
				int x = threadStarts[snapNum] * 150 + 30 + i*150;
				
				CommandBlock toAdd = new CommandBlock(command, x, y, Color.WHITE, Color.BLACK);
				toAdd.snap(snapNum);
				commands.add(toAdd);
				
				
			}
			
			txtThreadStarts[snapNum] = new JTextField(2);
			txtThreadStarts[snapNum].setText(Integer.toString(threadStarts[snapNum]));
			snapNum++;
			
		}
		this.repaint();
		
		
	}
	
	public void dispose() {
		super.dispose();
		
		ArrayList<ArrayList<Command>> toExport = new ArrayList<ArrayList<Command>>();
		
		for(int i = 0; i < numThreads; i++) {

			ArrayList<Command> program = new ArrayList<>();
				for(CommandBlock c: commands)
				if (c.getSnapped() == i)
					program.add(c.getCommand());
				
			toExport.add(program);
		}
		
		cmdSet.set(toExport, threadStarts);
	
			
	}
	
//	
//	public static void main(String[] args) {
//		CommandEditor x = new CommandEditor(null);
//		x.pack();
//		x.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		x.setVisible(true);
//
//	}
}
