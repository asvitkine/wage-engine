package info.svitkine.alexei.wage;

import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

public class WindowManager extends JPanel {
	private JComponent modalDialog;

	public WindowManager() {
		setLayout(null);
	}

	public void add(JComponent c) {
		c.setBorder(new WindowBorder());
		// TODO: use chain of events pattern...
		MouseInputListener listener = new CloseBoxListener();
		c.addMouseListener(listener);
		c.addMouseMotionListener(listener);
		listener = new WindowDragListener();
		c.addMouseListener(listener);
		c.addMouseMotionListener(listener);
		super.add(c);
	}
	
	public void remove(JComponent comp) {
		super.remove(comp);
		if (comp == modalDialog) {
			for (int i = 0; i < getComponentCount(); i++)
				getComponent(i).setEnabled(true);
			modalDialog = null;
		}
	}

	public void addModalDialog(JComponent dialog) {
		if (modalDialog != null)
			throw new IllegalArgumentException();
		for (int i = 0; i < getComponentCount(); i++)
			getComponent(i).setEnabled(false);
		add(dialog);
		setComponentZOrder(dialog, 0);
		modalDialog = dialog;
	}
	
	public JComponent getModalDialog() {
		return modalDialog;
	}
	
	private static void repaintShape(JComponent c, Shape s) {
		Rectangle b = s.getBounds();
		c.repaint(b.x, b.y, b.width, b.height);
	}
	
	private static class WindowDragListener extends MouseInputAdapter {
		private Point startPos = null; 
		private Timer scrollTimer;

		@Override
		public void mousePressed(MouseEvent event) {
			Point p = event.getPoint();
			JComponent c = (JComponent) event.getComponent();
			if (!c.isEnabled() || (c.getBorder() instanceof WindowBorder))
				return;
			WindowBorder border = (WindowBorder) c.getBorder();
			Shape[] borderShapes = border.getBorderShapes(c);
			Shape shape = borderShapes[WindowBorder.BORDER_SHAPE];
			if (shape != null && shape.contains(p)) {
				Shape closeBoxShape = borderShapes[WindowBorder.CLOSE_BOX];
				if (closeBoxShape == null || !closeBoxShape.contains(p)) {
					startPos = event.getPoint();
				}
				if (border.isScrollable()) {
					if (borderShapes[WindowBorder.SCROLL_UP].contains(p)) {
						scroll(c, -1);
					} else if (borderShapes[WindowBorder.SCROLL_DOWN].contains(p)) { 
						scroll(c, 1);
					}
				}
			}
		}

		private void scroll(JComponent c, final int amount) {
			while (!(c instanceof JScrollPane)) {
				c = (JComponent) c.getComponent(0);
			}
			final JScrollBar bar = ((JScrollPane) c).getVerticalScrollBar();
			scrollTimer = new Timer(3, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					bar.setValue(bar.getValue() + amount);
				}
			});
			scrollTimer.start();
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			if (startPos != null) { 
				int dx = event.getX() - startPos.x;
				int dy = event.getY() - startPos.y;
				JComponent c = (JComponent) event.getComponent();
				Rectangle bounds = c.getBounds();
				bounds.translate(dx, dy); 
				c.setBounds(bounds);
				Container p = c.getParent();
				p.repaint();
				p.invalidate();
				((JComponent)p).revalidate();
			}
		}

		@Override
		public void mouseReleased(MouseEvent event) { 
			startPos = null;
			if (scrollTimer != null) {
				scrollTimer.stop();
				scrollTimer = null;
			}
		}
	}

	private static class CloseBoxListener extends MouseInputAdapter {
		private boolean clickedInCloseBox;

		private void updateCloseBox(MouseEvent event) {
			if (clickedInCloseBox) {
				JComponent c = (JComponent) event.getComponent();
				WindowBorder border = (WindowBorder) c.getBorder();
				Shape closeBox = border.getBorderShapes(c)[WindowBorder.CLOSE_BOX];
				boolean wasPressed = border.isCloseBoxPressed();
				border.setCloseBoxPressed(closeBox.contains(event.getPoint()));
				if (wasPressed != border.isCloseBoxPressed())
					repaintShape(c, closeBox);
			}			
		}
		
		@Override
		public void mouseMoved(MouseEvent event) {
			updateCloseBox(event);
		}
		
		@Override
		public void mousePressed(MouseEvent event) {
			JComponent c = (JComponent) event.getComponent();
			if (!c.isEnabled())
				return;
			WindowBorder border = (WindowBorder) c.getBorder();
			Shape closeBox = border.getBorderShapes(c)[WindowBorder.CLOSE_BOX];
			System.out.println("CB="+closeBox.toString());
			System.out.println("Clicked="+event.getPoint().toString());
			System.out.println("Bounds="+event.getComponent().getBounds().toString());
			if (closeBox != null && closeBox.contains(event.getPoint())) {
				clickedInCloseBox = true;
				updateCloseBox(event);
			}
		} 

		@Override
		public void mouseDragged(MouseEvent event) {
			updateCloseBox(event);
		} 

		@Override
		public void mouseReleased(MouseEvent event) {
			updateCloseBox(event);
			clickedInCloseBox = false;
			JComponent c = (JComponent) event.getComponent();
			WindowBorder border = (WindowBorder) c.getBorder();
			if (border.isCloseBoxPressed()) {
				Shape closeBox = border.getBorderShapes(c)[WindowBorder.CLOSE_BOX];
				if (closeBox.contains(event.getPoint())) {
					System.out.println("Remove!");
					border.setCloseBoxPressed(false);
					Container p = c.getParent();
					p.remove(c);
					repaintShape((JComponent) p, c.getBounds());
				}	
			}
		} 
	}
}
