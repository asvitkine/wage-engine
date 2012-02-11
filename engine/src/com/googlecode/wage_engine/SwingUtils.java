package com.googlecode.wage_engine;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class SwingUtils {

	public static void runOnEventDispatchThread(Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else try {
			SwingUtilities.invokeAndWait(runnable);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

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
