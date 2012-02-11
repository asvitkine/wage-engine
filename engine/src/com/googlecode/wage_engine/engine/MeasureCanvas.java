package com.googlecode.wage_engine.engine;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;

public class MeasureCanvas implements Canvas {
	private Rectangle bounds;

	public MeasureCanvas() {
		bounds = new Rectangle();
	}

	public void fill(Area area) {
		bounds = bounds.union(area.getBounds());
	}

	public void fill(Shape shape) {
		bounds = bounds.union(shape.getBounds());
	}

	public void fillPixel(int x, int y) {
		bounds = bounds.union(new Rectangle(x, y, 1, 1));
	}

	public void setColor(Color color) {
	}

	public void setPaint(Paint paint) {
	}

	public Rectangle getBounds() {
		return new Rectangle(bounds);
	}

	public void drawLine(int x1, int y1, int x2, int y2) {
		int x = Math.min(x1, x2);
		int y = Math.min(y1, y2);
		int w = Math.abs(x1 - x2);
		int h = Math.abs(y1 - y2);
		bounds = bounds.union(new Rectangle(x, y, w, h));
	}

	public void fillPolygon(int[] xpoints, int[] ypoints, int npoints) {
		for (int i = 0; i < npoints; i++) {
			fillPixel(xpoints[i], ypoints[i]);
		}
	}

	public void fillRect(int x, int y, int width, int height) {
		bounds = bounds.union(new Rectangle(x, y, width, height));
	}

	public Stroke getStroke() {
		return null;
	}

	public void setStroke(Stroke oldStroke) {		
	}
}
