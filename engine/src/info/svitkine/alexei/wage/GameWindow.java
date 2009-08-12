package info.svitkine.alexei.wage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;

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
	private Timer randomSoundTimer;
	
	public GameWindow(final World world) {
		this.world = world;
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = createFileMenu();
		JMenu editMenu = createEditMenu();
		final JMenu commandsMenu = createCommandsMenu();
		JMenu weaponsMenu = createWeaponsMenu();
		menubar.add(createAppleMenu());
		menubar.add(fileMenu);
		menubar.add(editMenu);
		menubar.add(commandsMenu);
		menubar.add(weaponsMenu);
		Loader.setupCloseWindowKeyStrokes(this, getRootPane());
		WindowManager wm = new WindowManager();
		viewer = new SceneViewer();
		textArea = createTextArea();
		final JScrollPane scrollPane = wrapInScrollPane(textArea);
		panel = wrapInPanel(scrollPane);
		engine = new Engine(world, textArea.getOut(), new Engine.Callbacks() {
			public void setCommandsMenu(String format) {
				updateMenuFromString(commandsMenu, format);
			}
		});
		engine.processTurn(null, null);
		Scene scene = world.getPlayer().getCurrentScene();
		world.addMoveListener(new World.MoveListener() {
			public void onMove(World.MoveEvent event) {
				Scene currentScene = world.getPlayer().getCurrentScene();
				if (event.getTo() == currentScene || event.getFrom() == currentScene) {
					Runnable repainter = new Runnable() {
						public void run() {
							viewer.repaint();
							getContentPane().repaint();
						}
					};
					runOnEventDispatchThread(repainter);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					textArea.setText("");
				}
			}
		});
		wm.add(viewer);
		wm.add(panel);
		updateSceneViewerForScene(viewer, scene);
		updateTextAreaForScene(textArea, panel, scene);
		updateSoundTimerForScene(scene);
		viewer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				synchronized (engine) {
					final Object target = viewer.getClickTarget(e);
					if (target != null) {
						Thread thread = new Thread(new Runnable() {
							public void run() {
								engine.processTurn(null, target);
								textArea.getOut().append("\n");
								processEndOfTurn();
							}
						});
						thread.start();
						while (true) {
							try { thread.join(); break; } catch (InterruptedException e1) { }
						}
					}
				}
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
			processEndOfTurn();
		}
	}
	
	private void processEndOfTurn() {
		final Scene scene = world.getPlayer().getCurrentScene();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (scene != viewer.getScene()) {
					if (scene == world.getStorageScene()) {
						JOptionPane.showMessageDialog(GameWindow.this, "Game over!");
						setVisible(false);
						dispose();
					} else {
						updateSceneViewerForScene(viewer, scene);
						updateTextAreaForScene(textArea, panel, scene);
						updateSoundTimerForScene(scene);
					}
				}
				getContentPane().validate();
				getContentPane().repaint();
				textArea.postUpdateUI();
			}
		});
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
		menu.add(new JMenuItem("New"));
		menu.add(new JMenuItem("Open..."));
		menu.add(new JMenuItem("Close"));
		menu.add(new JMenuItem("Save"));
		menu.add(new JMenuItem("Save As..."));
		menu.add(new JMenuItem("Revert"));
		menu.add(new JMenuItem("Quit"));
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

	private JMenu createWeaponsMenu() {
		final JMenu menu = new JMenu("Weapons");
		menu.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent e) {
				menu.removeAll();
				Chr player = world.getPlayer();
				if (player.getNativeWeapon1().length() > 0)
					menu.add(new JMenuItem(player.getOperativeVerb1() + " " + player.getNativeWeapon1()));
				if (player.getNativeWeapon2().length() > 0)
					menu.add(new JMenuItem(player.getOperativeVerb2() + " " + player.getNativeWeapon2()));
				for (Obj obj : player.getInventory()) {
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
				JMenuItem menuItem;
				int index = item.lastIndexOf("/");
				if (index != -1) {
					menuItem = new JMenuItem(item.substring(0, index));
					menuItem.setAccelerator(KeyStroke.getKeyStroke(item.substring(index).charAt(1),
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
				} else {
					menuItem = new JMenuItem(item);
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

	private static class PlaySoundTask extends TimerTask {
		private Sound sound;

		public PlaySoundTask(Sound sound) {
			this.sound = sound;
		}

		public void run() {
			sound.play();
		}
	}

	private class UpdateSoundTimerTask extends TimerTask {
		private Scene scene;

		public UpdateSoundTimerTask(Scene scene) {
			this.scene = scene;
		}

		public void run() {
			synchronized (engine) {
				if (world.getPlayer().getCurrentScene() == scene) {
					updateSoundTimerForScene(scene);
				}
			}
		}
	}

	private void updateSoundTimerForScene(final Scene scene) {
		if (randomSoundTimer != null) {
			randomSoundTimer.cancel();
			randomSoundTimer = null;
		}
		if (scene.getSoundFrequency() > 0 && scene.getSoundName() != null && scene.getSoundName().length() > 0) {
			final Sound sound = world.getSounds().get(scene.getSoundName().toLowerCase());
			if (sound != null) {
				randomSoundTimer = new Timer();
				switch (scene.getSoundType()) {
					case Scene.PERIODIC:
						int delay = 60000 / scene.getSoundFrequency();
						randomSoundTimer.schedule(new PlaySoundTask(sound), delay);
						randomSoundTimer.schedule(new UpdateSoundTimerTask(scene), delay + 1);
						break;
					case Scene.RANDOM:
						for (int i = 0; i < scene.getSoundFrequency(); i++)
							randomSoundTimer.schedule(new PlaySoundTask(sound), (int) (Math.random() * 60000));
						randomSoundTimer.schedule(new UpdateSoundTimerTask(scene), 60000);
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
