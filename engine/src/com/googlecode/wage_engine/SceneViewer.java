package com.googlecode.wage_engine;

import java.awt.*;
import java.util.List;

import javax.swing.border.Border;

import com.googlecode.wage_engine.engine.Chr;
import com.googlecode.wage_engine.engine.Obj;
import com.googlecode.wage_engine.engine.Scene;

public class SceneViewer extends WComponent {
	private DesignRenderer renderer;
	private Scene scene;
	private Object lock;

	public SceneViewer(DesignRenderer renderer, Object lock) {
		this.renderer = renderer;
		this.lock = lock;
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
			renderer.paintDesign(scene.getDesign(), g2d);
			synchronized (lock) {
				for (Obj o : scene.getState().getObjs())
					renderer.paintDesign(o.getDesign(), g2d);
				for (Chr c : scene.getState().getChrs())
					if (!c.isPlayerCharacter())
						renderer.paintDesign(c.getDesign(), g2d);
			}
			g2d.translate(-2, -2);
			g2d.setClip(null);
		}
		super.paint(g);
	}

	public Object getClickTarget(int x, int y) {
		List<Obj> objs = scene.getState().getObjs();
		for (int i = objs.size() - 1; i >= 0; i--) {
			Obj o = objs.get(i);
			if (renderer.isPointOpaque(o.getDesign(), x, y)) {
				return o;
			}
		}
		List<Chr> chrs = scene.getState().getChrs();
		for (int i = chrs.size() - 1; i >= 0; i--) {
			Chr c = chrs.get(i);
			if (renderer.isPointOpaque(c.getDesign(), x, y)) {
				return c;
			}
		}
		return null;
	}
}
