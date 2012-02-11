package com.googlecode.wage_engine;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.fizzysoft.sdu.RecentDocumentsManager;
import com.googlecode.wage_engine.engine.World;
import com.googlecode.wage_engine.engine.WorldLoader;

public class WorldLoaderGUI {
	private RecentDocumentsManager rdm;

	private static WorldLoaderGUI instance;

	protected WorldLoaderGUI() {
		rdm = new RecentDocumentsManager() {
			private Preferences getPreferences() {
				return Preferences.userNodeForPackage(WorldLoader.class);
			}

			@Override
			protected byte[] readRecentDocs() {
				return getPreferences().getByteArray("RecentDocuments", null);
			}

			@Override
			protected void writeRecentDocs(byte[] data) {
				getPreferences().putByteArray("RecentDocuments", data);
			}

			@Override
			protected void openFile(File file, ActionEvent event) {
				try {
					openWorldFile(file);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}

	public static WorldLoaderGUI getInstance() {
		if (instance == null)
			instance = new WorldLoaderGUI();
		return instance;
	}
	
	public RecentDocumentsManager getRecentDocumentsManager() {
		return rdm;
	}

	private void openWorldFile(File file) throws FileNotFoundException, IOException  {
		rdm.addDocument(file, new Properties());
		JFrame f = new JFrame();
		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.add(rdm.createOpenRecentMenu());
		menubar.add(menu);
		f.setJMenuBar(menubar);
		SwingUtils.setupCloseWindowKeyStrokes(f, f.getRootPane());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getRootPane().putClientProperty("Window.documentFile", file);
		f.setTitle(file.getName());
		World world = new WorldLoader().loadWorld(file);
		f.setContentPane(new WorldBrowser(world));
		f.setSize(640, 480);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		System.setProperty("apple.awt.graphics.UseQuartz", "true");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		FileDialog dialog = new FileDialog(new Frame(), "Open File", FileDialog.LOAD);
		dialog.setVisible(true);
		if (dialog.getFile() == null)
			return;
		File file = new File(dialog.getDirectory() + "/" + dialog.getFile());
		WorldLoaderGUI.getInstance().openWorldFile(file);
	}
}
