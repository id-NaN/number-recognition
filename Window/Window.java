package Window;


import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Component;
import javax.swing.JFrame;


public class Window extends Canvas{
	public <Panel extends GamePanel> Window(Dimension size, String title, Panel panel) {
		JFrame frame = new JFrame(title);
		panel.setSize(size);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.add((Component)panel);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		panel.start();
	}
}
