package info.svitkine.alexei.wage;

import info.svitkine.alexei.wage.World.MoveEvent;
import info.svitkine.alexei.wage.World.MoveListener;

import java.io.PrintStream;


public class Engine implements Script.Callbacks, MoveListener {
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
		world.addMoveListener(this);
	}

	private Scene getSceneByName(String location) {
		Scene scene;
		if (location.equals("random@")) {
			scene = world.getOrderedScenes().get((int) (Math.random() * world.getOrderedScenes().size()));
		} else {
			scene = world.getScenes().get(location);
		}
		return scene;
	}

	private void performInitialSetup() {
		for (Obj obj : world.getOrderedObjs())
			world.move(obj, world.getStorageScene());
		for (Chr chr : world.getOrderedChrs())
			world.move(chr, world.getStorageScene());
		for (Obj obj : world.getOrderedObjs()) {
			if (!obj.getSceneOrOwner().equals(World.STORAGE)) {
				String location = obj.getSceneOrOwner().toLowerCase();
				Scene scene = getSceneByName(location);
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
				Scene scene = getSceneByName(chr.getInitialScene().toLowerCase());
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

	public void onMove(MoveEvent event) {
		Chr player = world.getPlayer();
		if (event.getWhat() != player && event.getWhat() instanceof Chr) {
			if (event.getTo() == player.getCurrentScene()) {
				Chr chr = (Chr) event.getWhat();
				StringBuilder sb = new StringBuilder("You encounter ");
				if (!chr.isNameProperNoun())
					sb.append("a ");
				sb.append(chr.getName());
				sb.append(".");
				appendText(sb.toString());
				if (chr.getInitialComment() != null && chr.getInitialComment().length() > 0)
					appendText(chr.getInitialComment());
				performAttack(chr, player);
			}
		}
	}
	
	private void performAttack(Chr attacker, Chr victim) {
		String verb = attacker.getOperativeVerb1();
		String weapon = attacker.getNativeWeapon1();
		appendText(getAttackMessage(attacker, victim, verb, weapon, "chest"));
		// TODO: roll some dice
		// TODO: create interface weapon and getWeapon1() getWeapon2() adapters for chr
		appendText("A miss!");
	}

	private String getAttackMessage(Chr attacker, Chr victim, String verb, String weapon, String target) {
		StringBuilder sb = new StringBuilder();
		if (!attacker.isNameProperNoun())
			sb.append("The ");
		sb.append(String.format("%s %ss ", attacker.getName(), verb));
		if (attacker.getGender() == Chr.GENDER_HE)
			sb.append("his ");
		else if (attacker.getGender() == Chr.GENDER_SHE)
			sb.append("her ");
		else
			sb.append("its ");
		sb.append(weapon);
		sb.append(" at ");
		if (!victim.isNameProperNoun())
			sb.append("the ");
		sb.append(victim.getName());
		sb.append("'s ");
		sb.append(target);
		sb.append(".");
		return sb.toString();
	}
}
