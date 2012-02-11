package com.googlecode.wage_engine;

import java.util.Arrays;
import java.util.Iterator;

public class Menu implements Iterable<MenuItem> {
	private String name;
	protected MenuItem[] items;

	public Menu(String name, MenuItem[] items) {
		this.name = name;
		this.items = items;
	}

	public void willShow() {
		/* Can be overridden by subclasses. */
	}

	public MenuItem getItem(int index) {
		return items[index];
	}

	public int getItemCount() {
		return items.length;
	}
	
	public Iterator<MenuItem> iterator() {
		return Arrays.asList(items).iterator();
	}

	public String getName() {
		return name;
	}
}
