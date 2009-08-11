package info.svitkine.alexei.wage;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

public class SceneViewer extends JPanel {
	private static TexturePaint[] patterns;
	static {
		try {
			int numPatterns = 29;
			patterns = new TexturePaint[numPatterns];
			for (int i = 1; i <= numPatterns; i++) {
				BufferedImage image = ImageIO.read(new File("patterns/" + i + ".png"));
				patterns[i - 1] = new TexturePaint(image, new Rectangle(0, 0, image.getWidth(), image.getHeight()));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Scene scene;

	public SceneViewer() {
		setOpaque(false);
	}
	
	public Scene getScene() {
		return scene;
	}
	
	public void setScene(Scene scene) {
		this.scene = scene;
		Border border = getBorder();
		if (border instanceof WindowBorder) {
			WindowBorder wb = (WindowBorder) border;
			if (scene == null)
				wb.setTitle(null);
			else
				wb.setTitle(scene.getName());
		}
	}

	@Override
	public void paint(Graphics g) {
		if (scene != null) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setClip(2, 2, getWidth()-4, getHeight()-4);
			g2d.translate(2, 2);
			g2d.setColor(Color.WHITE);
			g2d.fillRect(2, 2, getWidth()-4, getHeight()-4);
			scene.getDesign().paint(g2d, patterns);
			for (Obj o : scene.getObjs())
				o.getDesign().paint(g2d, patterns);
			for (Chr c : scene.getChrs())
				c.getDesign().paint(g2d, patterns);
			g2d.translate(-2, -2);
			g2d.setClip(null);
		}
		super.paint(g);
	}

	public Object getClickTarget(MouseEvent e) {
		for (int i = scene.getObjs().size() - 1; i >= 0; i--) {
			Obj o = scene.getObjs().get(i);
			if (o.getDesign().isPointOpaque(e.getX(), e.getY())) {
				return o;
			}
		}
		for (int i = scene.getChrs().size() - 1; i >= 0; i--) {
			Chr c = scene.getChrs().get(i);
			if (c.getDesign().isPointOpaque(e.getX(), e.getY())) {
				return c;
			}
		}
		System.err.println("Clicked nothing!");
		return null;
	}
}
