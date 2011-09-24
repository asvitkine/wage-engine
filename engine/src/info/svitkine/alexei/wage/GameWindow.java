package info.svitkine.alexei.wage;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;

public class GameWindow extends JFrame {
	private World world;
	private Engine engine;
	private SceneViewer viewer;
	private ConsoleTextArea textArea;
	private JPanel panel;
	private Timer soundTimer;
	private File lastSaveFile;
	private WindowManager wm;

	public GameWindow(final World world, TexturePaint[] patterns) {
		this.world = world;
		Utils.setupCloseWindowKeyStrokes(this, getRootPane());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				showSaveDialog();
			}	
		});
		wm = new WindowManager();
		viewer = new SceneViewer(patterns);
		textArea = new ConsoleTextArea();
		panel = wrapInPanel(wrapInScrollPane(textArea));
		wm.add(viewer);
		wm.setComponentZOrder(viewer, 0);
		wm.add(panel);
		((WindowBorder) panel.getBorder()).setScrollable(true);
		wm.setComponentZOrder(viewer, 1);
		initializeGame();
		setContentPane(wm);
		setSize(640, 480);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void initializeGame() {
		Menu[] menus;
		if (!world.isWeaponsMenuDisabled())
			menus = new Menu[5];
		else
			menus = new Menu[4];
		menus[0] = createAppleMenu();
		menus[1] = createFileMenu();
		menus[2] = createEditMenu();
		menus[3] = createCommandsMenu();
		if (!world.isWeaponsMenuDisabled())
			menus[4] = createWeaponsMenu();
		final MenuBar menubar = new MenuBar(menus);
		wm.setMenuBar(menubar);
		engine = new Engine(world, textArea.getOut(), new Engine.Callbacks() {
			public void setCommandsMenu(String format) {
				menubar.setMenu(3, createMenuFromString(world.getCommandsMenuName(), format));
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
		synchronized (engine) {
			engine.processTurn("look", null);
		}
		viewer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (!viewer.isEnabled())
					return;
				startThread(new Runnable() {
					public void run() {
						synchronized (engine) {
							final Object target = viewer.getClickTarget(e);
							if (target != null) {
								engine.processTurn(null, target);
							}
						}
					}
				});
			}
		});
		startThread(new Runnable() {
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
		});
	}
	
	private void startThread(Runnable runnable) {
		new Thread(runnable).start();
	}
	
	private void redrawScene() {
		final Scene currentScene = world.getPlayerScene();
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
			for (Obj o : world.getPlayerScene().getState().getObjs())
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
		}
	}
	
	private void showDialog(Dialog dialog) {
		if (wm.getModalDialog() != null)
			return;
		int w = GameWindow.this.getContentPane().getWidth();
		int h = GameWindow.this.getContentPane().getHeight();
		dialog.setLocation(w/2-dialog.getWidth()/2, h/2-dialog.getHeight()/2);
		wm.addModalDialog(dialog);
		// FIXME: below is to work around a bug with overlaps...
		textArea.setVisible(false);
		// FIXME: need to disable menus too!
		wm.repaint();
		wm.invalidate();
		wm.revalidate();
	}
	
	private void gameOver() {
		if (isVisible()) {
			GameOverDialog dialog = new GameOverDialog(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					setVisible(false);
					dispose();
				}
			}, world.getGameOverMessage());
			showDialog(dialog);
		}
	}

	private Menu createAppleMenu() {
		MenuItem[] items = new MenuItem[] {
			new MenuItem(world.getAboutMenuItemName()) {
				public void performAction() {
					String aboutMessage = world.getAboutMessage();
					aboutMessage = "<html><center>" + aboutMessage.replace("\n", "<br>");
					JOptionPane.showMessageDialog(GameWindow.this, new JLabel(aboutMessage));
				}
			}
		};
		return new Menu("\uF8FF", items);
	}
	
	private Menu createFileMenu() {
		MenuItem[] items = new MenuItem[] {
			new MenuItem("New") {
				public void performAction() {
					JOptionPane.showMessageDialog(null, "Not implemented yet.");
				}
			},
			new MenuItem("Open...") {
				public void performAction() {
					showOpenDialog();
				}
			},
			new MenuItem("Close") {
				public void performAction() {
					showSaveDialog();
				}
			},
			new MenuItem("Save") {
				public void performAction() {
					doSave();
				}
			},
			new MenuItem("Save as...") {
				public void performAction() {
					doSaveAs();
				}
			},
			new MenuItem("Revert") {
				public void performAction() {
					doRevert();
				}
			},
			new MenuItem("Quit")
		};
		return new Menu("File", items);
	}

	private Menu createEditMenu() {
		MenuItem[] items = new MenuItem[] {
			new MenuItem("Undo", 0, 'Z'),
			null, // separator
			new MenuItem("Cut", 0, 'K'),
			new MenuItem("Copy", 0, 'C'),
			new MenuItem("Paste", 0, 'V'),
			new MenuItem("Clear", 0, 'B'),
			
		};
		return new Menu("Edit", items);
	}

	private void showOpenDialog() {
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

	private void showSaveDialog() {
		SaveDialog dialog = new SaveDialog(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (SaveDialog.NO_TEXT.equals(event.getActionCommand())) {
					GameWindow.this.setVisible(false);
				} else if (SaveDialog.YES_TEXT.equals(event.getActionCommand())) {
					doSave();
					// TODO: If they clicked cancel in the save dialog, don't close the window!
					GameWindow.this.setVisible(false);					
				} else if (SaveDialog.CANCEL_TEXT.equals(event.getActionCommand())) {
					closeSaveDialog();
				}
			}
		});
		showDialog(dialog);
	}
	
	private void closeSaveDialog() {
		wm.remove(wm.getModalDialog());
		textArea.setVisible(true);
		wm.repaint();
		wm.invalidate();
		wm.revalidate();
	}
	
	private void doSave() {
		if (lastSaveFile == null) {
			doSaveAs();
			return;
		}

		try {
			engine.saveState(lastSaveFile);
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		}
	}
	
	private void doSaveAs() {
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
	
	private void doRevert() {
		try {
			engine.revert();
			GameWindow.this.redrawScene();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private Menu createWeaponsMenu() {
		return new Menu(world.getWeaponsMenuName(), new MenuItem[0]) {
			public void willShow() {
				this.items = generateWeaponsMenuItems();
			}
		};
	}
	
	private MenuItem[] generateWeaponsMenuItems() {
		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
		Chr player = world.getPlayer();
		for (Weapon obj : player.getWeapons(true)) {
			if (obj.getType() == Obj.REGULAR_WEAPON ||
				obj.getType() == Obj.THROW_WEAPON ||
				obj.getType() == Obj.MAGICAL_OBJECT) {
				menuItems.add(new MenuItem(obj.getOperativeVerb() + " " + obj.getName()) {
					public void performAction() {
						textArea.getOut().append(getText() + "\n");
						doCommand(getText());
					}
				});
			}
		}
		if (menuItems.size() == 0) {
			menuItems.add(new MenuItem("You have no weapons", 0, (char) 0, false));
		}
		return menuItems.toArray(new MenuItem[menuItems.size()]);
	}

	private Menu createCommandsMenu() {
		return createMenuFromString(world.getCommandsMenuName(), world.getDefaultCommandsMenu());
	}
	
	private Menu createMenuFromString(String name, String string) {
		String[] items = string.split(";");
		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
		for (String item : items) {
			if (item.equals("(-")) {
				menuItems.add(null); // separator
			} else {
				boolean enabled = true;
				int style = 0;
				char shortcut = 0;
				int index = item.lastIndexOf("/");
				if (index != -1) {
					shortcut = item.substring(index).charAt(1);
					item = item.substring(0, index);
				}
				while (item.length() >= 2 && item.charAt(item.length() - 2) == '<') {
					char c = item.charAt(item.length() - 1);
					if (c == 'B') {
						style |= MenuItem.BOLD;
					} else if (c == 'I') {
						style |= MenuItem.ITALIC;
					} else if (c == 'U') {
						style |= MenuItem.UNDERLINE;
					} else if (c == 'O') {
						style |= MenuItem.OUTLINE;
					} else if (c == 'S') {
						style |= MenuItem.SHADOW;
					} else if (c == 'C') {
						style |= MenuItem.CONDENSED;
					} else if (c == 'E') {
						style |= MenuItem.EXTENDED;
					}
					item = item.substring(0, item.length() - 2);
				}
				if (item.trim().startsWith("(")) {
					enabled = false;
					int loc = item.indexOf("(");
					item = item.substring(0, loc) + item.substring(loc + 1);
				}
				menuItems.add(new MenuItem(item, style, shortcut, enabled) {
					public void performAction() {
						textArea.getOut().append(getText() + "\n");
						doCommand(getText());
					}
				});
			}
		}
		return new Menu(name, menuItems.toArray(new MenuItem[menuItems.size()]));
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
			if (world.getPlayerScene() == scene) {
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
		if (world.getPlayerScene() != scene)
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

	private JScrollPane wrapInScrollPane(JComponent textArea) {
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
