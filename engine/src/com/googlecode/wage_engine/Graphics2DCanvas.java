package com.googlecode.wage_engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;

public class Graphics2DCanvas implements Canvas {
	private Graphics2D g2d;

	public Graphics2DCanvas(Graphics2D g2d) {
		this.g2d = g2d;
	}

	public void fillPixel(int x, int y) {
		g2d.drawRect(x, y, 0, 0);
	}

	public void setColor(Color color) {
		g2d.setColor(color);
	}

	public void fill(Area area) {
		g2d.fill(area);
	}

	public void setPaint(Paint paint) {
		g2d.setPaint(paint);
	}

	public void fill(Shape shape) {
		g2d.fill(shape);
	}

	public void fillRect(int x, int y, int width, int height) {
		g2d.fillRect(x, y, width, height);
	}

	public void drawLine(int x1, int y1, int x2, int y2) {
		g2d.drawLine(x1, y1, x2, y2);
	}

	public void fillPolygon(int[] xpoints, int[] ypoints, int npoints) {
		g2d.fillPolygon(xpoints, ypoints, npoints);
	}

	public Stroke getStroke() {
		return g2d.getStroke();
	}

	public void setStroke(Stroke stroke) {
		g2d.setStroke(stroke);
	}
}
