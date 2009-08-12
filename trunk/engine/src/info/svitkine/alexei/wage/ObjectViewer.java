package info.svitkine.alexei.wage;

import java.awt.*;
import javax.swing.*;

public class ObjectViewer extends JPanel {
	private static final long serialVersionUID = 1L;
	private Design design;
	private TexturePaint[] patterns;
	private boolean maskMode;

	public ObjectViewer(Design design, TexturePaint[] patterns) {
		this.design = design;
		this.patterns = patterns;
	}

	public void setMaskMode(boolean m) {
		maskMode = m;
	}
	
	public void setDesign(Design design) {
		this.design = design;
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
}
