package info.svitkine.alexei.wage;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

public abstract class Utils {

	public static int fourCharsToInt(String str) {
		return (str.charAt(0) << 24) + (str.charAt(1) << 16) + (str.charAt(2) << 8) + str.charAt(3);
	}
	
	public static String intToFourChars(int value) {
		char[] chars = {
			(char) (0xFF & (value >> 24)),
			(char) (0xFF & (value >> 16)),
			(char) (0xFF & (value >> 8)),
			(char) (0xFF & value)
		};
		return new String(chars);
	}
	
	public static void setFileTypeAndCreator(String path, String type, String creator) {
		int typeInt = fourCharsToInt(type);
		int creatorInt = fourCharsToInt(creator);
		try {
			Class<?> fileManagerClass = Class.forName("com.apple.eio.FileManager");
			Class<?>[] argTypes = new Class[] { String.class, int.class };
			Method setFileTypeMethod = fileManagerClass.getDeclaredMethod("setFileType", argTypes);
			Method setFileCreatorMethod = fileManagerClass.getDeclaredMethod("setFileCreator", argTypes);
			setFileTypeMethod.invoke(null, new Object[] { path, new Integer(typeInt) });
			setFileCreatorMethod.invoke(null, new Object[] { path, new Integer(creatorInt) });
		} catch (Throwable e) {
		}
	}
	
	public static String[] getFileTypeAndCreator(String path) {
		// TODO: What about MacBinary files?
		try {
			Class<?> fileManagerClass = Class.forName("com.apple.eio.FileManager");
			Class<?>[] argTypes = new Class[] { String.class };
			Method getFileTypeMethod = fileManagerClass.getDeclaredMethod("getFileType", argTypes);
			Method getFileCreatorMethod = fileManagerClass.getDeclaredMethod("getFileCreator", argTypes);
			Integer type = (Integer) getFileTypeMethod.invoke(null, new Object[] { path });
			Integer creator = (Integer) getFileCreatorMethod.invoke(null, new Object[] { path });
			return new String[] { intToFourChars(type), intToFourChars(creator) };
		} catch (Throwable e) {
		}
		return null;
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
