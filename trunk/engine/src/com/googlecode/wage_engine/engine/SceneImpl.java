package com.googlecode.wage_engine.engine;

import java.awt.Rectangle;

import com.googlecode.wage_engine.FontNames;

public class SceneImpl implements Scene {
	private int index;
	private String name;
	private short resourceID;
	private Script script;
	private Design design;
	private Rectangle designBounds = new Rectangle();
	private String text;
	private Rectangle textBounds = new Rectangle();
	private int fontSize;
	private int fontType; // 3 => Geneva, 22 => Courier, param to TextFont() function
	private boolean[] blocked = new boolean[4];
	private String[] messages = new String[4];
	private int soundFrequency; // times a minute, max 3600
	private int soundType;
	private String soundName;
	private int worldX;
	private int worldY;

	private State state;

	public State getState() {
		if (state == null)
			state = new State(this);
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
	
	public int getSoundFrequency() {
		return soundFrequency;
	}

	public void setSoundFrequency(int soundFrequency) {
		this.soundFrequency = soundFrequency;
	}

	public Rectangle getDesignBounds() {
		return designBounds == null ? null : new Rectangle(designBounds);
	}

	public void setDesignBounds(Rectangle bounds) {
		this.designBounds = new Rectangle(bounds);
	}

	public Rectangle getTextBounds() {
		return textBounds == null ? null : new Rectangle(textBounds);
	}

	public void setTextBounds(Rectangle bounds) {
		this.textBounds = new Rectangle(bounds);
	}
	
	public void setDirMessage(int dir, String message) {
		messages[dir] = message;
	}
	
	public String getDirMessage(int dir) {
		return messages[dir];
	}

	public void setDirBlocked(int dir, boolean blocked) {
		this.blocked[dir] = blocked;
	}
	
	public boolean isDirBlocked(int dir) {
		return blocked[dir];
	}

	public Design getDesign() {
		return design;
	}

	public void setDesign(Design design) {
		this.design = design;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public short getResourceID() {
		return resourceID;
	}

	public void setResourceID(short resourceID) {
		this.resourceID = resourceID;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
	}

	public int getSoundType() {
		return soundType;
	}

	public void setSoundType(int soundType) {
		this.soundType = soundType;
	}

	public int getWorldX() {
		return worldX;
	}

	public void setWorldX(int worldX) {
		this.worldX = worldX;
	}

	public int getWorldY() {
		return worldY;
	}

	public void setWorldY(int worldY) {
		this.worldY = worldY;
	}

	public String getSoundName() {
		return soundName;
	}

	public void setSoundName(String soundName) {
		this.soundName = soundName;
	}

	public String toString() {
		return getName();
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public int getFontType() {
		return fontType;
	}

	public void setFontType(int fontType) {
		this.fontType = fontType;
	}

	public String getFontName() {
		return FontNames.getFontName(fontType);
	}
}
