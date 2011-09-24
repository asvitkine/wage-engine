package info.svitkine.alexei.wage;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

public class ConsoleView extends JComponent implements KeyListener, Console {
	private List<String> lines;
	private StringBuilder currentLine;
	private PrintStream out;
	private PipedInputStream in;
	private PrintWriter inPipe;

	public ConsoleView() {
		clear();
		out = new PrintStream(new ConsoleWriter());
		
		PipedOutputStream outPipe = new PipedOutputStream();
		inPipe = new PrintWriter(outPipe);
		in = new PipedInputStream();
		try {
			outPipe.connect(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// user types stuff... buffer it and readable from |in|		
		// game writes stuff to |out|, is appended
		// full text => text written so far + line of input
		addKeyListener(this);
	}
	
	public InputStream getIn() {
		return in;
	}

	public PrintStream getOut() {
		return out;
	}

	private List<String> computeWrappedLinesForDrawing(int width) {
		FontMetrics m = getFontMetrics(getFont());
		ArrayList<String> wrappedLines = new ArrayList<String>();
		String partialLine = null;
		for (int i = 0; i < lines.size(); i++) {
			String line = (partialLine != null ? partialLine : lines.get(i));
			partialLine = null;
			if (m.stringWidth(line) <= width) {
				wrappedLines.add(line);
			} else {
				int whiteSpaceStart = 0;
				int wordStart = 0;
				boolean wasWhitespace = false;
				for (int j = 0; j < line.length(); j++) {
					boolean whitespace = Character.isWhitespace(line.charAt(j));						
					if (!whitespace) {
						if (whitespace != wasWhitespace)
							wordStart = j;
						String linePart = line.substring(0, j + 1);
						if (m.stringWidth(linePart) > width) {
							// TODO: handle case where whole word won't fit
							linePart = line.substring(0, whiteSpaceStart);
							wrappedLines.add(linePart);
							partialLine = line.substring(wordStart).trim();
							i--;
							break;
						}
						wasWhitespace = false;
					} else if (whitespace != wasWhitespace) {
						whiteSpaceStart = j;
					}
					wasWhitespace = whitespace;
				} 
			}
		}
		wrappedLines.add(currentLine.toString());
		return wrappedLines;
	}

	
	@Override
	public void paint(Graphics g) {
		int horizontalInset = 2;
		int verticalInset = 10;
		int width = getWidth() - horizontalInset * 2;
		int y = verticalInset;
		int lineHeight = getFontMetrics(getFont()).getHeight();
		// TODO: Cache wrapped lines and only re-calc them on size change?
		for (String line : computeWrappedLinesForDrawing(width)) {
			g.drawString(line, horizontalInset, y);
			y += lineHeight;
		}
	}

	public void clear() {
		lines = new ArrayList<String>();
		currentLine = new StringBuilder();
	}

	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(KeyEvent event) {
		char c = event.getKeyChar();
		if (c != KeyEvent.CHAR_UNDEFINED) {
			currentLine.append(c);
			if (c == '\n') {
				String line = currentLine.toString();
				currentLine = new StringBuilder();
				lines.add(line);
				inPipe.append(line);
			}
			repaint();
		}
	}

	private class ConsoleWriter extends java.io.OutputStream {
		private ByteArrayOutputStream buffer;

		public ConsoleWriter() {
			buffer = new ByteArrayOutputStream();
		}

		public synchronized void write(int b) {
			buffer.write(b);
			if (b == '\n') {
				flush();
			}
		}

		public synchronized void flush() {
			byte[] data = buffer.toByteArray();
			if (data.length > 0) {
				String str = new String(buffer.toByteArray());
				buffer = new ByteArrayOutputStream();
				lines.add(str);
				repaint();
			}
		}

		public void close() {
			flush();
		}
	}

	public void postUpdateUI() {
		repaint();
	}
}
