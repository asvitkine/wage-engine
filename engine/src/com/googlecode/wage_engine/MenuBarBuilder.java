package com.googlecode.wage_engine;

import java.util.ArrayList;

public class MenuBarBuilder {
	public interface Callbacks {
		public void showAboutDialog();
		public void showOpenDialog();
		public void showSaveDialog();
		public void doNew();
		public void doSave();
		public void doSaveAs();
		public void doRevert();
		public void performCommand(String command);
	}

	private World world;
	private Callbacks callbacks;

	public MenuBarBuilder(World world, Callbacks callbacks) {
		this.world = world;
		this.callbacks = callbacks;
	}

	public MenuBar createMenuBar() {
		Menu[] menus;
		if (!world.isWeaponsMenuDisabled())
			menus = new Menu[5];
		else
			menus = new Menu[4];
		menus[0] = createAppleMenu();
		menus[1] = createFileMenu();
		menus[2] = createEditMenu();
		menus[3] = createCommandsMenu();
		if (!world.isWeaponsMenuDisabled())
			menus[4] = createWeaponsMenu();
		return new MenuBar(menus);
	}

	private Menu createAppleMenu() {
		MenuItem[] items = new MenuItem[] {
			new MenuItem(world.getAboutMenuItemName()) {
				public void performAction() {
					callbacks.showAboutDialog();
				}
			}
		};
		return new Menu("\uF8FF", items);
	}
	
	private Menu createFileMenu() {
		MenuItem[] items = new MenuItem[] {
			new MenuItem("New") {
				public void performAction() {
					callbacks.doNew();
				}
			},
			new MenuItem("Open...") {
				public void performAction() {
					callbacks.showOpenDialog();
				}
			},
			new MenuItem("Close") {
				public void performAction() {
					callbacks.showSaveDialog();
				}
			},
			new MenuItem("Save") {
				public void performAction() {
					callbacks.doSave();
				}
			},
			new MenuItem("Save as...") {
				public void performAction() {
					callbacks.doSaveAs();
				}
			},
			new MenuItem("Revert") {
				public void performAction() {
					callbacks.doRevert();
				}
			},
			new MenuItem("Quit")
		};
		return new Menu("File", items);
	}

	private Menu createEditMenu() {
		MenuItem[] items = new MenuItem[] {
			new MenuItem("Undo", 0, 'Z'),
			null, // separator
			new MenuItem("Cut", 0, 'K'),
			new MenuItem("Copy", 0, 'C'),
			new MenuItem("Paste", 0, 'V'),
			new MenuItem("Clear", 0, 'B'),
		};
		return new Menu("Edit", items);
	}

	private Menu createCommandsMenu() {
		return createMenuFromString(world.getCommandsMenuName(), world.getDefaultCommandsMenu());
	}
	
	public Menu createMenuFromString(String name, String string) {
		String[] items = string.split(";");
		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
		for (String item : items) {
			if (item.equals("(-")) {
				menuItems.add(null); // separator
			} else {
				boolean enabled = true;
				int style = 0;
				char shortcut = 0;
				int index = item.lastIndexOf("/");
				if (index != -1) {
					shortcut = item.substring(index).charAt(1);
					item = item.substring(0, index);
				}
				while (item.length() >= 2 && item.charAt(item.length() - 2) == '<') {
					char c = item.charAt(item.length() - 1);
					if (c == 'B') {
						style |= MenuItem.BOLD;
					} else if (c == 'I') {
						style |= MenuItem.ITALIC;
					} else if (c == 'U') {
						style |= MenuItem.UNDERLINE;
					} else if (c == 'O') {
						style |= MenuItem.OUTLINE;
					} else if (c == 'S') {
						style |= MenuItem.SHADOW;
					} else if (c == 'C') {
						style |= MenuItem.CONDENSED;
					} else if (c == 'E') {
						style |= MenuItem.EXTENDED;
					}
					item = item.substring(0, item.length() - 2);
				}
				if (item.trim().startsWith("(")) {
					enabled = false;
					int loc = item.indexOf("(");
					item = item.substring(0, loc) + item.substring(loc + 1);
				}
				menuItems.add(new MenuItem(item, style, shortcut, enabled) {
					public void performAction() {
						callbacks.performCommand(getText());
					}
				});
			}
		}
		return new Menu(name, menuItems.toArray(new MenuItem[menuItems.size()]));
	}
	
	private Menu createWeaponsMenu() {
		return new Menu(world.getWeaponsMenuName(), new MenuItem[0]) {
			public void willShow() {
				this.items = generateWeaponsMenuItems();
			}
		};
	}
	
	private MenuItem[] generateWeaponsMenuItems() {
		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
		Chr player = world.getPlayer();
		for (Weapon obj : player.getWeapons(true)) {
			if (obj.getType() == Obj.REGULAR_WEAPON ||
				obj.getType() == Obj.THROW_WEAPON ||
				obj.getType() == Obj.MAGICAL_OBJECT) {
				menuItems.add(new MenuItem(obj.getOperativeVerb() + " " + obj.getName()) {
					public void performAction() {
						callbacks.performCommand(getText());
					}
				});
			}
		}
		if (menuItems.size() == 0) {
			menuItems.add(new MenuItem("You have no weapons", 0, (char) 0, false));
		}
		return menuItems.toArray(new MenuItem[menuItems.size()]);
	}
}
