package info.svitkine.alexei.wage;

import info.svitkine.alexei.wage.World.MoveEvent;
import info.svitkine.alexei.wage.World.MoveListener;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;


public class Engine implements Script.Callbacks, MoveListener {
	private World world;
	private StateManager stateManager;
	private Scene lastScene;
	private PrintStream out;
	private int loopCount;
	private int turn;
	private Callbacks callbacks;
	private Chr monster;
	private Chr running;
	private Obj offer;
	private boolean commandWasQuick;
	private int aim = -1;
	private boolean temporarilyHidden;

	public interface Callbacks {
		public void setCommandsMenu(String format);
		public void redrawScene();
		public void clearOutput();
		public void gameOver();
	}

	public Engine(World world, PrintStream out, Callbacks callbacks) {
		this.world = world;
		this.stateManager = new StateManager(world);
		this.out = out;
		this.callbacks = callbacks;
		world.addMoveListener(this);
	}

	private Scene getSceneByName(String location) {
		Scene scene;
		if (location.equals("random@")) {
			scene = world.getRandomScene();
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
		boolean playerPlaced = false;
		for (Chr chr : world.getOrderedChrs()) {
			if (!chr.getInitialScene().equals(World.STORAGE)) {
				Scene scene = getSceneByName(chr.getInitialScene().toLowerCase());
				if (scene != null) {
					world.move(chr, scene);
				} else {
					world.move(chr, world.getRandomScene());
				}
				if (chr.isPlayerCharacter()) {
					playerPlaced = true;
				}
			}
		}
		if (!playerPlaced) {
			world.move(world.getPlayer(), world.getRandomScene());
		}
	}
	
	public void loadState(File file) throws IOException {
		// parse the save file
		stateManager.readSaveData(file);
		
		// uncomment the next line for a human readable dump of information contained in save file on load
		//stateManager.printAll(file.getPath() + "_dump.txt");

		// update the world based on the file
		if (stateManager.updateWorld()) {
			
			//TODO: make sure that armor in the inventory gets put on if we are wearing it

			loopCount = world.getCurrentState().getLoopNum();

			// let the engine know if there is a npc in the current scene
			int presMonHexOffset = world.getCurrentState().getPresCharHexOffset();			
			if (presMonHexOffset != 0xffff) {
				monster = world.getCharByHexOffset((short)presMonHexOffset);
			}

			this.callbacks.clearOutput();

			processTurn("look", null);
		}
	}

	public void saveState(File toFile) throws IOException {
		// updates state variables with current world info
		boolean success = stateManager.updateState(monster, running, loopCount);
		// output state info to disk
		if (success) {
			stateManager.writeSaveData(toFile);
			// TODO: We should get the creator code from the game app.
			Utils.setFileTypeAndCreator(toFile.getAbsolutePath(), "WDOC", "WEDT");
		}	
	}
	
	public void revert() throws IOException {
		if (stateManager.updateWorld() == false) {
			System.err.println("Error reverting to last saved game!");
			return;
		}
		
		//TODO: make sure that armor in the inventory gets put on if we are wearing it
		loopCount = world.getCurrentState().getLoopNum();
		
		// let the engine know if there is a npc in the current scene
		int presMonHexOffset = world.getCurrentState().getPresCharHexOffset();
		if (presMonHexOffset != 0xffff)
			monster = world.getCharByHexOffset((short)presMonHexOffset);
		
		this.callbacks.clearOutput();
		
		processTurn("look",null);
	}

	private void processTurnInternal(String textInput, Object clickInput) {
		Scene playerScene = world.getPlayer().getCurrentScene();
		if (playerScene == world.getStorageScene())
			return;
		boolean shouldEncounter = false;
		if (playerScene != lastScene) {
			loopCount = 0;
			lastScene = playerScene;
			monster = null;
			running = null;
			offer = null;
			for (Chr chr : playerScene.getChrs()) {
				if (!chr.isPlayerCharacter()) {
					monster = chr;
					shouldEncounter = true;
					break;
				}
			}
		}
		boolean monsterWasNull = (monster == null);
		boolean handled = playerScene.getScript().execute(world, loopCount++, textInput, clickInput, this);
		playerScene = world.getPlayer().getCurrentScene();
		if (playerScene == world.getStorageScene())
			return;
		if (playerScene != lastScene) {
			temporarilyHidden = true;
			callbacks.clearOutput();
			regen();
			processTurnInternal("look", null);
			callbacks.redrawScene();
			temporarilyHidden = false;
		} else if (loopCount == 1) {
			if (shouldEncounter && monster != null) {
				encounter(world.getPlayer(), monster);
			}
		} else if (textInput != null && !handled) {
			if (monsterWasNull && monster != null)
				return;
			String[] messages = { "What?", "Huh?" };
			appendText(messages[(int) (Math.random()*messages.length)]);
			commandWasQuick = true;
		}
	}

	public void processTurn(String textInput, Object clickInput) {
		System.out.println("processTurn");
		if (turn == 0) {
			temporarilyHidden = true;
			performInitialSetup();
			callbacks.redrawScene();
			temporarilyHidden = false;
		}
		commandWasQuick = false;
		Scene prevScene = world.getPlayer().getCurrentScene();
		Chr prevMonster = getMonster();
		processTurnInternal(textInput, clickInput);
		Scene playerScene = world.getPlayer().getCurrentScene();
		if (prevScene != playerScene && playerScene != world.getStorageScene()) {
			if (prevMonster != null) {
				boolean followed = false;
				if (getMonster() == null) {
					Set<Scene> scenes = world.getAdjacentScenes(prevMonster.getCurrentScene());
					// TODO: adjacent scenes doesn't contain up/down etc... verify that monsters can't follow these...
					if (scenes.contains(playerScene)) {
						int chance = (int) (Math.random() * 255);
						followed = (chance < prevMonster.getFollowsOpponent());
					}
				}
				if (followed) {
					appendText("%s follows you.", getNameWithDefiniteArticle(prevMonster, true));
					world.move(prevMonster, playerScene);
				} else {
					appendText("You escape %s.", getNameWithDefiniteArticle(prevMonster, false));
				}
			}
		}
		if (!commandWasQuick && getMonster() != null) {
			performCombatAction(getMonster(), world.getPlayer());
		}
		turn++;
	}

	public void appendText(String text, Object... args) {
		appendText(String.format(text, args));
	}
	
	public void setCommandWasQuick() {
		commandWasQuick = true;
	}

	public void appendText(String text) {
		if (text != null && text.length() > 0) {
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
		Scene currentScene = player.getCurrentScene();
		if (currentScene == world.getStorageScene()) {
			callbacks.gameOver();
			return;
		}
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
		if (!temporarilyHidden) {
			if (event.getTo() == currentScene || event.getFrom() == currentScene) {
				callbacks.redrawScene();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
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
	}


	public void setAim(int aim) {
		this.aim = aim;
	}
	
	public void performCombatAction(Chr npc, Chr player) {
		final int WEAPONS = -400;
		final int MAGIC = -300;
		final int RUN = -200;
		final int OFFER = -100;
		RandomHat<Integer> hat = new RandomHat<Integer>();
		boolean winning = npc.getContext().getStatVariable(Context.PHYS_HIT_CUR) >
			player.getContext().getStatVariable(Context.PHYS_HIT_CUR);
		int validMoves = getValidMoveDirections(npc);
		if (winning) {
			if (!world.isWeaponsMenuDisabled()) {
				if (npc.getWeapons().length > 0)
					hat.addTokens(WEAPONS, npc.getWinningWeapons() + 1);
				if (hasMagic(npc))
					hat.addTokens(MAGIC, npc.getWinningMagic() + 1);
			}
			if (validMoves != 0)
				hat.addTokens(RUN, npc.getWinningRun() + 1);
			if (!npc.getInventory().isEmpty())
				hat.addTokens(OFFER, npc.getWinningOffer() + 1);
		} else {
			if (!world.isWeaponsMenuDisabled()) {
				if (npc.getWeapons().length > 0)
					hat.addTokens(WEAPONS, npc.getLosingWeapons() + 1);
				if (hasMagic(npc))
					hat.addTokens(MAGIC, npc.getLosingMagic() + 1);
			}
			if (validMoves != 0)
				hat.addTokens(RUN, npc.getLosingRun() + 1);
			if (!npc.getInventory().isEmpty())
				hat.addTokens(OFFER, npc.getLosingOffer() + 1);
		}
		List<Obj> objs = npc.getCurrentScene().getObjs();
		if (npc.getInventory().size() < npc.getMaximumCarriedObjects()) {
			for (int i = 0; i < objs.size(); i++) {
				Obj o = objs.get(i);
				if (o.getType() != Obj.IMMOBILE_OBJECT) {
					// TODO: I'm not sure what the chance should be here.
					hat.addTokens(i, 123);
				}
			}
		}
		int token = hat.drawToken();
		switch (token) {
			case WEAPONS:
				Weapon[] weapons = npc.getWeapons();
				Weapon weapon = weapons[(int) (Math.random()*weapons.length)];
				// TODO: I think the monster should choose the "best" weapon.
				performAttack(npc, player, weapon);
				break;
			case MAGIC:
				performMagic(npc, player);
				break;
			case RUN:
				performMove(npc, validMoves);
				break;
			case OFFER:
				performOffer(npc, player);
				break;
			default:
				performTake(npc, objs.get(token));
				break;
		}
	}

	private void performTake(Chr npc, Obj obj) {
		appendText("%s picks up the %s.",
			getNameWithDefiniteArticle(npc, true),
			TextUtils.prependIndefiniteArticle(obj.getName()));
		world.move(obj, npc);
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
				o.isNamePlural() ? o.getName() :
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
		running = chr;
		int dx[] = new int[] { 0, 0, 1, -1 };
		int dy[] = new int[] { -1, 1, 0, 0 };
		Scene currentScene = chr.getCurrentScene();
		int destX = currentScene.getWorldX() + dx[dir];
		int destY = currentScene.getWorldY() + dy[dir];
		world.move(chr, world.getSceneAt(destX, destY));
	}

	public void performAttack(Chr attacker, Chr victim, Weapon weapon) {
		if (world.isWeaponsMenuDisabled())
			return;
		String[] targets = new String[] { "head", "chest", "side" };
		int targetIndex = (attacker.isPlayerCharacter() && aim != -1 ? aim : (int) (Math.random()*targets.length));
		String target = targets[targetIndex];
		if (!attacker.isPlayerCharacter()) {
			appendText("%s %ss %s at %s's %s.",
					getNameWithDefiniteArticle(attacker, true),
					weapon.getOperativeVerb(),
					TextUtils.prependGenderSpecificPronoun(weapon.getName(), attacker.getGender()),
					getNameWithDefiniteArticle(victim, false),
					target);
		}
		playSound(weapon.getSound());
		int chance = (int) (Math.random() * 255);
		if (chance < attacker.getPhysicalAccuracy()) {
			appendText("A hit to the %s.", target);
			if (victim.getArmor()[targetIndex] != null) {
				// TODO: Absorb some damage.
				appendText("%s's %s weakens the impact of %s's %s.",
						getNameWithDefiniteArticle(victim, true),
						victim.getArmor()[targetIndex].getName(),
						getNameWithDefiniteArticle(attacker, false),
						weapon.getName());
			}
			playSound(attacker.getScoresHitSound());
			appendText(attacker.getScoresHitComment());
			playSound(victim.getReceivesHitSound());
			appendText(victim.getReceivesHitComment());
			if (weapon.getType() == Obj.THROW_WEAPON) {
				world.move((Obj) weapon, victim.getCurrentScene());
			}
			Context victimContext = victim.getContext();
			int victimHp = victim.getContext().getStatVariable(Context.PHYS_HIT_CUR);
			victimHp -= weapon.getDamage();
			victimContext.setStatVariable(Context.PHYS_HIT_CUR, victimHp);
			if (victimHp < 0) {
				playSound(victim.getDyingSound());
				appendText(victim.getDyingWords());
				appendText("%s is dead.", getNameWithDefiniteArticle(victim, true));
				Context attackerContext = attacker.getContext();
				attackerContext.setKills(attackerContext.getKills() + 1);
				attackerContext.setExperience(attackerContext.getExperience() + 1 + victim.getPhysicalHp());
				if (!victim.isPlayerCharacter() && !victim.getInventory().isEmpty()) {
					for (int i = victim.getInventory().size() - 1; i >= 0; i--) {
						world.move(victim.getInventory().get(i), victim.getCurrentScene());
					}
					appendText(Script.getGroundItemsList(victim.getCurrentScene()));
				}
				world.move(victim, world.getStorageScene());
			} else if (attacker.isPlayerCharacter()) {
				appendText("%s's condition appears to be %s.",
					getNameWithDefiniteArticle(victim, true),
					Script.getPercentMessage(victim, Context.PHYS_HIT_CUR, Context.PHYS_HIT_BAS));
			}
		} else if (weapon.getFailureMessage() != null) {
			appendText(weapon.getFailureMessage());
		} else {
			appendText("A miss!");
		}
		if (weapon instanceof Obj) {
			Obj weaponObj = (Obj) weapon;
			int numberOfUses = weaponObj.getCurrentNumberOfUses();
			if (numberOfUses != -1) {
				numberOfUses--;
				if (numberOfUses > 0) {
					weaponObj.setCurrentNumberOfUses(numberOfUses);
				} else {
					weaponObj.setCurrentNumberOfUses(weaponObj.getNumberOfUses());
					if (weaponObj.getReturnToRandomScene()) {
						world.move(weaponObj, world.getRandomScene());
					} else {
						world.move(weaponObj, world.getStorageScene());
					}
				}
			}
		}
	}

	public static String getNameWithDefiniteArticle(Chr chr, boolean capitalize) {
		StringBuilder sb = new StringBuilder();
		if (!chr.isNameProperNoun())
			sb.append(capitalize ? "The " : "the ");
		sb.append(chr.getName());
		return sb.toString();
	}


	public StateManager getStateManager() {
		return stateManager;
	}
}
