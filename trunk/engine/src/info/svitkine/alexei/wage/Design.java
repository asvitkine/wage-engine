package info.svitkine.alexei.wage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Design {
	private byte[] data;
	private Rectangle bounds;
	private BufferedImage image;
	private BufferedImage maskImage;

	public Design(byte[] data) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
		try {
			this.data = new byte[in.readShort() - 2];
			System.arraycopy(data, 2, this.data, 0, this.data.length);
			//System.err.println("Extra = " + (data.length - this.data.length - 2));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setBounds(Rectangle bounds) {
		this.bounds = new Rectangle(bounds);
	}
	
	public Rectangle getBounds() {
		return new Rectangle(bounds);
	}
	
	private void paintShape(Graphics2D g2d, TexturePaint[] patterns,
			Shape outer, Shape inner, byte borderFillType, byte fillType) {
		Area border;
		if (borderFillType <= patterns.length && borderFillType > 0) {
			border = new Area(outer);
			border.subtract(new Area(inner));
			g2d.setPaint(patterns[borderFillType - 1]);
			g2d.fill(border);
		}
		if (fillType <= patterns.length && fillType > 0) {
			g2d.setPaint(patterns[fillType - 1]);
			g2d.fill(inner);
		}
	}
	
	private void realPaint(Graphics2D g2d, TexturePaint[] patterns, boolean mask) throws IOException {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
		if (mask) {
			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, bounds.width, bounds.height);
			g2d.setColor(Color.BLACK);
		}
		while (in.available() > 0) {
			byte fillType = in.readByte();
			byte borderThickness = in.readByte();
			byte borderFillType = in.readByte();
			int type = in.readByte();
			short x, y, width, height;
			//System.out.println("Drawing " + type);
			Shape outer, inner;
			switch (type) {
			case 4: // rect
				y = in.readShort();
				x = in.readShort();
				height = (short) (in.readShort() - y);
				width = (short) (in.readShort() - x);
				outer = new Rectangle(x, y, width, height);
				if (mask) {
					g2d.fill(outer);
					break;
				}
				inner = new Rectangle(x+borderThickness, y+borderThickness, width-2*borderThickness, height-2*borderThickness);
				paintShape(g2d, patterns, outer, inner, borderFillType, fillType);
				break;
			case 8: // rounded rect
				y = in.readShort();
				x = in.readShort();
				height = (short) (in.readShort() - y);
				width = (short) (in.readShort() - x);
				short arc = in.readShort();
				outer = new RoundRectangle2D.Float(x, y, width, height, 12, 12);
				if (mask) {
					g2d.fill(outer);
					break;
				}
				inner = new RoundRectangle2D.Float(x+borderThickness, y+borderThickness, width-2*borderThickness, height-2*borderThickness, arc, arc);
				paintShape(g2d, patterns, outer, inner, borderFillType, fillType);
				break;
			case 12: // oval
				y = in.readShort();
				x = in.readShort();
				height = (short) (in.readShort() - y);
				width = (short) (in.readShort() - x);
				outer = new Ellipse2D.Float(x, y, width, height);
				if (mask) {
					g2d.fill(outer);
					break;
				}
				inner = new Ellipse2D.Float(x+borderThickness, y+borderThickness, width-2*borderThickness, height-2*borderThickness);
				paintShape(g2d, patterns, outer, inner, borderFillType, fillType);
				break;

			case 16: case 20: {
				g2d.setColor(Color.BLACK);
				in.readShort();
			//	System.out.printf("ignored => %d\n", in.readShort());
				int numBytes = in.readShort(); // #bytes used by polygon data, including the numBytes
			//	System.out.println("Num bytes is " + numBytes);
					// Ignoring these values works!!!
				in.readShort(); in.readShort(); in.readShort(); in.readShort();
				/*
					System.out.printf("Ignoring: %d\n", in.readShort());
					System.out.printf("Ignoring: %d\n", in.readShort());
					System.out.printf("Ignoring: %d\n", in.readShort());
					System.out.printf("Ignoring: %d\n", in.readShort());
				*/
					numBytes -= 8;
				int y1 = in.readShort();
				int x1 = in.readShort();
				ArrayList<Integer> xcoords = new ArrayList<Integer>();
				ArrayList<Integer> ycoords = new ArrayList<Integer>();
				//System.out.printf("Start point is (%d,%d)\n", x1, y1);
				numBytes -= 6;
				while (numBytes > 0) {
					int y2 = y1;
					int x2 = x1;
					int b = in.readByte();
				//	System.out.printf("YB = %x\n", b);
					if (b == (byte) 0x80) {
						y2 = in.readShort();
						numBytes -= 3;
					} else {
				//		System.out.println("Y");
						y2 += b;
						numBytes -= 1;
					}
					b = in.readByte();
				//	System.out.printf("XB = %x\n", b);
					if (b == (byte) 0x80) {
						x2 = in.readShort();
						numBytes -= 3;
					} else {
				//		System.out.println("X");
						x2 += b;
						numBytes -= 1;
					}
//					g2d.setColor(colors[c++]);
					//g2d.setColor(Color.black);
					xcoords.add(x1);
					ycoords.add(y1);
				//	System.out.printf("%d %d %d %d\n", x1, y1, x2, y2);
					//g2d.drawLine(x1, y1, x2, y2);
					x1 = x2;
					y1 = y2;
				}
				xcoords.add(x1);
				ycoords.add(y1);
				int npoints = xcoords.size();
				int[] xpoints = new int[npoints];
				int[] ypoints = new int[npoints];
				for (int i = 0; i < npoints; i++) {
					xpoints[i] = xcoords.get(i);
					ypoints[i] = ycoords.get(i);
				}
			//	System.out.println(fillType);
				if (mask) {
					g2d.fillPolygon(xpoints, ypoints, npoints);
					if (borderThickness > 0) {
						Stroke oldStroke = g2d.getStroke();
						g2d.setStroke(new BasicStroke(borderThickness - 0.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
						for (int i = 1; i < npoints; i++)
							g2d.drawLine(xpoints[i-1], ypoints[i-1], xpoints[i], ypoints[i]);
						g2d.setStroke(oldStroke);
					}
					break;
				}
				if (fillType <= patterns.length && fillType > 0) {
					g2d.setPaint(patterns[fillType - 1]);
					g2d.fillPolygon(xpoints, ypoints, npoints);
				}
			//	System.out.println(borderFillType);
				//g2d.setColor(Color.black);
				//if (1==0)
				if (borderThickness > 0 && borderFillType <= patterns.length && borderFillType > 0) {
					g2d.setPaint(patterns[borderFillType - 1]);
					Stroke oldStroke = g2d.getStroke();
					//if (borderThickness != 1)
					g2d.setStroke(new BasicStroke(borderThickness - 0.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
					for (int i = 1; i < npoints; i++)
						g2d.drawLine(xpoints[i-1], ypoints[i-1], xpoints[i], ypoints[i]);
					g2d.setStroke(oldStroke);
				}
				break; }
			case 24: {
				// http://developer.apple.com/technotes/tn/tn1023.html
				//System.out.print("Not the bits!\n");
				int numBytes = in.readShort();
				int y1 = in.readShort();
				int x1 = in.readShort();
				int y2 = in.readShort();
				int x2 = in.readShort();
				int w = x2-x1;
				int h = y2-y1;
				int bpr = (w+7)/8;
			//	System.out.printf("Size=%d\n", numBytes);
			//	System.out.printf("Dims=%d,%d\n",w,h);
			//	System.out.printf("BPR=%d\n",bpr);
				in.mark(Integer.MAX_VALUE);
				byte[] bits = PackBits.unpack(in, bpr*h);
				Color[][] canvas = new Color[w][h];
				DataInputStream bin = new DataInputStream(new ByteArrayInputStream(bits));
				for (int yy = 0; yy < h; yy++) {
					byte[] row = new byte[bpr];
					bin.readFully(row);
					for (int b = 0; b < bpr; b++) {
						for (int bi = 0; bi < 8; bi++) {
							int xx = (b*8+(7-bi));
							if ((row[b] & (1 << bi)) != 0) {
								canvas[xx][yy] = Color.BLACK;
							} else {
								canvas[xx][yy] = Color.WHITE;
							}
						}
					}
				}
				FloodFill<Color> ff = new FloodFill<Color>(canvas, Color.WHITE, null);
				for (int yy = 0; yy < h; yy++) {
					ff.addSeed(0, yy);
					ff.addSeed(w - 1, yy);
				}
				for (int xx = 0; xx < w; xx++) {
					ff.addSeed(xx, 0);
					ff.addSeed(xx, h - 1);
				}
				ff.fill();
				for (int yy = 0; yy < h; yy++) {
					for (int xx = 0; xx < w; xx++) {
						if (canvas[xx][yy] != null) {
							if (!mask) {
								g2d.setColor(canvas[xx][yy]);
							}
							g2d.drawRect(x1+xx, y1+yy, 0, 0);
						}
					}
				}
				in.reset();
				for (int i = 0; i < numBytes - 10; i++)
					in.readByte();
				break; }
			default:
				System.err.println("Unknown type => " + type);
				return;
			}
		}
	}

	public boolean isPointOpaque(int x, int y) {
		if (maskImage == null) {
			maskImage = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_BYTE_BINARY);
			try {
				realPaint(maskImage.createGraphics(), null, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (x >= 0 && y >= 0 && x < maskImage.getWidth() && y < maskImage.getHeight()) {
			return (maskImage.getRGB(x, y) & 0xFF) == 0;	
		}
		return false;
	}

	public void paint(Graphics2D g, TexturePaint[] patterns) {
		if (image == null) {
		    image = g.getDeviceConfiguration().createCompatibleImage(bounds.width, bounds.height, Transparency.BITMASK);
			try {
				realPaint(image.createGraphics(), patterns, false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		g.drawImage(image, 0, 0, bounds.width, bounds.height, null);
	}

	public void paintMask(Graphics2D g) {
		if (maskImage == null) {
			maskImage = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_BYTE_BINARY);
			try {
				realPaint(maskImage.createGraphics(), null, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/*
		g.setColor(Color.BLACK);
		for (int x = 0; x < bounds.width; x++) {
			for (int y = 0; y < bounds.height; y++) {
				if (isPointOpaque(x, y)) {
					g.fillRect(x, y, 1, 1);
				}
			}
		}
		*/
		g.drawImage(maskImage, 0, 0, bounds.width, bounds.height, null);
	}
}
