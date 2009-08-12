package info.svitkine.alexei.wage;

import java.io.PrintStream;


public class Engine implements Script.Callbacks {
	private World world;
	private Scene lastScene;
	private PrintStream out;
	private int loopCount;
	private int turn;
	private boolean hadOutput;
	private Callbacks callbacks;

	public interface Callbacks {
		public void setCommandsMenu(String format);
	}
	
	public Engine(World world, PrintStream out, Callbacks callbacks) {
		this.world = world;
		this.out = out;
		this.callbacks = callbacks;
	}
	
	private void performInitialSetup() {
		for (Obj obj : world.getOrderedObjs())
			world.move(obj, world.getStorageScene());
		for (Chr chr : world.getOrderedChrs())
			world.move(chr, world.getStorageScene());
		for (Obj obj : world.getOrderedObjs()) {
			if (!obj.getSceneOrOwner().equals(World.STORAGE)) {
				// TODO: What about RANDOM@!
				String location = obj.getSceneOrOwner().toLowerCase();
				Scene scene = world.getScenes().get(location);
				if (scene != null) {
					scene.getObjs().add(obj);
					obj.setCurrentScene(scene);
				} else {
					Chr chr = world.getChrs().get(location);
					if (chr == null) {
						System.out.println(obj.getName());
						System.out.println(obj.getSceneOrOwner());
					} else {
						// TODO: Add check for max items. (order of them added?)
						chr.getInventory().add(obj);
						obj.setCurrentOwner(chr);
					}
				}
			}
		}
		for (Chr chr : world.getOrderedChrs()) {
			if (!chr.getInitialScene().equals(World.STORAGE)) {
				// TODO: What about RANDOM@!
				Scene scene = world.getScenes().get(chr.getInitialScene().toLowerCase());
				if (scene != null) {
					world.move(chr, scene);
				}
			}
		}
		world.getPlayer().setVisits(1);
		System.out.println("Player begins in " + world.getPlayer().getCurrentScene().getName());
	}

	public void processTurn(String textInput, Object clickInput) {
		System.out.println("processTurn");
		if (turn == 0) {
			performInitialSetup();
		}
		Scene playerScene = world.getPlayer().getCurrentScene();
		if (playerScene == world.getStorageScene())
			return;
		if (playerScene != lastScene) {
			loopCount = 0;
		}
		hadOutput = false;
		playerScene.getScript().execute(world, loopCount++, textInput, clickInput, this);
		playerScene = world.getPlayer().getCurrentScene();
		if (playerScene == world.getStorageScene())
			return;
		if (playerScene != lastScene) {
			lastScene = playerScene;
			if (turn != 0) {
				loopCount = 0;
				playerScene.getScript().execute(world, loopCount++, "look", null, this);
			}
			if (turn == 0) {
				out.append("\n");
			}
		} else if (!hadOutput && textInput != null) {
			String[] messages = { "What?", "Huh?" };
			appendText(messages[(int) (Math.random()*messages.length)]);
		}
		turn++;
	}

	public void appendText(String text) {
		hadOutput = true;
		out.append(text);
		out.append("\n");
	}

	public void playSound(String soundName) {
		Sound sound = world.getSounds().get(soundName.toLowerCase());
		if (sound != null)
			sound.play();
	}

	public void setMenu(String menuData) {
		callbacks.setCommandsMenu(menuData);
	}
}
