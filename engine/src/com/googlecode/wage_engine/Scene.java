package com.googlecode.wage_engine;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public interface Scene {
	public static final int NORTH = 0;
	public static final int SOUTH = 1;
	public static final int EAST = 2;
	public static final int WEST = 3;
	
	public static final int PERIODIC = 0;
	public static final int RANDOM = 1;

	public static class State {
		private int worldX;
		private int worldY;
		private boolean[] blocked = new boolean[4];
		private int soundFrequency; // times a minute, max 3600
		private int soundType;
		private boolean visited;
		private List<Obj> objs = new ArrayList<Obj>();
		private List<Chr> chrs = new ArrayList<Chr>();

		public State(Scene scene) {
			worldX = scene.getWorldX();
			worldY = scene.getWorldY();
			blocked[Scene.NORTH] = scene.isDirBlocked(Scene.NORTH);
			blocked[Scene.SOUTH] = scene.isDirBlocked(Scene.SOUTH);
			blocked[Scene.EAST] = scene.isDirBlocked(Scene.EAST);
			blocked[Scene.WEST] = scene.isDirBlocked(Scene.WEST);
			soundFrequency = scene.getSoundFrequency();
			soundType = scene.getSoundType();
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

		public void setDirBlocked(int dir, boolean blocked) {
			this.blocked[dir] = blocked;
		}
		
		public boolean isDirBlocked(int dir) {
			return blocked[dir];
		}

		public int getSoundFrequency() {
			return soundFrequency;
		}

		public void setSoundFrequency(int soundFrequency) {
			this.soundFrequency = soundFrequency;
		}

		public int getSoundType() {
			return soundType;
		}

		public void setSoundType(int soundType) {
			this.soundType = soundType;
		}

		public List<Chr> getChrs() {
			return chrs;
		}

		public List<Obj> getObjs() {
			return objs;
		}

		public void setVisited(boolean visited) {
			this.visited = true;
		}
		
		public boolean wasVisited() {
			return visited;
		}
	}

	public State getState();
	public void setState(State state);
	
	public int getSoundFrequency();
	public Rectangle getDesignBounds();
	public Rectangle getTextBounds();
	public String getDirMessage(int dir);
	public boolean isDirBlocked(int dir);
	public Design getDesign();
	public int getIndex();
	public String getName();
	public short getResourceID();
	public String getText();
	public Script getScript();
	public int getSoundType();
	public int getWorldX();
	public int getWorldY();
	public String getSoundName();
	public int getFontSize();
	public int getFontType();
	public String getFontName();
}
