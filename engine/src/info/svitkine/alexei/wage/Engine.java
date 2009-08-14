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
					world.move(obj, scene);
				} else {
					Chr chr = world.getChrs().get(location);
					if (chr == null) {
						System.out.println(obj.getName());
						System.out.println(obj.getSceneOrOwner());
					} else {
						// TODO: Add check for max items.
						world.move(obj, chr);
					}
				}
			}
		}
		for (Chr chr : world.getOrderedChrs()) {
			if (!chr.getInitialScene().equals(World.STORAGE)) {
				Scene scene = getSceneByName(chr.getInitialScene().toLowerCase());
				// TODO: We can't put two monsters in the same scene.
				if (scene != null) {
					world.move(chr, scene);
				}
			}
		}
		Chr player = world.getPlayer();
		Context context = world.getPlayerContext();
		context.setPlayerVariable(Context.PHYS_ACC_BAS, player.getPhysicalAccuracy());
		context.setPlayerVariable(Context.PHYS_ACC_CUR, player.getPhysicalAccuracy());
		context.setPlayerVariable(Context.PHYS_ARM_BAS, player.getNaturalArmor());
		context.setPlayerVariable(Context.PHYS_ARM_CUR, player.getNaturalArmor());
		context.setPlayerVariable(Context.PHYS_HIT_BAS, player.getPhysicalHp());
		context.setPlayerVariable(Context.PHYS_HIT_CUR, player.getPhysicalHp());
		context.setPlayerVariable(Context.PHYS_SPE_BAS, player.getRunningSpeed());
		context.setPlayerVariable(Context.PHYS_SPE_CUR, player.getRunningSpeed());
		context.setPlayerVariable(Context.PHYS_STR_BAS, player.getPhysicalStrength());
		context.setPlayerVariable(Context.PHYS_STR_CUR, player.getPhysicalStrength());
		context.setPlayerVariable(Context.SPIR_ACC_BAS, player.getSpiritualAccuracy());
		context.setPlayerVariable(Context.SPIR_ACC_CUR, player.getSpiritualAccuracy());
		context.setPlayerVariable(Context.SPIR_ARM_BAS, player.getResistanceToMagic());
		context.setPlayerVariable(Context.SPIR_ARM_CUR, player.getResistanceToMagic());
		context.setPlayerVariable(Context.SPIR_HIT_BAS, player.getSpiritialHp());
		context.setPlayerVariable(Context.SPIR_HIT_CUR, player.getSpiritialHp());
		context.setPlayerVariable(Context.SPIR_STR_BAS, player.getSpiritualStength());
		context.setPlayerVariable(Context.SPIR_STR_CUR, player.getSpiritualStength());
		player.setVisits(1);
		System.out.println("Player begins in " + player.getCurrentScene().getName());
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
				// TODO: what if the "look" script moves the player again?
				if (playerScene.getChrs().size() == 2) {
					Chr a = playerScene.getChrs().get(1);
					Chr b = playerScene.getChrs().get(1);
					encounter(world.getPlayer(), world.getPlayer() == a ? b : a);
				}
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
		if (text != null && text.length() > 0) {
			hadOutput = true;
			out.append(text);
			out.append("\n");
		}
	}

	public void playSound(String soundName) {
		if (soundName != null) {
			Sound sound = world.getSounds().get(soundName.toLowerCase());
			if (sound != null)
				sound.play();
		}
	}

	public void setMenu(String menuData) {
		callbacks.setCommandsMenu(menuData);
	}

	public void onMove(MoveEvent event) {
		Chr player = world.getPlayer();
		if (event.getWhat() != player && event.getWhat() instanceof Chr) {
			if (event.getTo() == player.getCurrentScene() && event.getTo() != world.getStorageScene()) {
				Chr chr = (Chr) event.getWhat();
				encounter(player, chr);
			}
		}
	}

	private void encounter(Chr player, Chr chr) {
		StringBuilder sb = new StringBuilder("You encounter ");
		if (!chr.isNameProperNoun())
			sb.append(TextUtils.prependDefiniteArticle(chr.getName()));
		else
			sb.append(chr.getName());
		sb.append(".");
		appendText(sb.toString());
		if (chr.getInitialComment() != null && chr.getInitialComment().length() > 0)
			appendText(chr.getInitialComment());
		react(chr, player);
	}
	
	private void react(Chr npc, Chr player) {
		// TODO: The NPC may also decide to just move instead of attacking...
		// "The Invisible Warrior runs west."
		// then, when you follow it, it could escape: "The Invisible Warrior escapes!"
		Weapon[] weapons = npc.getWeapons();
		Weapon weapon = weapons[(int) (Math.random()*weapons.length)];
		performAttack(npc, player, weapon);
	}

	public void performAttack(Chr attacker, Chr victim, Weapon weapon) {
		String[] targets = new String[] { "chest", "head", "side" };
		String target = targets[(int) (Math.random()*targets.length)];
		if (!attacker.isPlayerCharacter())
			appendText(getAttackMessage(attacker, victim, weapon, target));
		playSound(weapon.getSound());
		// TODO: roll some dice
		if (Math.random() > 0.5) {
			appendText("A miss!");
		} else {
			appendText("A hit to the " + target + ".");
			playSound(attacker.getScoresHitSound());
			appendText(victim.getReceivesHitComment());
			playSound(victim.getReceivesHitSound());
		}
		if (attacker.isPlayerCharacter()) {
			react(victim, attacker);
		}
	}
	
	private String getAttackMessage(Chr attacker, Chr victim, Weapon weapon, String target) {
		StringBuilder sb = new StringBuilder();
		if (!attacker.isNameProperNoun())
			sb.append("The ");
		sb.append(String.format("%s %ss ", attacker.getName(), weapon.getOperativeVerb()));
		sb.append(TextUtils.prependGenderSpecificPronoun(weapon.getName(), attacker.getGender()));
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
