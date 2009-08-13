package info.svitkine.alexei.wage;

import java.awt.*;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.border.Border;

public class SceneViewer extends JPanel {
	private TexturePaint[] patterns;
	private Scene scene;

	public SceneViewer(TexturePaint[] patterns) {
		this.patterns = patterns;
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
				if (!c.isPlayerCharacter())
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
