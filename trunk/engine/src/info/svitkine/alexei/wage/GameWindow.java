package info.svitkine.alexei.wage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class GameWindow extends JFrame {
	private World world;
	private Engine engine;
	private SceneViewer viewer;
	private ConsoleTextArea textArea;
	private JPanel panel;
	private Timer soundTimer;
	private File lastSaveFile;
	
	public GameWindow(final World world, TexturePaint[] patterns) {
		this.world = world;
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = createFileMenu();
		JMenu editMenu = createEditMenu();
		final JMenu commandsMenu = createCommandsMenu();
		menubar.add(createAppleMenu());
		menubar.add(fileMenu);
		menubar.add(editMenu);
		menubar.add(commandsMenu);
		if (!world.isWeaponsMenuDisabled())
			menubar.add(createWeaponsMenu());
		Utils.setupCloseWindowKeyStrokes(this, getRootPane());
		WindowManager wm = new WindowManager();
		viewer = new SceneViewer(patterns);
		textArea = createTextArea();
		final JScrollPane scrollPane = wrapInScrollPane(textArea);
		panel = wrapInPanel(scrollPane);
		engine = new Engine(world, textArea.getOut(), new Engine.Callbacks() {
			public void setCommandsMenu(String format) {
				updateMenuFromString(commandsMenu, format);
			}
			public void redrawScene() {
				GameWindow.this.redrawScene();
			}
			public void clearOutput() {
				textArea.clear();
			}
			public void gameOver() {
				runOnEventDispatchThread(new Runnable() {
					public void run() {
						GameWindow.this.gameOver();
					}
				});
			}
		});
		engine.processTurn("look", null);
		textArea.getOut().append("\n");
		wm.add(viewer);
		wm.setComponentZOrder(viewer, 0);
		wm.add(panel);
		((WindowBorder) panel.getBorder()).setScrollable(true);
		wm.setComponentZOrder(viewer, 1);
		viewer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						synchronized (engine) {
							final Object target = viewer.getClickTarget(e);
							if (target != null) {
								engine.processTurn(null, target);
								textArea.getOut().append("\n");
							}
						}
					}
				});
				thread.start();
			}
		});
		new Thread(new Runnable() {
			public void run() {
				BufferedReader in = new BufferedReader(new InputStreamReader(textArea.getIn()));
				try {
					String line = in.readLine();
					while (line != null) {
						doCommand(line);
						line = in.readLine();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		setJMenuBar(menubar);
		setContentPane(wm);
		setSize(640, 480);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void redrawScene() {
		final Scene currentScene = world.getPlayer().getCurrentScene();
		if (currentScene != null) {
			Runnable repainter = new Runnable() {
				public void run() {
					updateTextAreaForScene(textArea, panel, currentScene);
					updateSceneViewerForScene(viewer, currentScene);
					viewer.paintImmediately(viewer.getBounds());
					getContentPane().validate();
					getContentPane().repaint();
					textArea.postUpdateUI();
					updateSoundTimerForScene(currentScene, true);
				}
			};
			runOnEventDispatchThread(repainter);
		}
	}

	private static void runOnEventDispatchThread(Runnable runnable) {
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

	private void doCommand(String line) {
		if (line.equals("debug")) {
			for (Obj o : world.getPlayer().getCurrentScene().getObjs())
				System.out.println(o.getName());
			return;
		}
		line = line.trim().toLowerCase();
		if (line.equals("n"))
			line = "north";
		else if (line.equals("s"))
			line = "south";
		else if (line.equals("w"))
			line = "west";
		else if (line.equals("e"))
			line = "east";
		else
			line = line.replaceAll("\\s+", " ");
		synchronized (engine) {
			engine.processTurn(line, null);
			textArea.getOut().append("\n");
		}
	}
	
	private void gameOver() {
		if (isVisible()) {
			JOptionPane.showMessageDialog(GameWindow.this, "Game over!");
			setVisible(false);
			dispose();
		}
	}

	private JMenu createAppleMenu() {
		// TODO: extract info (such as about name), out of the MENU resource
		JMenu menu = new JMenu("\uF8FF");
		JMenuItem menuItem = new JMenuItem("About " + world.getName() + "...");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String aboutMessage = world.getAboutMessage();
				aboutMessage = "<html><center>" + aboutMessage.replace("\n", "<br>");
				JOptionPane.showMessageDialog(GameWindow.this, new JLabel(aboutMessage));
			}
		});
		menu.add(menuItem);
		return menu;
	}
	
	private JMenu createFileMenu() {
		JMenu menu = new JMenu("File");
		menu.add(new JMenuItem(new NewAction()));
		menu.add(new JMenuItem(new OpenAction()));
		menu.add(new JMenuItem(new CloseAction()));
		menu.add(new JMenuItem(new SaveAction()));
		menu.add(new JMenuItem(new SaveAsAction()));
		menu.add(new JMenuItem(new RevertAction()));
		menu.add(new JMenuItem(new QuitAction()));
		return menu;
	}

	private JMenu createEditMenu() {
		JMenu menu = new JMenu("Edit");
		menu.add(new JMenuItem("Undo"));
		menu.addSeparator();
		menu.add(new JMenuItem("Cut"));
		menu.add(new JMenuItem("Copy"));
		menu.add(new JMenuItem("Paste"));
		menu.add(new JMenuItem("Clear"));
		return menu;
	}
	
	public class NewAction extends AbstractAction {
		public NewAction() {
			putValue(NAME, "New");
		}

		public void actionPerformed(ActionEvent e) {
			// TODO
			JOptionPane.showMessageDialog(null, "Not implemented yet.");
		}
	}
	
	public class OpenAction extends AbstractAction {
		public OpenAction() {
			putValue(NAME, "Open...");
		}

		public void actionPerformed(ActionEvent e) {
			FileDialog dialog = new FileDialog(new Frame(), "Load Game", FileDialog.LOAD);
			dialog.setVisible(true);
			if (dialog.getFile() == null)
				return;
			File file = new File(dialog.getDirectory() + "/" + dialog.getFile());
			try {
				engine.loadState(file);
				lastSaveFile = file;
				GameWindow.this.redrawScene();
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				ioe.printStackTrace();
			}
		}
	}

	public class CloseAction extends AbstractAction {
		public CloseAction() {
			putValue(NAME, "Close");
		}

		public void actionPerformed(ActionEvent e) {
			// TODO
			JOptionPane.showMessageDialog(null, "Not implemented yet.");
		}
	}
	
	public class SaveAction extends SaveAsAction {
		public SaveAction() {
			putValue(NAME, "Save");
		}

		public void actionPerformed(ActionEvent e) {
			if (lastSaveFile == null) {
				super.actionPerformed(e);
				return;
			}

			try {
				engine.saveState(lastSaveFile);
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				ioe.printStackTrace();
			}
		}
	}
	
	public class SaveAsAction extends AbstractAction {
		public SaveAsAction() {
			putValue(NAME, "Save As...");
		}

		public void actionPerformed(ActionEvent e) {
			FileDialog dialog = new FileDialog(new Frame(), "Save Game", FileDialog.SAVE);
			dialog.setVisible(true);
			if (dialog.getFile() == null)
				return;
			File file = new File(dialog.getDirectory() + "/" + dialog.getFile());
			try {
				engine.saveState(file);
				lastSaveFile = file;
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				ioe.printStackTrace();
			}
		}
	}

	public class RevertAction extends AbstractAction {
		public RevertAction() {
			putValue(NAME, "Revert");
		}

		public void actionPerformed(ActionEvent e) {
			try {
				engine.revert();
				GameWindow.this.redrawScene();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public class QuitAction extends AbstractAction {
		public QuitAction() {
			putValue(NAME, "Quit");
		}

		public void actionPerformed(ActionEvent e) {

		}
	}
	
	private JMenu createWeaponsMenu() {
		final JMenu menu = new JMenu("Weapons");
		menu.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent e) {
				menu.removeAll();
				Chr player = world.getPlayer();
				for (Weapon obj : player.getWeapons()) {
					if (obj.getType() == Obj.REGULAR_WEAPON ||
						obj.getType() == Obj.THROW_WEAPON ||
						obj.getType() == Obj.MAGICAL_OBJECT) {
						menu.add(new JMenuItem(obj.getOperativeVerb() + " " + obj.getName()));
					}
				}
				for (Object item : menu.getMenuComponents()) {
					((JMenuItem) item).addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							textArea.getOut().append(e.getActionCommand() + "\n");
							doCommand(e.getActionCommand());
						}
					});
				}
			}

			public void menuCanceled(MenuEvent e) {}
			public void menuDeselected(MenuEvent e) {}
		});

		return menu;
	}

	private JMenu createCommandsMenu() {
		JMenu menu = new JMenu("Commands");
		updateMenuFromString(menu, "North/N;South/S;East/E;West/W;Up/U;Down/D;(-;Look/L;Rest/R;Status/T;Inventory/I;Search/F;(-;Open;Close");
		return menu;
	}
	
	private void updateMenuFromString(JMenu menu, String string) {
		String[] items = string.split(";");
		menu.removeAll();
		for (String item : items) {
			if (item.equals("(-")) {
				menu.addSeparator();
			} else {
				boolean enabled = true;
				int style = 0;
				KeyStroke shortcut = null;
				while (item.length() >= 2 && item.charAt(item.length() - 2) == '<') {
					char c = item.charAt(item.length() - 1);
					if (c == 'B') {
						style |= Font.BOLD;
					} else if (c == 'I') {
						style |= Font.ITALIC;
					}
					item = item.substring(0, item.length() - 2);
				}
				int index = item.lastIndexOf("/");
				if (index != -1) {
					shortcut = KeyStroke.getKeyStroke(item.substring(index).charAt(1),
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
					item = item.substring(0, index);
				}
				if (item.trim().startsWith("(")) {
					enabled = false;
					int loc = item.indexOf("(");
					item = item.substring(0, loc) + item.substring(loc + 1);
				}
				JMenuItem menuItem = new JMenuItem(item);
				if (style != 0) {
					Font font = menuItem.getFont();
					menuItem.setFont(new Font(font.getFamily(), style, font.getSize()));
				}
				if (shortcut != null) {
					menuItem.setAccelerator(shortcut);
				}
				if (!enabled) {
					menuItem.setEnabled(false);
				}
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						textArea.getOut().append(e.getActionCommand() + "\n");
						doCommand(e.getActionCommand());
					}
				});
				menu.add(menuItem);
			}
		}
	}

	private void updateTextAreaForScene(ConsoleTextArea textArea, JPanel panel, Scene scene) {
		textArea.setFont(new Font(scene.getFontName(), 0, scene.getFontSize()));
		panel.setBounds(scene.getTextBounds());
	}

	private void updateSceneViewerForScene(SceneViewer viewer, Scene scene) {
		viewer.setScene(scene);
		viewer.setBounds(scene.getDesignBounds());
	}

	private class PlaySoundTask extends TimerTask {
		private Scene scene;
		private Sound sound;

		public PlaySoundTask(Scene scene, Sound sound) {
			this.scene = scene;
			this.sound = sound;
		}

		public void run() {
			if (world.getPlayer().getCurrentScene() == scene) {
				sound.play();
			}
		}
	}

	private class UpdateSoundTimerTask extends TimerTask {
		private Scene scene;

		public UpdateSoundTimerTask(Scene scene) {
			this.scene = scene;
		}

		public void run() {
			updateSoundTimerForScene(scene, false);
		}
	}

	private void updateSoundTimerForScene(Scene scene, boolean firstTime) {
		if (soundTimer != null) {
			soundTimer.cancel();
			soundTimer = null;
		}
		if (world.getPlayer().getCurrentScene() != scene)
			return;
		if (scene.getSoundFrequency() > 0 && scene.getSoundName() != null && scene.getSoundName().length() > 0) {
			final Sound sound = world.getSounds().get(scene.getSoundName().toLowerCase());
			if (sound != null) {
				soundTimer = new Timer();
				switch (scene.getSoundType()) {
					case Scene.PERIODIC:
						if (firstTime)
							soundTimer.schedule(new PlaySoundTask(scene, sound), 0);
						int delay = 60000 / scene.getSoundFrequency();
						soundTimer.schedule(new PlaySoundTask(scene, sound), delay);
						soundTimer.schedule(new UpdateSoundTimerTask(scene), delay + 1);
						break;
					case Scene.RANDOM:
						for (int i = 0; i < scene.getSoundFrequency(); i++)
							soundTimer.schedule(new PlaySoundTask(scene, sound), (int) (Math.random() * 60000));
						soundTimer.schedule(new UpdateSoundTimerTask(scene), 60000);
						break;
				}
			}
		}
	}

	private ConsoleTextArea createTextArea() {
		ConsoleTextArea textArea = new ConsoleTextArea();
		textArea.setColumns(0);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		return textArea;
	}

	private JScrollPane wrapInScrollPane(JTextArea textArea) {
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		return scrollPane;
	}

	private JPanel wrapInPanel(JScrollPane scrollPane) {
		JPanel text = new JPanel() {
			public void paint(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setClip(2, 2, getWidth()-4, getHeight()-4);
				super.paint(g);
				g2d.setClip(null);
				paintBorder(g);
			}
		};
		text.setBackground(Color.WHITE);
		text.setLayout(new BorderLayout());
		text.add(scrollPane, BorderLayout.CENTER);
		return text;
	}
}
