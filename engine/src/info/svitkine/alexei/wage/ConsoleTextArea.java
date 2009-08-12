package info.svitkine.alexei.wage;

/*
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Rhino JavaScript Debugger code, released
 * November 21, 2000.
 *
 * The Initial Developer of the Original Code is
 * See Beyond Corporation.
 * Portions created by the Initial Developer are Copyright (C) 2000
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Christopher Oliver
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU General Public License Version 2 or later (the "GPL"), in which
 * case the provisions of the GPL are applicable instead of those above. If
 * you wish to allow use of your version of this file only under the terms of
 * the GPL and not to allow others to use your version of this file under the
 * MPL, indicate your decision by deleting the provisions above and replacing
 * them with the notice and other provisions required by the GPL. If you do
 * not delete the provisions above, a recipient may use your version of this
 * file under either the MPL or the GPL.
 *
 * ***** END LICENSE BLOCK *****
 */

import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Document;
import javax.swing.text.Segment;

class ConsoleWrite implements Runnable {
	private ConsoleTextArea textArea;
	private String str;

	public ConsoleWrite(ConsoleTextArea textArea, String str) {
		this.textArea = textArea;
		this.str = str;
	}

	public void run() {
		textArea.write(str);
	}
}

class ConsoleWriter extends java.io.OutputStream {
	private ConsoleTextArea textArea;
	private ByteArrayOutputStream buffer;

	public ConsoleWriter(ConsoleTextArea textArea) {
		this.textArea = textArea;
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
			SwingUtilities.invokeLater(new ConsoleWrite(textArea, str));
		}
	}

	public void close() {
		flush();
	}
}

public class ConsoleTextArea extends JTextArea implements KeyListener, CaretListener, DocumentListener {
	private ConsoleWriter writer;
	private PrintStream out;
	private PrintWriter inPipe;
	private PipedInputStream in;
	private int outputMark = 0;

	public ConsoleTextArea() {
		super();
		writer = new ConsoleWriter(this);
		out = new PrintStream(writer);
		PipedOutputStream outPipe = new PipedOutputStream();
		inPipe = new PrintWriter(outPipe);
		in = new PipedInputStream();
		try {
			outPipe.connect(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		getDocument().addDocumentListener(this);
		addKeyListener(this);
		addCaretListener(this);
		setLineWrap(true);
	}

	@Override
	public synchronized void select(int start, int end) {
		requestFocus();
		super.select(start, end);
	}

	private synchronized void returnPressed() {
		Document doc = getDocument();
		int len = doc.getLength();
		Segment segment = new Segment();
		try {
			doc.getText(outputMark, len - outputMark, segment);
		} catch (javax.swing.text.BadLocationException ignored) {
			ignored.printStackTrace();
		}
		inPipe.write(segment.array, segment.offset, segment.count);
		append("\n");
		outputMark = doc.getLength();
		inPipe.write("\n");
		inPipe.flush();
		writer.flush();
	}

	public synchronized void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		if (code == KeyEvent.VK_BACK_SPACE || code == KeyEvent.VK_LEFT) {
			if (getSelectionStart() < getSelectionEnd()) {
				if (getSelectionStart() < outputMark) {
					e.consume();
				}
			} else if (outputMark == getCaretPosition()) {
				e.consume();
			}
		} else if (code == KeyEvent.VK_DELETE) {
			if (getSelectionStart() < getSelectionEnd() && getSelectionStart() < outputMark) {
				e.consume();
			}
		} else if (code == KeyEvent.VK_HOME) {
			int caretPos = getCaretPosition();
			if (caretPos == outputMark) {
				e.consume();
			} else if (caretPos > outputMark) {
				if (!e.isControlDown()) {
					if (e.isShiftDown()) {
						moveCaretPosition(outputMark);
					} else {
						setCaretPosition(outputMark);
					}
					e.consume();
				}
			}
		} else if (code == KeyEvent.VK_ENTER) {
			returnPressed();
			e.consume();
		} else if (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN || code == KeyEvent.VK_TAB) {
			e.consume();
		}
	}

	public synchronized void keyTyped(KeyEvent e) {
		int code = e.getKeyCode();
		if (code == KeyEvent.VK_BACK_SPACE) {
			if (getSelectionStart() < getSelectionEnd()) {
				if (getSelectionStart() < outputMark) {
					e.consume();
				}
			} else if (outputMark == getCaretPosition()) {
				e.consume();
			}
		} else if (code == KeyEvent.VK_DELETE) {
			if (getSelectionStart() < getSelectionEnd() && getSelectionStart() < outputMark) {
				e.consume();
			}
		} else if (getCaretPosition() < outputMark) {
			setCaretPosition(outputMark);
		}
	}

	public synchronized void keyReleased(KeyEvent e) {
	}

	public synchronized void write(String str) {
		int start = getSelectionStart();
		int end = getSelectionEnd();
		insert(str, outputMark);
		int len = str.length();
		outputMark += len;
		select(start + len, end + len);
	}

	public synchronized void insertUpdate(DocumentEvent e) {
		int len = e.getLength();
		int off = e.getOffset();
		if (outputMark > off) {
			outputMark += len;
		}
	}

	public synchronized void removeUpdate(DocumentEvent e) {
		int len = e.getLength();
		int off = e.getOffset();
		if (outputMark > off) {
			if (outputMark >= off + len) {
				outputMark -= len;
			} else {
				outputMark = off;
			}
		}
	}

	public synchronized void postUpdateUI() {
		// this attempts to cleanup the damage done by updateComponentTreeUI
		int start = getSelectionStart();
		int end = getSelectionEnd();
		requestFocus();
		setCaret(getCaret());
		if (start < outputMark)
			select(outputMark, outputMark);
		else
			select(start, end);
	}

	public void changedUpdate(DocumentEvent e) {}

	public synchronized void caretUpdate(CaretEvent e) {
		if (e.getMark() == e.getDot() && e.getMark() < outputMark) {
			setCaretPosition(outputMark);
		}
	}

	public InputStream getIn() {
		return in;
	}

	public PrintStream getOut() {
		return out;
	}
}
