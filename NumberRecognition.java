import javax.swing.JPanel;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.awt.Dimension;
import java.awt.image.BufferStrategy;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Canvas;
import java.util.Arrays;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import Network.Network;
import Network.NetworkTools;
import Window.Window;
import Window.GamePanel;
import javax.swing.SwingUtilities;

public class NumberRecognition extends GamePanel implements Runnable {
	private int        tile_size;
	private int        sidebar_size;
	private int[]      image_size;
	private Dimension  window_size;

	private boolean    running;
	private Thread     thread;

	private Network    net;

	private double[]   guesses;
	private double[][] image;

	private Listener listener;

	private Font main_font = new Font("SanSerif", Font.PLAIN, 12);
	private Font large_font = new Font("SanSerif", Font.PLAIN, 120);

	public static void main(String[] args) {
		new NumberRecognition();
	}

	public NumberRecognition() {
		tile_size =    10;
		sidebar_size = 100;
		image_size =  new int[]{28, 28};
		window_size =  new Dimension(tile_size * image_size[0] + sidebar_size, tile_size * image_size[1]);

		image = new double[image_size[0]][image_size[1]];
		new Window(window_size, "Number Panel", this);
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
		running = true;
	}

	public void run() {
		/*double[] data = new double[]{};
		for (int i = 0; i < image_size[0]; i ++) {
			for (int j = 0; j < image_size[0]; j ++) {
				image[i][j] = data[i + j * 28];
			}
		}*/

		// add mouselistener
		listener = new Listener(this);
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
		this.addKeyListener(listener);

		// load network
		System.out.print("attempting to load Network ... ");
		try {
			net = Network.load_network("Number_recognition");

		// if no file was found 
		} catch (FileNotFoundException e) {
			System.out.println("No matching network file found.");
			System.exit(0);

		// in case of any other error
		} catch (Exception e) {
			System.out.println("Failed:");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("completed");

		// main loop variables
		long start_time = System.nanoTime();
		long last_time = start_time;
		long updates_per_second = 60;
		long ns_per_update = 1000000000 / updates_per_second;
		
		long timer = System.currentTimeMillis();
		long now;
		long wait_time;

		// main loop
		while (running) {
			now = System.nanoTime();
			last_time = now;

			update();
			render();

			// wait to reach ~1/10 second per update
			wait_time = (ns_per_update - (System.nanoTime() - now)) / 1000000;
			
			if (wait_time > 0) {
				try {
					Thread.sleep(wait_time);
				} catch (InterruptedException e) {}
			}
		}
		stop();
	}

	public synchronized void stop() {
		try {
			thread.join();
			running = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void update() {
		double[] input = new double[image_size[0] * image_size[1]];
		for (int i = 0; i < image_size[0]; i ++) {
			for (int j = 0; j < image_size[1]; j ++) {
				input[i * image_size[1] + j] = image[j][i];
			}
		}
		guesses = net.calculate(input);
	}

	private void render() {

		// get buffer
		BufferStrategy buffer_strategy = this.getBufferStrategy();
		if (buffer_strategy == null) {
			this.createBufferStrategy(3);
			return;
		}

		// get drawing tools
		Graphics2D graphics = (Graphics2D)buffer_strategy.getDrawGraphics();
		
		// draw background
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, window_size.width, window_size.height);

		// draw image
		for (int i = 0; i < image_size[0]; i ++) {
			for (int j = 0; j < image_size[0]; j ++) {
				graphics.setColor(new Color((int)(image[i][j] * 255), (int)(image[i][j] * 255), (int)(image[i][j] * 255)));
				graphics.fillRect(i * tile_size, j * tile_size, tile_size, tile_size);
			}
		}

		// display results
		
		graphics.setColor(Color.black);
		graphics.setFont(large_font);
		FontMetrics metrics = graphics.getFontMetrics();
		graphics.drawString(Integer.toString(NetworkTools.index_of_highest_value(guesses)), image_size[0] * tile_size + 10, metrics.getAscent() - 30);
		graphics.setFont(main_font);
		graphics.drawString(((double)Math.round(guesses[NetworkTools.index_of_highest_value(guesses)] * 10000) / 100) + "%", image_size[0] * tile_size + 10, metrics.getAscent());

		graphics.setFont(main_font);
		metrics = graphics.getFontMetrics();
		int font_height = metrics.getAscent();
		for (int i = 0; i < 10; i ++) {
			String string = i + ": " + ((double)Math.round(guesses[i] * 10000) / 100) + "%";
			graphics.drawString(string, image_size[0] * tile_size + 10, font_height * (i + 12));
		}

		// dispose of the drawing tools
		graphics.dispose();

		// show update the screen
		buffer_strategy.show();
	}

	private void clear() {
		image = new double[image_size[0]][image_size[1]];
	}

	private void draw(int[] position) {
		if (position[0] > image_size[0] * tile_size || position[1] > image_size[1] * tile_size) return;
		int x = position[0] / tile_size;
		int y = position[1] / tile_size;
		double offsetX = ((double) position[0] / tile_size) % 1;
		double offsetY = ((double) position[1] / tile_size) % 1;
		for (int i = -2; i < 3; i++) {
			for (int j = -2; j < 3; j++) {
				try {
					image[x + i][y + j] = Math.max(Math.min((1.0 / 3.0) / Math.sqrt(Math.pow(offsetX + i - .5, 2) + Math.pow(offsetY + j - .5, 2)), 1), image[x + i][y + j]);
				} catch (ArrayIndexOutOfBoundsException e) {}
			}
		}
	}

	private void train(byte number) {
		double[] input = new double[image_size[0] * image_size[1]];
		for (int i = 0; i < image_size[0]; i ++) {
			for (int j = 0; j < image_size[1]; j ++) {
				input[i * image_size[1] + j] = image[j][i];
			}
		}

		double[] output = new double[10];
		output[number] = 1d;
		net.train(input, output, .3);
		System.out.println("succesfully trained Network");
		try {
			net.save_network("Number_recognition");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class Listener implements MouseListener, MouseMotionListener, KeyListener {
		private NumberRecognition panel;

		public Listener(NumberRecognition panel) {
			this.panel = panel;
		}

		public void keyTyped(KeyEvent event) {
			try {
				int pressed = Character.getNumericValue(event.getKeyChar());
				if (pressed >= 0 && pressed <= 9) {
					panel.train((byte) pressed);
				}
			} catch (NumberFormatException e) {}
		}

		public void mouseClicked(MouseEvent event) {
			if (event.getButton() == 3) {
				panel.clear();
			}
		}

		public void mouseDragged(MouseEvent event) {
			if (SwingUtilities.isRightMouseButton(event)) {
				panel.clear();
				return;
			}
			panel.draw(new int[]{event.getX(), event.getY()});
		}

		public void mousePressed(MouseEvent event) {
			if (event.getButton() == 3) return;
			panel.draw(new int[]{event.getX(), event.getY()});
		}
		public void mouseReleased(MouseEvent event) {}
		public void mouseEntered(MouseEvent event) {}
		public void mouseExited(MouseEvent event) {}
		public void mouseMoved(MouseEvent event) {}
		public void keyReleased(KeyEvent event) {}
		public void keyPressed(KeyEvent event) {}
	}
}
