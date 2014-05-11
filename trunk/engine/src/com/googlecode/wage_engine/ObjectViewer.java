package com.googlecode.wage_engine;

import java.awt.*;
import javax.swing.*;

import com.googlecode.wage_engine.engine.Design;

public class ObjectViewer extends JPanel {
	private static final long serialVersionUID = 1L;
	private DesignRenderer renderer;
	private Design design;
	private boolean maskMode;

	public ObjectViewer(DesignRenderer renderer, Design design) {
		this.renderer = renderer;
		this.design = design;
	}

	public void setMaskMode(boolean m) {
		maskMode = m;
	}
	
	public void setDesign(Design design) {
		this.design = design;
	}

	public void paint(Graphics g) {
		if (design != null) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setClip(2, 2, getWidth()-4, getHeight()-4);
			g2d.translate(2, 2);
			if (maskMode) {
				renderer.paintDesignMask(design, g2d);
			} else {
				renderer.paintDesign(design, g2d);
			}
			g2d.translate(-2, -2);
			g2d.setClip(null);
		}
		super.paint(g);
	}

	@Override
	public boolean isOpaque() {
		return false;
	}
}
