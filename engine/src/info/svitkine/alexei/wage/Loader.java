package info.svitkine.alexei.wage;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

import javax.swing.*;

import org.freeshell.gbsmith.rescafe.resourcemanager.Resource;
import org.freeshell.gbsmith.rescafe.resourcemanager.ResourceModel;
import org.freeshell.gbsmith.rescafe.resourcemanager.ResourceType;

public class Loader {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		FileDialog dialog = new FileDialog(new Frame(), "Open File", FileDialog.LOAD);
		dialog.setVisible(true); 
		if (dialog.getFile() == null)
			return;
		String path = dialog.getDirectory() + "/" + dialog.getFile() + "/rsrc";
		ResourceModel model = new ResourceModel(dialog.getFile());
		model.read(new RandomAccessFile(path, "r"));
	/*	String theTypes[];
		theTypes = model.getTypeArray();
		if(theTypes == null) return;

		Arrays.sort(theTypes);
		for(int t=0; t < theTypes.length; t++)
		{
			System.out.println("Type\t'" + theTypes[t] + "' \t" +
					model.getCountOfType(theTypes[t]));
		}*/
		JFrame f = new JFrame();
		setupCloseWindowKeyStrokes(f, f.getRootPane());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));
		f.getContentPane().add(createResourceBrowser(model, "AOBJ"));
		f.getContentPane().add(createResourceBrowser(model, "ACHR"));
		f.getContentPane().add(createResourceBrowser(model, "ASCN"));
		f.getContentPane().add(createResourceBrowser(model, "ACOD"));
		f.getContentPane().add(createResourceBrowser(model, "ATXT"));
		f.getContentPane().add(createResourceBrowser(model, "GCOD"));
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		// AWTUtilities.setWindowShape(Window, Shape) ...
	}
	
	private static JComponent createResourceBrowser(final ResourceModel model, final String type) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		final JComboBox list = new JComboBox();
		final ResourceType rt = model.getResourceType(type);
		final HashMap<String, Short> ids = new HashMap<String, Short>();
		Resource[] arr = rt.getResArray();
		for (Resource r : arr) {
			if (type.equals("ACOD") || type.equals("ATXT")) {
				Resource scene = model.getResource("ASCN", r.getID());
				if (scene != null) {
					list.addItem(scene.getName());
					ids.put(scene.getName(), r.getID());
				}
			} else {
				list.addItem(r.getName());
				ids.put(r.getName(), r.getID());
			}
		}
		JLabel label = new JLabel("  " + type + ":");
		label.setPreferredSize(new Dimension(60, 22));
		panel.add(label);
		panel.add(list);
		JButton button = new JButton("Load");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = (String) list.getSelectedItem();
				Resource r = model.getResource(type, ids.get(name));
				if (type.equals("ACOD") || type.equals("GCOD")) {
			
						final Script script = new Script(r.getData());
						java.awt.EventQueue.invokeLater(new Runnable() {
							public void run() {
								createAndShowWindowWithContent(new JScrollPane(new JTextArea(script.toString())), 444, 333);
							}
						});
				} else if (type.equals("AOBJ") || type.equals("ACHR") || type.equals("ASCN")) {
					try {
						final ObjectViewer viewer = new ObjectViewer(new Design(r.getData()));
						java.awt.EventQueue.invokeLater(new Runnable() {
							public void run() {
								createAndShowWindowWithContent(viewer, 444, 333);
							}
						});
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				} else if (type.equals("ATXT")) {
					byte[] data = r.getData();
					final String text = new String(data, 12, data.length-12);
					java.awt.EventQueue.invokeLater(new Runnable() {
						public void run() {
							createAndShowWindowWithContent(new JScrollPane(new JTextArea(text)), 444, 333);
						}
					});
				}
			}
		});
		return panel;
	}
	
	public static void createAndShowWindowWithContent(JComponent content, int width, int height) {
		JFrame f = new JFrame();
		setupCloseWindowKeyStrokes(f, f.getRootPane());
		f.setContentPane(content);
		f.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		f.setSize(width, height);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
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
