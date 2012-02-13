package com.googlecode.wage_engine;

import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;

import javax.swing.JComponent;

import com.googlecode.wage_engine.engine.MenuBar;

public class WindowManager extends WComponent implements ComponentListener {
	private JComponent modalDialog;
	private MenuBarRenderer menubar;
	private BorderActionsController borderActionsController;

	public WindowManager() {
		borderActionsController = new BorderActionsController();
		setLayout(null);
		addComponentListener(this);
		MouseEventForwarder forwarder = new MouseEventForwarder();
		addMouseListener(forwarder);
		addMouseMotionListener(forwarder);
	}

	public void add(JComponent c) {
		add(c, false);
	}
	
	public void add(JComponent c, boolean scrollable) {
		WindowBorder border = new WindowBorder();
		border.setScrollable(scrollable);
		c.setBorder(border);
		super.add(c);
		setComponentZOrder(c, lowestZOrderForWindow());
	}
	
	public void remove(JComponent comp) {
		super.remove(comp);
		if (comp == modalDialog) {
			for (int i = 0; i < getComponentCount(); i++)
				getComponent(i).setEnabled(true);
			modalDialog = null;
		}
		if (comp == menubar) {
			menubar = null;
		}
	}
	
	public void removeAll() {
		super.removeAll();
		modalDialog = null;
		menubar = null;
	}

	public void addModalDialog(JComponent dialog) {
		if (modalDialog != null)
			throw new IllegalArgumentException();
		for (int i = 0; i < getComponentCount(); i++)
			getComponent(i).setEnabled(false);
		super.add(dialog);
		setComponentZOrder(dialog, 0);
		modalDialog = dialog;
	}
	
	public JComponent getModalDialog() {
		return modalDialog;
	}
	
	private JComponent[] getSortedComponents() {
		JComponent[] components = new JComponent[getComponentCount()];
		for (int i = 0; i < getComponentCount(); i++)
			components[i] = (JComponent) getComponent(i);
		Arrays.sort(components, new Comparator<JComponent>() {
			public int compare(JComponent c1, JComponent c2) {
				return getComponentZOrder(c2) - getComponentZOrder(c1);
			}
		});
		return components;
	}

	@Override
	public void paint(Graphics g) {
		JComponent[] components = getSortedComponents();
		for (JComponent c : components) {
			g.translate(c.getX(), c.getY());
			c.paint(g);
			g.translate(-c.getX(), -c.getY());
		}
	}

	private WComponent findWComponentAt(int x, int y) {
		JComponent[] components = getSortedComponents();
		for (int i = components.length - 1; i >= 0; i--) {
			JComponent c = components[i];
			if (c.contains(x, y)) {
				return (WComponent) c;
			}
		}
		return null;
	}
	
	@Override
	public void handleMouseEvent(int type, int x, int y) {
		if (menubar != null && menubar.isOpen()) {
			menubar.handleMouseEvent(type, x, y);
			return;
		}
		WComponent c = findWComponentAt(x, y);
		if (c != null) {
			if (type == MOUSE_PRESSED) {
				setComponentZOrder(c, lowestZOrderForWindow());
			}
			borderActionsController.setActiveComponent(c);
		}
		boolean handled = borderActionsController.handleMouseEvent(type, x, y);
		if (!handled && c != null) {
			c.handleMouseEvent(type, x - c.getX(), y - c.getY());
		}
	}

	public void setMenuBar(MenuBar menubar) {
		if (this.menubar != null)
			remove(this.menubar);
		if (menubar == null) {
			this.menubar = null;
			return;
		}
		this.menubar = new MenuBarRenderer(menubar);
		super.add(this.menubar);
		this.menubar.setBounds(getBounds());
		setComponentZOrder(this.menubar, lowestZOrderForWindow() - 1);
	}

	private int lowestZOrderForWindow() {
		int z = 0;
		if (menubar != null)
			z++;
		if (modalDialog != null)
			z++;
		return z;
	}

	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void componentResized(ComponentEvent arg0) {
		if (menubar != null) {
			menubar.setBounds(getBounds());
		}
	}

	public void componentShown(ComponentEvent arg0) {
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
