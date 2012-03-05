package com.googlecode.wage_engine;

import java.awt.*;
import java.util.List;

import javax.swing.border.Border;

import com.googlecode.wage_engine.engine.Chr;
import com.googlecode.wage_engine.engine.Obj;
import com.googlecode.wage_engine.engine.Scene;

public class SceneViewer extends WComponent {
	private TexturePaint[] patterns;
	private Scene scene;

	public SceneViewer(TexturePaint[] patterns) {
		this.patterns = patterns;
		setOpaque(false);
		setFocusable(false);
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
			for (Obj o : scene.getState().getObjs())
				o.getDesign().paint(g2d, patterns);
			for (Chr c : scene.getState().getChrs())
				if (!c.isPlayerCharacter())
					c.getDesign().paint(g2d, patterns);
			g2d.translate(-2, -2);
			g2d.setClip(null);
		}
		super.paint(g);
	}

	public Object getClickTarget(int x, int y) {
		List<Obj> objs = scene.getState().getObjs();
		for (int i = objs.size() - 1; i >= 0; i--) {
			Obj o = objs.get(i);
			if (o.getDesign().isPointOpaque(x, y)) {
				return o;
			}
		}
		List<Chr> chrs = scene.getState().getChrs();
		for (int i = chrs.size() - 1; i >= 0; i--) {
			Chr c = chrs.get(i);
			if (c.getDesign().isPointOpaque(x, y)) {
				return c;
			}
		}
		return null;
	}
}
