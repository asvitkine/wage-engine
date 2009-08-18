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
	private Chr monster;
	private Obj offer;

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
				if (scene != null) {
					world.move(chr, scene);
				}
			}
		}
	}

	private void processTurnInternal(String textInput, Object clickInput) {
		Scene playerScene = world.getPlayer().getCurrentScene();
		if (playerScene == world.getStorageScene())
			return;
		boolean escaped = false;
		Chr prevMonster = null;
		if (playerScene != lastScene) {
			loopCount = 0;
			lastScene = playerScene;
			prevMonster = monster;
			escaped = (monster != null);
			monster = null;
			offer = null;
			for (Chr chr : playerScene.getChrs()) {
				if (!chr.isPlayerCharacter()) {
					monster = chr;
					break;
				}
			}
		}
		hadOutput = false;
		boolean handled = playerScene.getScript().execute(world, loopCount++, textInput, clickInput, this);
		playerScene = world.getPlayer().getCurrentScene();
		if (playerScene == world.getStorageScene())
			return;
		if (playerScene != lastScene) {
			regen();
			processTurnInternal("look", null);
			if (escaped) {
				// TODO: Monsters can follow!
				appendText("You escape %s.", getNameWithDefiniteArticle(prevMonster, false));
			}
		} else if (loopCount == 1) {
			if (monster != null) {
				encounter(world.getPlayer(), monster);
			}
		} else if (!hadOutput && textInput != null && !handled) {
			String[] messages = { "What?", "Huh?" };
			appendText(messages[(int) (Math.random()*messages.length)]);
		}
	}

	public void processTurn(String textInput, Object clickInput) {
		System.out.println("processTurn");
		if (turn == 0) {
			performInitialSetup();
		}
		processTurnInternal(textInput, clickInput);
		turn++;
	}

	public void appendText(String text, Object... args) {
		appendText(String.format(text, args));
	}

	public void appendText(String text) {
		if (text != null && text.length() > 0) {
			hadOutput = true;
			out.append(text);
			out.append("\n");
		}
	}

	public Chr getMonster() {
		if (monster != null && monster.getCurrentScene() != world.getPlayer().getCurrentScene()) {
			monster = null;
		}
		return monster;
	}

	public Obj getOffer() {
		if (offer != null) {
			Chr owner = offer.getCurrentOwner();
			if (owner == null || owner.isPlayerCharacter() || owner.getCurrentScene() != world.getPlayer().getCurrentScene()) {
				offer = null;
			}
		}
		return offer;
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
			Chr chr = (Chr) event.getWhat();
			if (event.getTo() == world.getStorageScene()) {
				int returnTo = chr.getReturnTo();
				if (returnTo != Chr.RETURN_TO_STORAGE) {
					String returnToSceneName;
					if (returnTo == Chr.RETURN_TO_INITIAL_SCENE) {
						returnToSceneName = chr.getInitialScene().toLowerCase();
					} else {
						returnToSceneName = "random@";
					}
					Scene scene = getSceneByName(returnToSceneName);
					if (scene != null && scene != world.getStorageScene()) {
						world.move(chr, scene);
						return;
					}
				}
			} else if (event.getTo() == player.getCurrentScene()) {
				if (getMonster() == null) {
					monster = chr;
					encounter(player, chr);
				}
			}
		}
	}

	private void encounter(Chr player, Chr chr) {
		StringBuilder sb = new StringBuilder("You encounter ");
		if (!chr.isNameProperNoun())
			sb.append(TextUtils.prependIndefiniteArticle(chr.getName()));
		else
			sb.append(chr.getName());
		sb.append(".");
		appendText(sb.toString());
		if (chr.getInitialComment() != null && chr.getInitialComment().length() > 0)
			appendText(chr.getInitialComment());
		performCombatAction(chr, player);
	}
	
	public void performCombatAction(Chr npc, Chr player) {
		RandomHat<Integer> hat = new RandomHat<Integer>();
		boolean winning = npc.getContext().getStatVariable(Context.PHYS_HIT_CUR) >
			player.getContext().getStatVariable(Context.PHYS_HIT_CUR);
		int validMoves = getValidMoveDirections(npc);
		if (winning) {
			hat.addTokens(0, npc.getWinningWeapons() + 1);
			if (hasMagic(npc))
				hat.addTokens(1, npc.getWinningMagic() + 1);
			if (validMoves != 0)
				hat.addTokens(2, npc.getWinningRun() + 1);
			if (!npc.getInventory().isEmpty())
				hat.addTokens(3, npc.getWinningOffer() + 1);
		} else {
			hat.addTokens(0, npc.getLosingWeapons() + 1);
			if (hasMagic(npc))
				hat.addTokens(1, npc.getLosingMagic() + 1);
			if (validMoves != 0)
				hat.addTokens(2, npc.getLosingRun() + 1);
			if (!npc.getInventory().isEmpty())
				hat.addTokens(3, npc.getLosingOffer() + 1);
		}
		// TODO: npcs can also pick stuff up
		switch (hat.drawToken()) {
			case 0:
				Weapon[] weapons = npc.getWeapons();
				Weapon weapon = weapons[(int) (Math.random()*weapons.length)];
				performAttack(npc, player, weapon);
				break;
			case 1:
				performMagic(npc, player);
				break;
			case 2:
				performMove(npc, validMoves);
				break;
			case 3:
				performOffer(npc, player);
				break;
		}
	}

	public void regen() {
		Context context = world.getPlayerContext();
		int curHp = context.getStatVariable(Context.PHYS_HIT_CUR);
		int maxHp = context.getStatVariable(Context.PHYS_HIT_BAS);
		int delta = maxHp - curHp;
		if (delta > 0) {
			int bonus = (int) (delta / (8 + 2 * Math.random()));
			context.setStatVariable(Context.PHYS_HIT_CUR, curHp + bonus);
		}
	}
	
	private void performOffer(Chr attacker, Chr victim) {
		for (Obj o : attacker.getInventory()) {
			appendText("%s offers %s.",
				getNameWithDefiniteArticle(attacker, true),
				TextUtils.prependIndefiniteArticle(o.getName()));
			offer = o;
			return;
		}
	}

	private boolean hasMagic(Chr chr) {
		return false;
	}

	private void performMagic(Chr attacker, Chr victim) {
	}
	
	private int getValidMoveDirections(Chr npc) {
		int directions = 0;
		Scene currentScene = npc.getCurrentScene();
		int dx[] = new int[] { 0, 0, 1, -1 };
		int dy[] = new int[] { -1, 1, 0, 0 };
		for (int dir = 0; dir < 4; dir++) {
			if (!currentScene.isDirBlocked(dir)) {
				int destX = currentScene.getWorldX() + dx[dir];
				int destY = currentScene.getWorldY() + dy[dir];
				Scene scene = world.getSceneAt(destX, destY);
				if (scene != null && scene.getChrs().size() == 0) {
					directions |= (1 << dir);
				}
			}
 		}
		return directions;
	}

	private void performMove(Chr chr, int validMoves) {
		int[] moves = new int[4];
		int numValidMoves = 0;
		for (int dir = 0; dir < 4; dir++)
			if ((validMoves & (1 << dir)) != 0)
				moves[numValidMoves++] = dir;
		int dir = moves[(int) (Math.random() * numValidMoves)];
		appendText("%s runs %s.", getNameWithDefiniteArticle(chr, true),
			new String[] {"north", "south", "east", "west"}[dir]);
		int dx[] = new int[] { 0, 0, 1, -1 };
		int dy[] = new int[] { -1, 1, 0, 0 };
		Scene currentScene = chr.getCurrentScene();
		int destX = currentScene.getWorldX() + dx[dir];
		int destY = currentScene.getWorldY() + dy[dir];
		world.move(chr, world.getSceneAt(destX, destY));
	}

	public void performAttack(Chr attacker, Chr victim, Weapon weapon) {
		String[] targets = new String[] { "chest", "head", "side" };
		String target = targets[(int) (Math.random()*targets.length)];
		if (!attacker.isPlayerCharacter()) {
			appendText("%s %ss %s at %s's %s.",
					getNameWithDefiniteArticle(attacker, true),
					weapon.getOperativeVerb(),
					TextUtils.prependGenderSpecificPronoun(weapon.getName(), attacker.getGender()),
					getNameWithDefiniteArticle(victim, false),
					target);
		}
		playSound(weapon.getSound());
		// TODO: roll some dice
		if (Math.random() > 0.5) {
			appendText("A miss!");
		} else {
			appendText("A hit to the %s.", target);
			playSound(attacker.getScoresHitSound());
			appendText(victim.getReceivesHitComment());
			playSound(victim.getReceivesHitSound());
			// TODO: Armor can absorb some of the damage, I think.
			victim.setPhysicalHp(victim.getPhysicalAccuracy() - weapon.getDamage());
			if (victim.getPhysicalHp() < 0) {
				appendText("%s is dead.", getNameWithDefiniteArticle(victim, true));
				attacker.getContext().setKills(attacker.getContext().getKills() + 1);
				world.move(victim, world.getStorageScene());
			} else if (attacker.isPlayerCharacter()) {
				appendText("%s's condition appears to be %s.",
					getNameWithDefiniteArticle(victim, true),
					Script.getPercentMessage(victim, Context.PHYS_HIT_CUR, Context.PHYS_HIT_BAS));
			}
		}
		weapon.decrementNumberOfUses();
		if (attacker.isPlayerCharacter() && victim.getCurrentScene() == attacker.getCurrentScene()) {
			performCombatAction(victim, attacker);
		}
	}

	public static String getNameWithDefiniteArticle(Chr chr, boolean capitalize) {
		StringBuilder sb = new StringBuilder();
		if (!chr.isNameProperNoun())
			sb.append(capitalize ? "The " : "the ");
		sb.append(chr.getName());
		return sb.toString();
	}
}
