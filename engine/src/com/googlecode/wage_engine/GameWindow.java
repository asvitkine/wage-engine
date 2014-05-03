package com.googlecode.wage_engine;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import com.googlecode.wage_engine.engine.Engine;
import com.googlecode.wage_engine.engine.Menu;
import com.googlecode.wage_engine.engine.MenuBar;
import com.googlecode.wage_engine.engine.MenuBarBuilder;
import com.googlecode.wage_engine.engine.MenuItem;
import com.googlecode.wage_engine.engine.Obj;
import com.googlecode.wage_engine.engine.Scene;
import com.googlecode.wage_engine.engine.SoundManager;
import com.googlecode.wage_engine.engine.World;

public class GameWindow extends JFrame implements Engine.Callbacks, MenuBarBuilder.Callbacks {
	private World world;
	private TexturePaint[] patterns;
	private Engine engine;
	private SceneViewer viewer;
	private ConsoleView console;
	private SoundManager soundManager;
	private File lastSaveFile;
	private WindowManager wm;
	private MenuBarBuilder menuBuilder;
	private MenuBar menubar;
	private boolean gameInProgress;
	private byte[] initialGameState;

	public GameWindow(World world, TexturePaint[] patterns) {
		this.world = world;
		this.patterns = patterns;
		this.menuBuilder = new MenuBarBuilder(world, this);
		SwingUtils.setupCloseWindowKeyStrokes(this, getRootPane());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				if (gameInProgress) {
					showCloseDialog(true);
				} else {
					setVisible(false);
					dispose();
				}
			}	
		});
		initializeGame();
		setSize(640, 480);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void doClose() {
		wm.removeAll();
		wm.setMenuBar(menubar);
		for (Menu menu : menubar) {
			for (MenuItem item : menu) {
				if (item != null &&
					!"New".equals(item.getText()) &&
					!"Open...".equals(item.getText()) &&
					!"Quit".equals(item.getText()))
				{
					item.setEnabled(false);
				}
			}
		}
		gameInProgress = false;
	}

	public void doNew() {
		initializeGame();
		// FIXME: Find a better way to repaint...
		Dimension size = getSize();
		size.height++;
		setSize(size);
		size.height--;
		setSize(size);
	}
	
	private void initializeGame() {
		gameInProgress = true;
		world.reset();
		soundManager = new SoundManager(world);
		viewer = new SceneViewer(patterns, world) {
			public void handleMouseEvent(int type, int x, int y) {
				if (type == MOUSE_CLICKED && isEnabled()) {
					startThread(new ClickHandler(x, y));
				}
			}
		};
		console = new ConsoleView();
		wm = new WindowManager();
		wm.add(console, true);
		wm.add(viewer, false);
		menubar = menuBuilder.createMenuBar();
		wm.setMenuBar(menubar);
		setContentPane(new WindowManagerHost(wm));
		engine = new Engine(world, console.getOut(), this);
		if (initialGameState == null) {
			initialGameState = engine.getSaveStateAsByteArray();
		} else try {
			engine.loadState(new ByteArrayInputStream(initialGameState));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		updateConsoleForScene(console, world.getPlayerScene());
		synchronized (engine) {
			engine.processTurn("look", null);
		}
		startThread(new UserInputReader());
	}

	private class ClickHandler implements Runnable {
		private int x;
		private int y;
		public ClickHandler(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public void run() {
			synchronized (engine) {
				final Object target = viewer.getClickTarget(x, y);
				if (target != null) {
					engine.processTurn(null, target);
				}
			}
		}
	}
	
	private class UserInputReader implements Runnable {
		public void run() {
			BufferedReader in = new BufferedReader(new InputStreamReader(console.getIn()));
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
	}

	private void startThread(Runnable runnable) {
		new Thread(runnable).start();
	}
	
	public void setCommandsMenu(String format) {
		menubar.setMenu(3, menuBuilder.createMenuFromString(world.getCommandsMenuName(), format));
	}

	private Scene lastScene = null;
	public void redrawScene() {
		final Scene currentScene = world.getPlayerScene();
		if (currentScene != null) {
			final boolean firstTime = (lastScene != currentScene);
			lastScene = currentScene;
			Runnable repainter = new Runnable() {
				public void run() {
					updateConsoleForScene(console, currentScene);
					updateSceneViewerForScene(viewer, currentScene);
					viewer.paintImmediately(viewer.getBounds());
					getContentPane().validate();
					getContentPane().repaint();
					console.postUpdateUI();
					soundManager.updateSoundTimerForScene(currentScene, firstTime);
				}
			};
			SwingUtils.runOnEventDispatchThread(repainter);
		}
	}

	public void clearOutput() {
		console.clear();
	}

	public void gameOver() {
		SwingUtils.runOnEventDispatchThread(new Runnable() {
			public void run() {
				if (isVisible()) {
					GameOverDialog dialog = new GameOverDialog(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							doClose();
						}
					}, world.getGameOverMessage());
					showDialog(dialog);
				}
			}
		});
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
		int w = getContentPane().getWidth();
		int h = getContentPane().getHeight();
		dialog.setLocation(w/2-dialog.getWidth()/2, h/2-dialog.getHeight()/2);
		wm.addModalDialog(dialog);
		// FIXME: below is to work around a bug with overlaps...
		console.setVisible(false);
		// FIXME: need to disable menus too!
		wm.repaint();
		wm.invalidate();
		wm.revalidate();
	}

	public void showOpenDialog() {
		FileDialog dialog = new FileDialog(new Frame(), "Load Game", FileDialog.LOAD);
		dialog.setVisible(true);
		if (dialog.getFile() == null)
			return;
		File file = new File(dialog.getDirectory() + "/" + dialog.getFile());
		try {
			engine.loadState(file);
			lastSaveFile = file;
			redrawScene();
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		 }
	}

	public void showCloseDialog(final boolean quitOnClose) {
		SaveDialog dialog = new SaveDialog(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (SaveDialog.NO_TEXT.equals(event.getActionCommand())) {
					doClose();
					if (quitOnClose)
						setVisible(false);
				} else if (SaveDialog.YES_TEXT.equals(event.getActionCommand())) {
					if (doSave()) {
						doClose();
						if (quitOnClose)
							setVisible(false);
					} else {
						closeSaveDialog();
					}
				} else if (SaveDialog.CANCEL_TEXT.equals(event.getActionCommand())) {
					closeSaveDialog();
				}
			}
		});
		showDialog(dialog);
	}
	
	private void closeSaveDialog() {
		wm.remove(wm.getModalDialog());
		console.setVisible(true);
		wm.repaint();
		wm.invalidate();
		wm.revalidate();
	}
	
	public boolean doSave() {
		if (lastSaveFile == null) {
			return doSaveAs();
		}

		try {
			engine.saveState(lastSaveFile);
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		}
		return true;
	}
	
	public boolean doSaveAs() {
		FileDialog dialog = new FileDialog(new Frame(), "Save Game", FileDialog.SAVE);
		dialog.setVisible(true);
		if (dialog.getFile() == null)
			return false;
		File file = new File(dialog.getDirectory() + "/" + dialog.getFile());
		try {
			engine.saveState(file);
			lastSaveFile = file;
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		}
		return true;
	}
	
	public void doRevert() {
		try {
			engine.revert();
			redrawScene();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void showAboutDialog() {
		String aboutMessage = world.getAboutMessage();
		aboutMessage = "<html><center>" + aboutMessage.replace("\n", "<br>");
		JOptionPane.showMessageDialog(GameWindow.this, new JLabel(aboutMessage));		
	}

	public void performCommand(String command) {
		console.getOut().append(command + "\n");
		doCommand(command);
	}

	private void updateConsoleForScene(ConsoleView console, Scene scene) {
		console.setBounds(scene.getTextBounds());
		console.setFont(new Font(scene.getFontName(), 0, scene.getFontSize()));
	}

	private void updateSceneViewerForScene(SceneViewer viewer, Scene scene) {
		viewer.setScene(scene);
		viewer.setBounds(scene.getDesignBounds());
	}

	public boolean isGameInProgress() {
		return gameInProgress;
	}
}
