package info.svitkine.alexei.wage;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JMenu;

public class MenuBarRenderer extends JComponent implements MouseListener {
	private static final int HEIGHT = 19;

	private JMenuBar menubar;
	private int[] offsets;
	private int[] spans;
	private boolean[] pressed;
	
	public MenuBarRenderer(JMenuBar menubar) {
		this.menubar = menubar;
		Font f = new Font("Chicago", Font.PLAIN, 13); 
		setFont(f);
		FontMetrics m = getFontMetrics(f);
		addMouseListener(this);
		int menus = menubar.getMenuCount();
		pressed = new boolean[menus];
		offsets = new int[menus];
		spans = new int[offsets.length];
		int x = 20;
		for (int i = 0; i < menubar.getMenuCount(); i++) {
			JMenu menu = menubar.getMenu(i);
			spans[i] = m.stringWidth(menu.getText());
			offsets[i] = x;
			x += spans[i] + 12;
		}
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), HEIGHT);
		g.setColor(Color.BLACK);
		g.fillRect(0, HEIGHT, getWidth(), 1);
		Font f = getFont();
		g.setFont(f);
		for (int i = 0; i < menubar.getMenuCount(); i++) {
			JMenu menu = menubar.getMenu(i);
			g.setColor(Color.BLACK);
			if (pressed[i]) {
				g.fillRect(offsets[i] - 6, 1, spans[i] + 12, HEIGHT - 1);
				g.setColor(Color.WHITE);
			}
			g.drawString(menu.getText(), offsets[i], 14);
		}
	}

	public void mouseClicked(MouseEvent event) {
		if (event.getY() > HEIGHT)
			return;
		for (int i = 0; i < menubar.getMenuCount(); i++) {
			if (event.getX() > offsets[i] - 6 && event.getX() - offsets[i] < spans[i] + 6) {
				pressed[i] = !pressed[i];
				repaint();
				break;
			}
		}
	}

	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
}
