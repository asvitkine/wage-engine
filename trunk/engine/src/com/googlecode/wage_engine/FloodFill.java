package com.googlecode.wage_engine;

import java.util.LinkedList;

public class FloodFill<T> {
	private LinkedList<int[]> queue;
	private boolean[][] visited;
	private Canvas<T> canvas;
	private T search;
	private T replace;

	public static interface Canvas<T> {
		public int width();
		public int height();
		public T get(int x, int y);
		public void set(int x, int y, T t);
	}

	public static class ArrayCanvas<T> implements Canvas<T> {
		private T[][] array;
		public ArrayCanvas(T[][] array) {
			this.array = array;
		}
		public T get(int x, int y) {
			return array[x][y];
		}
		public void set(int x, int y, T t) {
			array[x][y] = t;
		}
		public int width() {
			return array.length;
		}
		public int height() {
			return array[0].length;
		}
	}

	public FloodFill(Canvas<T> canvas, T search, T replace) {
		this.canvas = canvas;
		this.search = search;
		this.replace = replace;
		queue = new LinkedList<int[]>();
		visited = new boolean[canvas.width()][canvas.height()];
	}

	public FloodFill(T[][] canvas, T search, T replace) {
		this(new ArrayCanvas<T>(canvas), search, replace);
	}

	public void addSeed(int x, int y) {
		if (x >= 0 && x < canvas.width() && y >= 0 && y < canvas.height()) {
			if (!visited[x][y] && canvas.get(x, y) == search) {
				visited[x][y] = true;
				canvas.set(x, y, replace);
				queue.add(new int[] {x,y});
			}
		}
	}

	public void fill() {
		while (!queue.isEmpty()) {
			int[] xy = queue.removeFirst();
			int x = xy[0], y = xy[1];
			addSeed(x    , y - 1);
			addSeed(x - 1, y    );
			addSeed(x    , y + 1);
			addSeed(x + 1, y    );
		}
	}
}
