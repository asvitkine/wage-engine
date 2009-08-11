package info.svitkine.alexei.wage;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class Parser {
	public static void main(String[] args) throws IOException {/*
		FileDialog dialog = new FileDialog(new Frame(), "Open File", FileDialog.LOAD);
		dialog.setVisible(true); 
		String path = dialog.getDirectory() + "/" + dialog.getFile(); */
		String path = "/Users/shadowknight/Desktop/spud.data";
		new Parser().parse(readAll(new FileInputStream(path)));
		System.exit(0);
	}

	public static byte[] readAll(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int nread;

		do {
			nread = in.read(buf, 0, buf.length);
			if (nread > 0) {
				out.write(buf, 0, nread);
			}
		} while (nread != -1);

		return out.toByteArray();
	}

	public void parse(byte[] data) {
		System.out.println(new Script(data).toString());
	}
}
