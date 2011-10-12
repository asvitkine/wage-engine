package info.svitkine.alexei.wage;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

import javax.swing.JComponent;

public class MenuBarRenderer extends JComponent implements MouseListener, MouseMotionListener {
	private static final int HEIGHT = 19;
	private static final int PADDING = 6;
	private static final int ITEM_HEIGHT = 19;

	private MenuBar menubar;
	private int[] offsets;
	private int[] spans;
	private int pressedMenu;
	private int pressedItem;
	
	public MenuBarRenderer(MenuBar menubar) {
		this.menubar = menubar;
		Font f = new Font("Chicago", Font.PLAIN, 13); 
		setFont(f);
		FontMetrics m = getFontMetrics(f);
		int menus = menubar.getMenuCount();
		offsets = new int[menus];
		spans = new int[offsets.length];
		int x = 20;
		for (int i = 0; i < menubar.getMenuCount(); i++) {
			Menu menu = menubar.getMenu(i);
			spans[i] = m.stringWidth(menu.getName());
			offsets[i] = x;
			x += spans[i] + 12;
		}
		pressedMenu = -1;
		pressedItem = -1;
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	private String getAcceleratorString(MenuItem item) {
		char accelerator = item.getShortcut();
		String text = null;
		if (accelerator != 0) {
			text = "      \u2318";
			text += accelerator;
		}
		return text;
	}
	
	private int calculateMenuWidth(Menu menu) {
		int maxWidth = 0;
		Font f = getFont();
		for (int j = 0; j < menu.getItemCount(); j++) {
			MenuItem item = menu.getItem(j);
			if (item != null) {
				f = new Font(f.getFamily(), item.getFontStyle(), f.getSize());
				FontMetrics m = getFontMetrics(f);
				String text = item.getText();
				String acceleratorText = getAcceleratorString(item);
				if (acceleratorText != null) {
					text += acceleratorText;
				}
				int width = m.stringWidth(text);
				if (width > maxWidth) {
					maxWidth = width;
				}
			}
		}
		return maxWidth;
	}

	private Rectangle getMenuBounds(int menuIndex) {
		Menu menu = menubar.getMenu(menuIndex);
		// TODO: cache maxWidth
		int maxWidth = calculateMenuWidth(menu);
		int x = offsets[menuIndex] - PADDING;
		int y = HEIGHT;
		int w = maxWidth + PADDING * 3;
		int h = menu.getItemCount() * ITEM_HEIGHT;
		return new Rectangle(x, y, w, h);
	}
	
	private void drawText(Graphics2D g2, String text, Font f, int style, int x, int y) {
		FontRenderContext frc = g2.getFontRenderContext();
		TextLayout tl = new TextLayout(text, f, frc);
		Color c0 = g2.getColor();
		Color c1 = (c0 == Color.BLACK ? Color.WHITE : Color.BLACK);
		if ((style & MenuItem.OUTLINE) != 0) {
			tl.draw(g2, x + 1, y + 1);
			tl.draw(g2, x + 1, y - 1);
			tl.draw(g2, x - 1, y - 1);
			tl.draw(g2, x - 1, y + 1);
			g2.setColor(c1);
		}
		if ((style & MenuItem.SHADOW) != 0) {
			g2.setColor(c0);
			tl.draw(g2, x + 2, y);
			tl.draw(g2, x, y + 2);
			g2.setColor(c1);
		}
		tl.draw(g2, x, y);
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), HEIGHT);
		g.setColor(Color.BLACK);
		g.fillRect(0, HEIGHT, getWidth(), 1);
		Font f = getFont();
		g.setFont(f);
		// TODO: generalize this... have each 'menu' have bounds and a styled string...
		for (int i = 0; i < menubar.getMenuCount(); i++) {
			Menu menu = menubar.getMenu(i);
			g.setColor(Color.BLACK);
			if (pressedMenu == i) {
				g.fillRect(offsets[i] - 6, 1, spans[i] + 12, HEIGHT - 1);
				g.setColor(Color.WHITE);
			}
			g.drawString(menu.getName(), offsets[i], 14);
			if (pressedMenu == i) {
				FontMetrics m = getFontMetrics(f);
				Rectangle bounds = getMenuBounds(i);
				g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
				g.setColor(Color.BLACK);
				g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
				g.drawLine(bounds.x + 3, bounds.y + bounds.height + 1, bounds.x + bounds.width + 1, bounds.y + bounds.height + 1);
				g.drawLine(bounds.x + bounds.width + 1, bounds.y + 3, bounds.x + bounds.width + 1, bounds.y + bounds.height + 1);
				int y = 33;
				for (int j = 0; j < menu.getItemCount(); j++) {
					MenuItem item = menu.getItem(j);
					g.setColor(Color.BLACK);
					if (pressedItem == j) {
						g.fillRect(bounds.x, y - 14, bounds.width, ITEM_HEIGHT);
						g.setColor(Color.WHITE);
					} else if (item != null && !item.isEnabled()) {
						g.setColor(Color.GRAY);
					}
					if (item != null) {
						Graphics2D g2 = ((Graphics2D)g);
						f = new Font(f.getFamily(), item.getFontStyle(), f.getSize());
						drawText(g2, item.getText(), f, item.getStyle(), offsets[i] + PADDING, y);
						String acceleratorText = getAcceleratorString(item);
						if (acceleratorText != null) {
							int width = m.stringWidth(acceleratorText);
							int x = bounds.x + bounds.width - width - PADDING;
							drawText(g2, acceleratorText, f, item.getStyle(), x, y);						
						}
					} else {
						g.drawLine(bounds.x, y - 7, bounds.x + bounds.width, y - 7);
					}
					y += ITEM_HEIGHT;
				}
			}
		}
	}

	private int getMenuAt(int x, int y) {
		if (y < HEIGHT) {
			for (int i = 0; i < menubar.getMenuCount(); i++) {
				if (x > offsets[i] - 6 && x - offsets[i] < spans[i] + 6) {
					return i;
				}
			}
		}
		return -1;
	}
	
	private int getMenuItemAt(int x, int y, boolean onlySelectable) {
		if (pressedMenu != -1) {
			Rectangle bounds = getMenuBounds(pressedMenu);
			if (bounds.contains(x, y)) {
				int dy = y - bounds.y;
				int itemIndex = dy / ITEM_HEIGHT;
				if (!onlySelectable)
					return itemIndex;
				MenuItem item = menubar.getMenu(pressedMenu).getItem(itemIndex);
				if (item != null && item.isEnabled()) {
					return itemIndex;
				}
			}
		}
		return -1;
	}

	@Override
	public boolean contains(Point p) {
		return contains(p.x, p.y);
	}

	@Override
	public boolean contains(int x, int y) {
		int menuIndex = getMenuAt(x, y);
		if (menuIndex != -1)
			return true;
		int menuItemIndex = getMenuItemAt(x, y, false);
		return menuItemIndex != -1;
	}
	
	public void mousePressed(MouseEvent event) {
		int menuIndex = getMenuAt(event.getX(), event.getY());
		if (menuIndex != -1) {
			if (pressedMenu != menuIndex) {
				pressedMenu = menuIndex;
				if (pressedMenu != -1)
					menubar.getMenu(pressedMenu).willShow();
				repaint();
			}
		}

		int menuItemIndex = getMenuItemAt(event.getX(), event.getY(), true);
		if (pressedItem != menuItemIndex) {
			pressedItem = menuItemIndex;
			repaint();
		}
	}
	
	public void mouseDragged(MouseEvent event) {
		mousePressed(event);
	}

	public void mouseReleased(MouseEvent event) {
		if (pressedMenu != -1 && pressedItem != -1) {
			Menu menu = menubar.getMenu(pressedMenu);
			MenuItem item = menu.getItem(pressedItem);
			item.performAction();
		}
		pressedMenu = -1;
		pressedItem = -1;
		repaint();
	}

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mouseMoved(MouseEvent arg0) {
	}
}
