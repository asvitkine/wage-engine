package com.googlecode.wage_engine;

import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JComponent;

public class BorderActionsController {
	private WComponent component;
	private WindowBorder border;
	private boolean clickedInCloseBox;
	private Point dragStartPos;

	public void setActiveComponent(WComponent c) {
		if (dragStartPos == null && !clickedInCloseBox) {
			component = c;
			border = (WindowBorder) c.getBorder();
		}
	}
	
	public boolean handleMouseEvent(int type, int x, int y) {
		if (component == null || border == null)
			return false;

		x -= component.getX();
		y -= component.getY();
		if (dragStartPos != null) {
			handleDrag(type, x, y);
			return true;
		}
	
		Shape[] borderShapes = border.getBorderShapes(component);
		Shape closeBox = borderShapes[WindowBorder.CLOSE_BOX];
		if (clickedInCloseBox) {
			handleCloseBox(closeBox, type, x, y);
			return true;
		}
		if (type == WComponent.MOUSE_PRESSED && closeBox != null && closeBox.contains(x, y)) {
			clickedInCloseBox = true;
			updateCloseBox(closeBox, x, y);
			return true;
		}

		if (type == WComponent.MOUSE_PRESSED && border.isScrollable()) {
			if (borderShapes[WindowBorder.SCROLL_UP].contains(x, y)) {
				component.scroll(-1);
				return true;
			}
			if (borderShapes[WindowBorder.SCROLL_DOWN].contains(x, y)) { 
				component.scroll(1);
				return true;
			}
		}

		if (type == WComponent.MOUSE_PRESSED && borderShapes[WindowBorder.BORDER_SHAPE].contains(x, y)) {
			dragStartPos = new Point(x, y);
			return true;
		}
		
		return false;
	}

	private void handleDrag(int type, int x, int y) {
		if (type == WComponent.MOUSE_DRAGGED && dragStartPos != null) { 
			int dx = x - dragStartPos.x;
			int dy = y - dragStartPos.y;
			Rectangle bounds = component.getBounds();
			bounds.translate(dx, dy); 
			component.setBounds(bounds);
			Container p = component.getParent();
			p.repaint();
			p.invalidate();
			((JComponent)p).revalidate();
			return;
		}

		if (type == WComponent.MOUSE_RELEASED) {
			dragStartPos = null;
		}
	}

	private void updateCloseBox(Shape closeBox, int x, int y) {
		boolean wasPressed = border.isCloseBoxPressed();
		border.setCloseBoxPressed(closeBox.contains(x, y));
		if (wasPressed != border.isCloseBoxPressed())
			repaintShape(component, closeBox);
	}

	private boolean handleCloseBox(Shape closeBox, int type, int x, int y) {
		if (clickedInCloseBox) {
			if (type == WComponent.MOUSE_RELEASED) {
				clickedInCloseBox = false;
				border.setCloseBoxPressed(false);
				if (closeBox.contains(x, y)) {
					Container p = component.getParent();
					p.remove(component);
					repaintShape((JComponent) p, component.getBounds());	
				}
			} else {
				updateCloseBox(closeBox, x, y);
			}
			return true;
		}

		return false;
	}
	
	private static void repaintShape(JComponent c, Shape s) {
		Rectangle b = s.getBounds();
		c.repaint(b.x, b.y, b.width, b.height);
	}
}
