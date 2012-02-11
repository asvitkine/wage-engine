package com.googlecode.wage_engine;

import java.awt.Rectangle;
import java.awt.event.ActionListener;

public class GameOverDialog extends Dialog {
	public GameOverDialog(ActionListener actionListener, String message) {
		super(actionListener, message,
		      new DialogButton[] { new DialogButton("OK", new Rectangle(112, 67, 68, 28)) },
		      0);
	}
}
