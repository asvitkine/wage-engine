package info.svitkine.alexei.wage;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

public abstract class Utils {
	public static Action setupCloseWindowKeyStrokes(Window window, JRootPane rootPane) {
		Action closeAction = new CloseWindowAction(window);
		KeyStroke closeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(closeKeyStroke, "CLOSE");
		rootPane.getActionMap().put("CLOSE", closeAction);
		return closeAction;
	}

	public static class CloseWindowAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private Window window;
		public CloseWindowAction(Window window) {
			super();
			this.window = window;
		}
		public void actionPerformed(ActionEvent e) {
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
		}
	}
}
