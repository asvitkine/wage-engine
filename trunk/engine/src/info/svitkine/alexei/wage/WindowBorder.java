package info.svitkine.alexei.wage;

import java.awt.*;
import java.awt.geom.Area;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.AbstractBorder;

public class WindowBorder extends AbstractBorder {
	private static final long serialVersionUID = -3235368186322519050L;

	public static final int CLOSE_BOX = 0;
	public static final int RESIZE_NW = 1;
	public static final int RESIZE_NE = 2;
	public static final int RESIZE_SE = 3;
	public static final int RESIZE_SW = 4;
	public static final int RESIZE_N = 5;
	public static final int RESIZE_E = 6;
	public static final int RESIZE_S = 7;
	public static final int RESIZE_W = 8;
	public static final int SCROLL_UP = 9;
	public static final int SCROLL_DOWN = 10;
	public static final int BORDER_SHAPE = 11;
	public static final int NUM_LOCATIONS = 12;

	private String title = null;
	private boolean closeable = true;
	private boolean closeBoxPressed = false;
	private boolean scrollable = false;
	private boolean active = true;

	private static void drawBox(Graphics g, int x, int y, int w, int h) {
		g.setColor(Color.WHITE);
		g.fillRect(x, y, w, h);
		g.setColor(Color.BLACK);
		g.drawRect(x, y, w, h);
	}
	
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Color oldColor = g.getColor();

		final int size = 17;
		drawBox(g, x, y, size, size);
		drawBox(g, x+width-size-1, y, size, size);
		drawBox(g, x+width-size-1, y+height-size-1, size, size);
		drawBox(g, x, y+height-size-1, size, size);
		drawBox(g, x + size, y + 2, width - 2*size - 1, size - 4);
		drawBox(g, x + size, y + height - size + 1, width - 2*size - 1, size - 4);
		drawBox(g, x + 2, y + size, size - 4, height - 2*size - 1);
		drawBox(g, x + width - size + 1, y + size, size - 4, height - 2*size-1);

		if (active) {
			g.setColor(Color.BLACK);
			g.fillRect(x + size, y + 5, width - 2*size - 1, 8);
			g.fillRect(x + size, y + height - 13, width - 2*size - 1, 8);
			g.fillRect(x + 5, y + size, 8, height - 2*size - 1);
			if (!scrollable) {
				g.fillRect(x + width - 13, y + size, 8, height - 2*size - 1);
			} else {
				int pixels[][] = new int[][] {
						{0,0,0,0,0,1,1,0,0,0,0,0},
						{0,0,0,0,1,1,1,1,0,0,0,0},
						{0,0,0,1,1,1,1,1,1,0,0,0},
						{0,0,1,1,1,1,1,1,1,1,0,0},
						{0,1,1,1,1,1,1,1,1,1,1,0},
						{1,1,1,1,1,1,1,1,1,1,1,1}};
				final int h = pixels.length;
				final int w = pixels[0].length;
				int x1 = x + width - 15;
				int y1 = y + size + 1;
				for (int yy = 0; yy < h; yy++) {
					for (int xx = 0; xx < w; xx++) {
						if (pixels[yy][xx] != 0) {
							g.drawRect(x1+xx, y1+yy, 0, 0);
						}
					}
				}
				g.fillRect(x + width - 13, y + size + h, 8, height - 2*size - 1 - h*2);
				y1 += height - 2*size - h - 2;
				for (int yy = 0; yy < h; yy++) {
					for (int xx = 0; xx < w; xx++) {
						if (pixels[h-yy-1][xx] != 0) {
							g.drawRect(x1+xx, y1+yy, 0, 0);
						}
					}
				}
			}
			if (closeable) {
				if (closeBoxPressed) {
					g.fillRect(x + 6, y + 6, 6, 6);
				} else {
					drawBox(g, x + 5, y + 5, 7, 7);
				}
			}
		}

		if (title != null) {
			// TODO: This "Chicago" is not faithful to the original one on the Mac.
			Font f = new Font("Chicago", Font.BOLD, 12);
			int w = g.getFontMetrics(f).stringWidth(title) + 6;
			int maxWidth = width - size*2 - 7;
			if (w > maxWidth) {
				w = maxWidth;
			}
			drawBox(g, x + (width - w) / 2, y, w, size);
			g.setFont(f);
			Shape clip = g.getClip();
			g.setClip(x + (width - w) / 2, y, w, size);
			g.drawString(title, x + (width - w) / 2 + 3, y + size - 4);
			g.setClip(clip);
		}

		g.setColor(oldColor);
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(18,18,18,18);
	}

	@Override
	public boolean isBorderOpaque() { 
		return false; 
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame();
				JPanel panel = new JPanel();
				panel.setBorder(new WindowBorder());
				f.setContentPane(panel);
				f.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
				f.setSize(444, 333);
				f.setLocationRelativeTo(null);
				f.setVisible(true);
			}
		});
	}

	private Shape getBorderShape(JComponent c) {
		Area area = new Area();
		final int size = 17;
		int x = 0;
		int y = 0;
		int width = c.getWidth();
		int height = c.getHeight();
		area.add(new Area(new Rectangle(x, y, size, size)));
		area.add(new Area(new Rectangle(x+width-size-1, y, size, size)));
		area.add(new Area(new Rectangle(x+width-size-1, y+height-size-1, size, size)));
		area.add(new Area(new Rectangle(x, y+height-size-1, size, size)));
		area.add(new Area(new Rectangle(x + size, y + 2, width - 2*size - 1, size - 4)));
		area.add(new Area(new Rectangle(x + size, y + height - size + 1, width - 2*size - 1, size - 4)));
		area.add(new Area(new Rectangle(x + 2, y + size, size - 4, height - 2*size - 1)));
		area.add(new Area(new Rectangle(x + width - size + 1, y + size, size - 4, height - 2*size-1)));
		return area;
	}
	
	public Shape[] getBorderShapes(JComponent c) {
		Shape[] shapes = new Shape[NUM_LOCATIONS];
		if (closeable) {
			shapes[CLOSE_BOX] = new Rectangle(5, 5, 9, 9);
		}
		if (scrollable) {
			final int size = 17;
			shapes[SCROLL_UP] = new Rectangle(c.getWidth() - 16, size + 1, 13, 12);
			shapes[SCROLL_DOWN] = new Rectangle(c.getWidth() - 16, c.getHeight() - size - 14, 13, 12);
		}
 		shapes[BORDER_SHAPE] = getBorderShape(c);
		return shapes;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isCloseable() {
		return closeable;
	}

	public void setCloseable(boolean closeable) {
		this.closeable = closeable;
	}

	public boolean isScrollable() {
		return scrollable;
	}

	public void setScrollable(boolean scrollable) {
		this.scrollable = scrollable;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isCloseBoxPressed() {
		return closeBoxPressed;
	}

	public void setCloseBoxPressed(boolean closeBoxPressed) {
		this.closeBoxPressed = closeBoxPressed;
	}
}
