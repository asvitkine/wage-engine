package com.googlecode.wage_engine;

import java.awt.Graphics;
import java.util.*;

import com.googlecode.wage_engine.engine.MenuBar;

public class WindowManager extends WComponent {
	private WComponent modalDialog;
	private MenuBarRenderer menubar;
	private BorderActionsController borderActionsController;
	private ArrayList<WComponent> components;

	public WindowManager() {
		borderActionsController = new BorderActionsController(new BorderActionsController.Callbacks() {
			public void repaint() {
				WindowManager.this.repaint();
				invalidate();
				revalidate();				
			}

			public void close(WComponent c) {
				remove(c);
				WindowManager.this.repaint(c.getBounds());
			}
		});
		components = new ArrayList<WComponent>();
	}

	public void add(WComponent c) {
		add(c, false);
	}
	
	public void add(WComponent c, boolean scrollable) {
		WindowBorder border = new WindowBorder();
		border.setScrollable(scrollable);
		c.setBorder(border);
		c.setParent(this);
		components.add(c);
		repaint();
	}
	
	public void remove(WComponent comp) {
		components.remove(comp);
		comp.setParent(null);
		if (comp == modalDialog) {
			for (int i = 0; i < components.size(); i++)
				components.get(i).setEnabled(true);
			modalDialog = null;
		}
		if (comp == menubar) {
			menubar = null;
		}
		repaint();
	}
	
	public void removeAll() {
		for (WComponent c : components)
			c.setParent(null);
		components.clear();
		modalDialog = null;
		menubar = null;
		repaint();
	}

	public void addModalDialog(WComponent dialog) {
		if (modalDialog != null)
			throw new IllegalArgumentException();
		for (int i = 0; i < components.size(); i++)
			components.get(i).setEnabled(false);
		dialog.setParent(this);
		components.add(0, dialog);
		modalDialog = dialog;
		modalDialog.setFont(getFont());
		repaint();
	}

	public WComponent getModalDialog() {
		return modalDialog;
	}

	@Override
	public void paint(Graphics g) {
		for (int i = components.size() - 1; i >= 0; i--) {
			WComponent c = components.get(i);
			g.translate(c.getX(), c.getY());
			c.paint(g);
			g.translate(-c.getX(), -c.getY());
		}
	}

	private WComponent findWComponentAt(int x, int y) {
		for (int i = 0; i < components.size(); i++) {
			WComponent c = components.get(i);
			if (c.contains(x, y)) {
				return c;
			}
		}
		return null;
	}

	public void setMenuBar(MenuBar menubar) {
		if (this.menubar != null)
			remove(this.menubar);
		if (menubar == null) {
			this.menubar = null;
			return;
		}
		this.menubar = new MenuBarRenderer(menubar);
		this.menubar.setBounds(getBounds());
		this.menubar.setParent(this);
		components.add(lowestZOrderForWindow() - 1, this.menubar);
	}

	private int lowestZOrderForWindow() {
		int z = 0;
		if (menubar != null)
			z++;
		if (modalDialog != null)
			z++;
		return z;
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
				if (c != modalDialog && c != menubar) {
					components.remove(c);
					components.add(lowestZOrderForWindow(), c);
				}
				repaint(c.getBounds());
			}
			borderActionsController.setActiveComponent(c);
		}
		boolean handled = borderActionsController.handleMouseEvent(type, x, y);
		if (!handled && c != null) {
			c.handleMouseEvent(type, x - c.getX(), y - c.getY());
		}
	}

	@Override
	public void handleKeyEvent(int type, char keyChar) {
		for (int i = lowestZOrderForWindow(); i < components.size(); i++) {
			WComponent c = components.get(i);
			if (c.isFocusable()) {
				c.handleKeyEvent(type, keyChar);
				break;
			}
		}
	}
}
