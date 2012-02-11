package com.googlecode.wage_engine;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

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
		MouseEventForwarder forwarder = new MouseEventForwarder();
		addMouseListener(forwarder);
		addMouseMotionListener(forwarder);
	}
	
	public void handleMouseEvent(int type, int x, int y) {	
	}

	private class MouseEventForwarder implements MouseListener, MouseMotionListener {		
		public void mousePressed(MouseEvent event) {
			handleMouseEvent(MOUSE_PRESSED, event.getX(), event.getY());
		}
		public void mouseReleased(MouseEvent event) {
			handleMouseEvent(MOUSE_RELEASED, event.getX(), event.getY());
		}
		public void mouseClicked(MouseEvent event) {
			handleMouseEvent(MOUSE_CLICKED, event.getX(), event.getY());
		}
		public void mouseMoved(MouseEvent event) {
			handleMouseEvent(MOUSE_MOVED, event.getX(), event.getY());
		}
		public void mouseDragged(MouseEvent event) {
			handleMouseEvent(MOUSE_DRAGGED, event.getX(), event.getY());
		}
		public void mouseEntered(MouseEvent event) {
			handleMouseEvent(MOUSE_ENTERED, event.getX(), event.getY());
		}
		public void mouseExited(MouseEvent event) {
			handleMouseEvent(MOUSE_EXITED, event.getX(), event.getY());
		}
	}
}
