package info.svitkine.alexei.wage;

import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

public class WindowManager extends JPanel {

	// TODO: z-order!
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

	private static void repaintShape(JComponent c, Shape s) {
		Rectangle b = s.getBounds();
		c.repaint(b.x, b.y, b.width, b.height);
	}
	
	private static class WindowDragListener extends MouseInputAdapter {
		private Point startPos = null; 

		@Override
		public void mousePressed(MouseEvent event) {
			Point p = event.getPoint();
			JComponent c = (JComponent) event.getComponent();
			WindowBorder border = (WindowBorder) c.getBorder();
			Shape shape = border.getBorderShapes(c)[WindowBorder.BORDER_SHAPE];
			if (shape != null && shape.contains(p)) {
				Shape closeBoxShape = border.getBorderShapes(c)[WindowBorder.CLOSE_BOX];
				if (closeBoxShape == null || !closeBoxShape.contains(p)) {
					startPos = event.getPoint();
				}
			}
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
	};
}
