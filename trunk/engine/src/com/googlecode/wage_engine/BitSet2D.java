package com.googlecode.wage_engine;

import java.util.BitSet;

public class BitSet2D {
	private BitSet bitSet;
	private int width;
	private int height;

	public BitSet2D(int width, int height, boolean initialValue) {
		this.bitSet = new BitSet(width * height);
		this.width = width;
		this.height = height;
		if (initialValue == true)
			bitSet.set(0, width * height); 
	}

	public boolean get(int x, int y) {
		return bitSet.get(y*height + x);
	}

	public void set(int x, int y, boolean value) {
		bitSet.set(y*height + x, value);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
