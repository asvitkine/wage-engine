package info.svitkine.alexei.wage;

import java.awt.*;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;

public class SaveDialog extends JComponent {
	private DialogButton[] buttons;
	private DialogButton defaultButton;
	private DialogButton pressedButton;
	private boolean mouseOverPressedButton;
	
	public SaveDialog() {
		setSize(292, 114);
		buttons = new DialogButton[] {
			new DialogButton("No", new Rectangle(19, 67, 68, 28)),
			new DialogButton("Yes", new Rectangle(112, 67, 68, 28)),
			new DialogButton("Cancel", new Rectangle(205, 67, 68, 28))
		};
		defaultButton = buttons[1];
		MouseInputAdapter listener = new MouseInputAdapter() {
			private boolean checkBounds(DialogButton button, MouseEvent event) {
				Rectangle bounds = new Rectangle(
					button.bounds.x + 5,button.bounds.y + 5,
					button.bounds.width - 10, button.bounds.height - 10);
				return bounds.contains(event.getPoint());
			}

			private void updateMouseOverPressButton(MouseEvent event) {
				if (pressedButton != null) {
					boolean over = checkBounds(pressedButton, event);
					if (over != mouseOverPressedButton) {
						mouseOverPressedButton = over;
						repaint();
					}
				}
			}
			
			@Override
			public void mousePressed(MouseEvent event) {
				for (DialogButton button : buttons) {
					if (checkBounds(button, event)) {
						pressedButton = button;
						mouseOverPressedButton = true;
						repaint();
						break;
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				pressedButton = null;
				mouseOverPressedButton = false;
				repaint();
			}

			@Override
			public void mouseDragged(MouseEvent event) {
				updateMouseOverPressButton(event);
			} 
			
			@Override
			public void mouseMoved(MouseEvent event) {
				updateMouseOverPressButton(event);
			}
		};
		addMouseListener(listener);
		addMouseMotionListener(listener);
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.WHITE);
		Rectangle bounds = new Rectangle(0, 0, getWidth(), getHeight());
		g2d.fill(bounds);
		g2d.setColor(Color.BLACK);
		g2d.drawString("Save changes before closing?", 24, 32);
		drawOutline(g2d, bounds, new int[] { 1, 0, 0, 1, 1 });
		for (DialogButton button : buttons) {
			int[] outline = new int[] { 0, 0, 0, 0, 1 };
			if (button == defaultButton) {
				outline[0] = outline[1] = 1;
			}
			if (pressedButton == button && mouseOverPressedButton) {
				g2d.setColor(Color.BLACK);
				g2d.fillRect(button.bounds.x + 5, button.bounds.y + 5,
					button.bounds.width - 10, button.bounds.height - 10);
				g2d.setColor(Color.WHITE);
			}
			int w = g.getFontMetrics(getFont()).stringWidth(button.text);
			int x = button.bounds.x + (button.bounds.width - w) / 2;
			int y = button.bounds.y + 19;
			g2d.drawString(button.text, x, y);
			g2d.setColor(Color.BLACK);
			drawOutline(g2d, button.bounds, outline);
		}
	}

	private void drawOutline(Graphics2D g2d, Rectangle bounds, int[] spec) {
		for (int i = 0; i < spec.length; i++) {
			if (spec[i] != 0) {
				g2d.drawRect(bounds.x + i, bounds.y + i, bounds.width - 1 - 2*i, bounds.height - 1 - 2*i);
			}
		}
	}
	
	private static class DialogButton {
		String text;
		Rectangle bounds;
		public DialogButton(String text, Rectangle bounds) {
			this.text = text;
			this.bounds = bounds;
		}
	}
}
