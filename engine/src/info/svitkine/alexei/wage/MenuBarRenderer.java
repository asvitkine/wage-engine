package info.svitkine.alexei.wage;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class MenuBarRenderer extends JComponent implements MouseListener, MouseMotionListener {
	private static final int HEIGHT = 19;
	private static final int PADDING = 6;
	private static final int ITEM_HEIGHT = 19;

	private JMenuBar menubar;
	private int[] offsets;
	private int[] spans;
	private int pressedMenu;
	private int pressedItem;
	
	public MenuBarRenderer(JMenuBar menubar) {
		this.menubar = menubar;
		Font f = new Font("Chicago", Font.PLAIN, 13); 
		setFont(f);
		FontMetrics m = getFontMetrics(f);
		addMouseListener(this);
		addMouseMotionListener(this);
		int menus = menubar.getMenuCount();
		offsets = new int[menus];
		spans = new int[offsets.length];
		int x = 20;
		for (int i = 0; i < menubar.getMenuCount(); i++) {
			JMenu menu = menubar.getMenu(i);
			spans[i] = m.stringWidth(menu.getText());
			offsets[i] = x;
			x += spans[i] + 12;
		}
		pressedMenu = -1;
		pressedItem = -1;
	}
	
	private String getAcceleratorString(JMenuItem item) {
		KeyStroke accelerator = item.getAccelerator();
		String text = null;
		if (accelerator != null) {
			text = "      \u2318";
			String t = accelerator.toString();
			text += t.charAt(t.length() - 1);
		}
		return text;
	}
	
	private int calculateMenuWidth(JMenu menu, FontMetrics m) {
		int maxWidth = 0;
		for (int j = 0; j < menu.getItemCount(); j++) {
			JMenuItem item = menu.getItem(j);
			if (item != null) {
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
		JMenu menu = menubar.getMenu(menuIndex);
		FontMetrics m = getFontMetrics(getFont());
		// TODO: cache maxWidth
		int maxWidth = calculateMenuWidth(menu, m);
		int x = offsets[menuIndex] - PADDING;
		int y = HEIGHT;
		int w = maxWidth + PADDING * 3;
		int h = menu.getItemCount() * 20;
		return new Rectangle(x, y, w, h);
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
			JMenu menu = menubar.getMenu(i);
			g.setColor(Color.BLACK);
			if (pressedMenu == i) {
				g.fillRect(offsets[i] - 6, 1, spans[i] + 12, HEIGHT - 1);
				g.setColor(Color.WHITE);
			}
			g.drawString(menu.getText(), offsets[i], 14);
			if (pressedMenu == i) {
				FontMetrics m = getFontMetrics(f);
				Rectangle bounds = getMenuBounds(i);
				g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
				g.setColor(Color.BLACK);
				g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
				int y = 33;
				for (int j = 0; j < menu.getItemCount(); j++) {
					JMenuItem item = menu.getItem(j);
					if (pressedItem == j) {
						g.fillRect(bounds.x, y - 14, bounds.width, ITEM_HEIGHT);
						g.setColor(Color.WHITE);
					}
					if (item != null) {
						// TODO: Get menu font style from the menu...
						String text = item.getText();
						g.drawString(text, offsets[i] + PADDING, y);
						String acceleratorText = getAcceleratorString(item);
						if (acceleratorText != null) {
							int width = m.stringWidth(acceleratorText);
							g.drawString(acceleratorText, bounds.x + bounds.width - width - PADDING, y);							
						}
					} else {
						g.drawLine(bounds.x, y - 7, bounds.x + bounds.width, y - 7);
					}
					if (pressedItem == j)
						g.setColor(Color.BLACK);
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
	
	private int getMenuItemAt(int x, int y) {
		if (pressedMenu != -1) {
			Rectangle bounds = getMenuBounds(pressedMenu);
			if (bounds.contains(x, y)) {
				int dy = y - bounds.y;
				return dy / ITEM_HEIGHT;
			}
		}
		return -1;
	}
	
	public void mousePressed(MouseEvent event) {
		int menuIndex = getMenuAt(event.getX(), event.getY());
		if (menuIndex != -1) {
			if (pressedMenu != menuIndex) {
				if (pressedMenu != -1)
					menubar.getMenu(pressedMenu).setSelected(false);
				pressedMenu = menuIndex;
				if (pressedMenu != -1)
					menubar.getMenu(pressedMenu).setSelected(true);
				repaint();
			}
			return;
		}

		int menuItemIndex = getMenuItemAt(event.getX(), event.getY());
		if (pressedItem != menuItemIndex) {
			pressedItem = menuItemIndex;
			repaint();
		}
		
		// TODO: Forward mouse event to the game...
	}

	public void mouseDragged(MouseEvent event) {
		mousePressed(event);
	}
	
	public void mouseReleased(MouseEvent event) {
		if (pressedMenu != -1 && pressedItem != -1) {
			JMenu menu = menubar.getMenu(pressedMenu);
			JMenuItem item = menu.getItem(pressedItem);
			item.doClick();
		}
		if (pressedMenu != -1)
			menubar.getMenu(pressedMenu).setSelected(false);
		pressedMenu = -1;
		pressedItem = -1;
		repaint();
	}

	public void mouseMoved(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
	public void mouseClicked(MouseEvent event) {}
}
