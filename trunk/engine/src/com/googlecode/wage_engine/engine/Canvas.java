package com.googlecode.wage_engine.engine;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;

public interface Canvas {
	public void setColor(Color color);
	public void setPaint(Paint paint);
	public void fill(Area area);
	public void fill(Shape shape);
	public void fillPixel(int x, int y);
	public void fillRect(int i, int j, int width, int height);
	public void fillPolygon(int[] xpoints, int[] ypoints, int npoints);
	public Stroke getStroke();
	public void setStroke(Stroke oldStroke);
	public void drawLine(int i, int j, int k, int l);
}
