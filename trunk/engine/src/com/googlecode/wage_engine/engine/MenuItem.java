package com.googlecode.wage_engine.engine;

import java.awt.Font;

public class MenuItem {
	public static final int BOLD = 1;
	public static final int ITALIC = 2;
	public static final int UNDERLINE = 4;
	public static final int OUTLINE = 8;
	public static final int SHADOW = 16;
	public static final int CONDENSED = 32;
	public static final int EXTENDED = 64;
	
	private String text;
	private int style;
	private char shortcut;
	private boolean enabled;

	public MenuItem(String text, int style, char shortcut, boolean enabled) {
		this.text = text;
		this.style = style;
		this.shortcut = shortcut;
		this.enabled = enabled;
	}
	
	public MenuItem(String text, int style, char shortcut) {
		this(text, style, shortcut, true);
	}
	
	public MenuItem(String text, int style) {
		this(text, style, (char) 0);
	}
	
	public MenuItem(String text) {
		this(text, 0, (char) 0);
	}

	public String getText() {
		return text;
	}
	
	public int getStyle() {
		return style;
	}
	
	public char getShortcut() {
		return shortcut;
	}
	
	public boolean hasShortcut() {
		return shortcut != 0;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void performAction() {
	}

	@Deprecated
	public int getFontStyle() {
		int fontStyle = 0;
		if ((style & BOLD) != 0)
			fontStyle |= Font.BOLD;
		if ((style & ITALIC) != 0)
			fontStyle |= Font.ITALIC;
		return fontStyle;
	}
}
