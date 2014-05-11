package com.googlecode.wage_engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import com.googlecode.wage_engine.engine.Canvas;
import com.googlecode.wage_engine.engine.Design;

public class DesignRenderer {
	private TexturePaint[] patterns;
	private HashMap<Design, BufferedImage> cachedImages;
	private HashMap<Design, BufferedImage> cachedMaskImages;

	public DesignRenderer(TexturePaint[] patterns) {
		this.patterns = patterns;
		this.cachedImages = new HashMap<Design, BufferedImage>();
		this.cachedMaskImages = new HashMap<Design, BufferedImage>();
	}
	
	private BufferedImage getMaskImage(Design design) {
		Rectangle bounds = design.getBounds();
		BufferedImage maskImage = cachedMaskImages.get(design);
		if (maskImage == null) {
			maskImage = new BufferedImage(1024, 1024, BufferedImage.TYPE_BYTE_BINARY);
			try {
				Graphics2D g2d = maskImage.createGraphics();
				g2d.translate(-bounds.x, -bounds.y);
				Canvas canvas = new Graphics2DCanvas(g2d);
				design.paint(canvas, null, true);
				cachedMaskImages.put(design,  maskImage);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return maskImage;
	}
	
	
	public void paintDesignMask(Design design, Graphics2D g) {
		Rectangle bounds = design.getBounds();
		BufferedImage maskImage = getMaskImage(design);
		g.setColor(Color.BLACK);
		for (int x = 0; x < bounds.width; x++) {
			for (int y = 0; y < bounds.height; y++) {
				if (isPointOpaqueImpl(maskImage, bounds, x, y)) {
					g.fillRect(x, y, 1, 1);
				}
			}
		}
	}
	
	private boolean isPointOpaqueImpl(BufferedImage maskImage, Rectangle bounds, int x, int y) {
		if (x >= bounds.x && y >= bounds.y && x < bounds.width && y < bounds.height) {
			return (maskImage.getRGB(x - bounds.x, y - bounds.y) & 0xFF) == 0;	
		}
		return false;
	}
	
	public boolean isPointOpaque(Design design, int x, int y) {
		BufferedImage maskImage = getMaskImage(design);
		Rectangle bounds = design.getBounds();
		return isPointOpaqueImpl(maskImage, bounds, x, y);
	}
	
	public void paintDesign(Design design, Graphics2D g) {
		Rectangle bounds = design.getBounds();
		if (bounds.width == 0 || bounds.height == 0)
			return;
		BufferedImage image = cachedImages.get(design);
		if (image == null) {
		    image = g.getDeviceConfiguration().createCompatibleImage(
		    	bounds.width, bounds.height, Transparency.BITMASK);
			try {
				Graphics2D g2d = image.createGraphics();
				g2d.translate(-bounds.x, -bounds.y);
				Canvas canvas = new Graphics2DCanvas(g2d);
				design.paint(canvas, patterns, false);
				cachedImages.put(design, image);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		g.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, null);
	}
}
