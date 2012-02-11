package com.googlecode.wage_engine.engine;

import java.util.Arrays;
import java.util.Iterator;

public class MenuBar implements Iterable<Menu> {
	private Menu[] menus;

	public MenuBar(Menu[] menus) {
		this.menus = menus;
	}

	public Menu getMenu(int index) {
		return menus[index];
	}

	public void setMenu(int index, Menu menu) {
		menus[index] = menu;
	}

	public int getMenuCount() {
		return menus.length;
	}

	public Iterator<Menu> iterator() {
		return Arrays.asList(menus).iterator();
	}
}
