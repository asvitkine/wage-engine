package info.svitkine.alexei.wage;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.Timer;

public class ConsoleView extends JComponent implements KeyListener, Console {
	private List<String> lines;
	private StringBuilder currentLine;
	private PrintStream out;
	private PipedInputStream in;
	private PrintWriter inPipe;
	private Timer cursorToggle;
	private boolean drawCursor;

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

		addKeyListener(this);
		setFocusable(true);
		
		cursorToggle = new Timer(500, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawCursor = !drawCursor;
				repaint(); // TODO: just the caret rect!
			}
		});
		cursorToggle.setRepeats(true);
		cursorToggle.start();
	}
	
	public InputStream getIn() {
		return in;
	}

	public PrintStream getOut() {
		return out;
	}

	private static class Word {
		private String text;
		private int offset;
		public Word(String text, int offset) {
			this.text = text;
			this.offset = offset;
		}
		public String getText() {
			return text;
		}
		public int getStartIndex() {
			return offset;
		}
		public int getEndIndex() {
			return offset + text.length();
		}
	}

	private List<Word> splitWords(String line) {
		ArrayList<Word> words = new ArrayList<Word>();
		int i = 0;
		do {
			while (i < line.length() && Character.isWhitespace(line.charAt(i)))
				i++;
			int j = i;
			while (j < line.length() && !Character.isWhitespace(line.charAt(j)))
				j++;
			if (i != j)
				words.add(new Word(line.substring(i, j), i));
			i = j;
		} while (i < line.length());
		return words;
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
				List<Word> words = splitWords(line);
				String prevLinePart = null;
				for (Word word : words) {
					String linePart = line.substring(0, word.getEndIndex());
					if (m.stringWidth(linePart) > width) {
						if (prevLinePart != null) {
							wrappedLines.add(prevLinePart);
							prevLinePart = null;
							partialLine = line.substring(word.getStartIndex());
						} else {
							// The word is too long. Just add it as a line - it will be truncated when drawn.
							wrappedLines.add(word.getText());
							partialLine = line.substring(word.getEndIndex()).trim();
						}
						i--;
						break;
					}
					prevLinePart = linePart;
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
		FontMetrics m = getFontMetrics(getFont());
		int lineHeight = m.getHeight();
		// TODO: Cache wrapped lines and only re-calc them on size change?
		List<String> wrappedLines = computeWrappedLinesForDrawing(width);
		for (String line : wrappedLines) {
			g.drawString(line, horizontalInset, y);
			y += lineHeight;
		}
		if (drawCursor) {
			String lastLine = wrappedLines.get(wrappedLines.size() - 1);
			int x = horizontalInset + m.stringWidth(lastLine);
			y -= lineHeight * 2 - m.getDescent();
			g.drawLine(x, y, x, y + lineHeight);
		}
	}

	public void clear() {
		lines = new ArrayList<String>();
		currentLine = new StringBuilder();
	}

	public void keyPressed(KeyEvent arg0) {
	}

	public void keyReleased(KeyEvent arg0) {
	}

	public void keyTyped(KeyEvent event) {
		char c = event.getKeyChar();
		if (c == KeyEvent.VK_BACK_SPACE) {
			if (currentLine.length() > 0) {
				currentLine = new StringBuilder(currentLine.substring(0, currentLine.length() - 1));
				repaint();
			}
		} else if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
			currentLine.append(c);
			if (c == '\n') {
				String line = currentLine.toString();
				currentLine = new StringBuilder();
				lines.add(line);
				inPipe.write(line);
				inPipe.write("\n");
				inPipe.flush();
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
