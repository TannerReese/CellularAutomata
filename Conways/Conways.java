import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.BufferStrategy;
import java.awt.geom.*;
import javax.swing.*;

public class Conways extends Canvas implements Runnable{
	JFrame frame;
	Canvas canvas;
	BufferStrategy bufferstrategy;
	CellGui cellgui;
	boolean[][] cells;
	boolean paused;
	private int interval;
	
	public static int WIDTH = 800, HEIGHT = 600;
	public static int PIXEL_WIDTH = 5, PIXEL_HEIGHT = 5;
	
	public Conways(){
		frame = new JFrame("Conway's Game of Life");
		
		JPanel panel = (JPanel) frame.getContentPane();
		panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		panel.setLayout(null);
		
		canvas = new Canvas();
		canvas.setBounds(0, 0, WIDTH, HEIGHT);
		canvas.setIgnoreRepaint(true);
		
		panel.add(canvas);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		
		canvas.createBufferStrategy(2);
		bufferstrategy = canvas.getBufferStrategy();
      
		canvas.requestFocus();
		cellgui = new CellGui(this);
		
		cells = new boolean[WIDTH/PIXEL_WIDTH][HEIGHT/PIXEL_HEIGHT];
		paused = false;
		for(int i=0; i<cells.length; i++){
			for(int j=0; j<cells[0].length; j++){
				cells[i][j] = false;
			}
		}
		interval = 100;
	}
	
	public void render(){
		Graphics2D g = (Graphics2D) bufferstrategy.getDrawGraphics();
		g.setColor(Color.BLACK);
		g.clearRect(0, 0, WIDTH, HEIGHT);
		render(g);
		g.dispose();
		bufferstrategy.show();
	}
	
	public void render(Graphics2D g){
		boolean[][] cells = this.cells;
		int pixW = (int)(WIDTH/cells.length), pixH = (int)(HEIGHT/cells[0].length);
		for(int i=0; i<cells.length; i++){
			for(int j=0; j<cells[0].length; j++){
				if(cells[i][j]){
					g.setColor(Color.WHITE);
				}else{
					g.setColor(Color.BLACK);
				}
				g.fillRect(i*pixW, j*pixH, pixW, pixH);
			}
		}
	}
	public void update(int time){
		boolean[][] edited = new boolean[cells.length][cells[0].length];
		int x, y, c;
		int sizeW = cells.length, sizeH = cells[0].length;
		
		for(int i=0; i<cells.length; i++){
			for(int j=0; j<cells[0].length; j++){
				c = 0;
				for(int g=0; g<3; g++){
					for(int h=0; h<3; h++){
						x = i + g - 1;
						y = j + h - 1;
						
						if(((x < 0) || (x > sizeW - 1)) || ((y < 0) || (y > sizeH - 1)) || ((x == i) && (y == j))){
							
						}else if(cells[x][y]){
							c++;
						}
					}	
				}
				if(c > 3){
					edited[i][j] = false;
				}else if(c == 3){
					edited[i][j] = true;
				}else if(c == 2){
					edited[i][j] = cells[i][j];
				}else if(c < 2){
					edited[i][j] = false;
				}
			}
		}
		cells = edited;
	}
	
	static Thread exThread;
	public static void main(String[] args){
		Conways ex = new Conways();
		exThread = new Thread(ex);
		exThread.start();
	}
	
	long desiredFPS = 60;
    long desiredDeltaLoop = (1000*1000*1000)/desiredFPS;
    
	boolean running = true;
	
	public void run(){
		exec();
	}
	public void halt(){
		running = false;
	}
	public static boolean[][] choiceFillPattern(int sizeW, int sizeH){
		Scanner in = new Scanner(System.in);
		Random rand = new Random();
		System.out.println("Fill Pattern");
		boolean on = true;
		int x, y;
		boolean[][] cs = new boolean[sizeW][sizeH];
		
		for(int i=0; i<cs.length; i++){
			for(int j=0; j<cs[i].length; j++){
				cs[i][j] = false;
			}
		}
		
		switch(in.nextLine().toUpperCase()){
			case "FULL RANDOM": for(int i=0; i<cs.length; i++){
									for(int j=0; j<cs[0].length; j++){
										cs[i][j] = rand.nextBoolean();
									}
								}
					break;
			case "RANDOM": for(int i=0; i<20; i++){
								for(int j=0; j<20; j++){
									x = i + 50;
									y = j + 50;
									
									cs[x][y] = rand.nextBoolean();
								}
							}
					break;
			case "CELLS": while(on){
								try{
									x = in.nextInt();
									y = in.nextInt();
									if(cs[x][y]){
										cs[x][y] = false;
									}else{
										cs[x][y] = true;
									}
									
									System.out.println("Cell "+x+", "+y+" has been changed to "+cs[x][y]);
								}catch(Exception er){
									on = false;
								}
							}
					break;
		}
		return cs;
	}
	public void exec(){
		Random rand = new Random();
		
		cells = Conways.choiceFillPattern(cells.length, cells[0].length);
		
		long endLoopTime;
		long currentUpdateTime = System.nanoTime();
		long lastUpdateTime;
		long deltaLoop;
		long begin = System.currentTimeMillis();
		long now = System.currentTimeMillis();
		long elapsed;
		
		while(running){
			
			render();
			
			now = System.currentTimeMillis();
			elapsed = now - begin;
			if(elapsed > interval){
				if(!paused){
					update(0);
				}
				begin = now;
			}
		}
	}
	public void pause(){
		paused = (!paused);
	}
	public void reset(){
		pause();
		cells = choiceFillPattern(cells.length, cells[0].length);
		pause();
	}
	public void tick(){
		if(!paused){
			paused = true;
		}
		update(0);
	}
	public void changeSpeed(){
		pause();
		Scanner in = new Scanner(System.in);
		interval = in.nextInt();
		pause();
	}

	public class CellGui{
		private Conways conways;
		private JFrame frame;
		private JPanel panel;
		private JButton pause, reset, tick, speed;
		
		public CellGui(Conways conways){
			this.conways = conways;
		
			frame = new JFrame("Conway's GUI");
			frame.setVisible(true);
			frame.setSize(250, 100);
			frame.setResizable(false);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			panel = new JPanel(){
				public void paintComponent(Graphics g){
					g.setColor(Color.BLACK);
					g.fillRect(0, 0, 250, 100);
				}
			};
			
			pause = new JButton("Pause/Play");
			reset = new JButton("Reset");
			tick = new JButton("Tick");
			speed = new JButton("Change Speed");
			
			pause.addActionListener(new ButtonAction(0, conways));
			reset.addActionListener(new ButtonAction(1, conways));
			tick.addActionListener(new ButtonAction(2, conways));
			speed.addActionListener(new ButtonAction(3, conways));
			
			panel.add(pause);
			panel.add(tick);
			panel.add(reset);
			panel.add(speed);
			frame.add(panel);
		}
		private class ButtonAction implements ActionListener{
			private int n;
			private Conways conways;
			
			public ButtonAction(int n, Conways conways){
				this.n = n;
				this.conways = conways;
			}
			public void actionPerformed(ActionEvent e){
				if(n == 0){
					conways.pause();
				}else if(n == 1){
					conways.reset();
				}else if(n == 2){
					conways.tick();
				}else if(n == 3){
					conways.changeSpeed();
				}
			}
		}
	}
}
