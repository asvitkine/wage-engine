package com.googlecode.wage_engine;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

public class GameWindow extends JFrame implements Engine.Callbacks, MenuBarBuilder.Callbacks {
	private World world;
	private Engine engine;
	private SceneViewer viewer;
	private Console textArea;
	private JComponent panel;
	private SoundManager soundManager;
	private File lastSaveFile;
	private WindowManager wm;
	private MenuBarBuilder menuBuilder;
	private MenuBar menubar;

	public GameWindow(World world, TexturePaint[] patterns) {
		this.world = world;
		Utils.setupCloseWindowKeyStrokes(this, getRootPane());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				showSaveDialog();
			}	
		});
		soundManager = new SoundManager(world);
		wm = new WindowManager();
		viewer = new SceneViewer(patterns);
		textArea = new ConsoleTextArea();
		panel = wrapInPanel(wrapInScrollPane((JComponent)textArea));
	//	textArea = new ConsoleView();
	//	panel = (JComponent) textArea;
		wm.add(viewer);
		wm.setComponentZOrder(viewer, 0);
		wm.add(panel, true);
		wm.setComponentZOrder(viewer, 1);
		initializeGame();
		setContentPane(wm);
		setSize(640, 480);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void initializeGame() {
		menuBuilder = new MenuBarBuilder(world, this);
		menubar = menuBuilder.createMenuBar();
		wm.setMenuBar(menubar);
		engine = new Engine(world, textArea.getOut(), this);
		synchronized (engine) {
			engine.processTurn("look", null);
		}
		viewer.addMouseListener(new ClickListener());
		startThread(new UserInputReader());
	}

	private class ClickListener extends MouseAdapter {
		public void mouseClicked(final MouseEvent e) {
			if (!viewer.isEnabled())
				return;
			startThread(new ClickHandler(e.getX(), e.getY()));
		}
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
	}

	private void startThread(Runnable runnable) {
		new Thread(runnable).start();
	}
	
	public void setCommandsMenu(String format) {
		menubar.setMenu(3, menuBuilder.createMenuFromString(world.getCommandsMenuName(), format));
	}

	public void redrawScene() {
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
					soundManager.updateSoundTimerForScene(currentScene, true);
				}
			};
			Utils.runOnEventDispatchThread(repainter);
		}
	}

	public void clearOutput() {
		textArea.clear();
	}

	public void gameOver() {
		Utils.runOnEventDispatchThread(new Runnable() {
			public void run() {
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
		textArea.setVisible(false);
		// FIXME: need to disable menus too!
		wm.repaint();
		wm.invalidate();
		wm.revalidate();
	}

	public void doNew() {
		JOptionPane.showMessageDialog(null, "Not implemented yet.");
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

	public void showSaveDialog() {
		SaveDialog dialog = new SaveDialog(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (SaveDialog.NO_TEXT.equals(event.getActionCommand())) {
					setVisible(false);
				} else if (SaveDialog.YES_TEXT.equals(event.getActionCommand())) {
					doSave();
					// TODO: If they clicked cancel in the save dialog, don't close the window!
					setVisible(false);					
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
	
	public void doSave() {
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
	
	public void doSaveAs() {
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
		textArea.getOut().append(command + "\n");
		doCommand(command);
	}

	private void updateTextAreaForScene(Console textArea, JComponent panel, Scene scene) {
		textArea.setFont(new Font(scene.getFontName(), 0, scene.getFontSize()));
		panel.setBounds(scene.getTextBounds());
	}

	private void updateSceneViewerForScene(SceneViewer viewer, Scene scene) {
		viewer.setScene(scene);
		viewer.setBounds(scene.getDesignBounds());
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
