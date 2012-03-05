package com.googlecode.wage_engine;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

public class WindowManagerHost extends JComponent {
	private WindowManager wm;

	public WindowManagerHost(WindowManager wm) {
		this.wm = wm;
		setLayout(new BorderLayout());
		add(wm);

		setFocusable(true);

		MouseEventForwarder forwarder = new MouseEventForwarder();
		addMouseListener(forwarder);
		addMouseMotionListener(forwarder);

		addKeyListener(new KeyEventForwarder());
	}
	
	private class KeyEventForwarder implements KeyListener {
		public void keyPressed(KeyEvent event) {
			wm.handleKeyEvent(WComponent.KEY_PRESSED, event.getKeyChar());
		}

		public void keyReleased(KeyEvent event) {
			wm.handleKeyEvent(WComponent.KEY_RELEASED, event.getKeyChar());
		}

		public void keyTyped(KeyEvent event) {
			wm.handleKeyEvent(WComponent.KEY_TYPED, event.getKeyChar());
		}
	}
	
	private class MouseEventForwarder implements MouseListener, MouseMotionListener {		
		public void mousePressed(MouseEvent event) {
			wm.handleMouseEvent(WComponent.MOUSE_PRESSED, event.getX(), event.getY());
		}
		public void mouseReleased(MouseEvent event) {
			wm.handleMouseEvent(WComponent.MOUSE_RELEASED, event.getX(), event.getY());
		}
		public void mouseClicked(MouseEvent event) {
			wm.handleMouseEvent(WComponent.MOUSE_CLICKED, event.getX(), event.getY());
		}
		public void mouseMoved(MouseEvent event) {
			wm.handleMouseEvent(WComponent.MOUSE_MOVED, event.getX(), event.getY());
		}
		public void mouseDragged(MouseEvent event) {
			wm.handleMouseEvent(WComponent.MOUSE_DRAGGED, event.getX(), event.getY());
		}
		public void mouseEntered(MouseEvent event) {
			wm.handleMouseEvent(WComponent.MOUSE_ENTERED, event.getX(), event.getY());
		}
		public void mouseExited(MouseEvent event) {
			wm.handleMouseEvent(WComponent.MOUSE_EXITED, event.getX(), event.getY());
		}
	}
}
