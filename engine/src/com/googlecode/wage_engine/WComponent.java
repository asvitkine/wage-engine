package com.googlecode.wage_engine;

import java.awt.Point;

import javax.swing.JComponent;

public class WComponent extends JComponent {
	public static final int MOUSE_PRESSED = 0;
	public static final int MOUSE_RELEASED = 1;
	public static final int MOUSE_CLICKED = 2;
	public static final int MOUSE_MOVED = 3;
	public static final int MOUSE_DRAGGED = 4;
	public static final int MOUSE_ENTERED = 5;
	public static final int MOUSE_EXITED = 6;

	public WComponent() {
	}

	public boolean contains(Point p) {
		return getBounds().contains(p);
	}

	public boolean contains(int x, int y) {
		return getBounds().contains(x, y);
	}

	public void handleMouseEvent(int type, int x, int y) {	
	}

	public void scroll(int amount) {		
	}
}
