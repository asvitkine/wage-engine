package info.svitkine.alexei.wage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.*;

public class ObjectViewer extends JPanel {
	private static final long serialVersionUID = 1L;
	private TexturePaint[] patterns;
	private Design design;
	private boolean maskMode;

	public ObjectViewer(Design design) throws IOException {
		int numPatterns = 29;
		patterns = new TexturePaint[numPatterns];
		for (int i = 1; i <= numPatterns; i++) {
			BufferedImage image = ImageIO.read(new File("patterns/" + i + ".png"));
			patterns[i - 1] = new TexturePaint(image, new Rectangle(0, 0, image.getWidth(), image.getHeight()));
		}
		this.design = design;
	}

	public void setMaskMode(boolean m) {
		maskMode = m;
	}
	
	public void setDesign(Design design) {
		this.design = design;
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

	public void paint(Graphics g) {
		if (design != null) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setClip(2, 2, getWidth()-4, getHeight()-4);
			g2d.translate(2, 2);
			if (maskMode) {
				design.paintMask(g2d);
			} else {
				design.paint(g2d, patterns);
			}
			g2d.translate(-2, -2);
			g2d.setClip(null);
		}
		super.paint(g);
	}

	@Override
	public boolean isOpaque() {
		return false;
	}

	public static void main(String args[]) throws IOException {
		//System.setProperty("apple.awt.graphics.UseQuartz", "true");
		String[] paths = {"objs.data", "wtf.data", "wtf2.dar", "poly.data",
				"polytest.data", "whatisit.data", "ray.data", "phone.data",
				"chart4.data", "computer2.data", "robot.body.data", "roach.data",
				"freehand.data", "fh2.data", "bb.data", "bb2.data", "bb3.data", "more-bb.data"};
		String path = "/Users/shadowknight/Desktop/" + paths[0];
		byte[] data = readAll(new FileInputStream(path));
		final ObjectViewer viewer = new ObjectViewer(new Design(data));
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame();
				f.setContentPane(viewer);
				f.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
				f.setSize(444, 333);
				f.setLocationRelativeTo(null);
				f.setVisible(true);
			}
		});
	}
}