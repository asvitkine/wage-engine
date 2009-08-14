package info.svitkine.alexei.wage;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.freeshell.gbsmith.rescafe.resourcemanager.ResourceModel;

public class WBGameFinder {
	private static final FileFilter filter = new FileFilter() {
		public boolean accept(File file) {
			if (file.isDirectory())
				return true;
			ResourceModel model = new ResourceModel(file.getName());
			RandomAccessFile raf;
			try {
				raf = new RandomAccessFile(file.getPath() + "/rsrc", "r");
				model.read(raf);
				if (model.getResourceType("ASCN") != null) {
					return true;
				}
			} catch (IOException e) {
			}
			return false;
		}
	};

	public static void main(String[] args) {
		System.setProperty("apple.awt.fileDialogForDirectories", "true");
		FileDialog dialog = new FileDialog(new Frame(), "Choose Folder", FileDialog.LOAD);
		dialog.setVisible(true);
		if (dialog.getFile() == null)
			return;
		File file = new File(dialog.getDirectory() + "/" + dialog.getFile());
		if (filter.accept(file))
			processFile(file);
		System.out.println("Done");
		System.exit(0);
	}

	private static void processFile(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles(filter);
			if (files != null) {
				for (File f : files) {
					processFile(f);
				}
			}
		} else {
			System.out.println("Found WB Game: " + file.getAbsolutePath());
		}
	}
}
