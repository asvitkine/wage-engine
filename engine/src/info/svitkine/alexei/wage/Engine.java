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
	private int aim = Chr.CHEST;
	private int opponentAim = Chr.CHEST; // TODO: use this ... let monsters aim...
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
			wearObjs(chr);
		}
		if (!playerPlaced) {
			world.move(world.getPlayer(), world.getRandomScene());
		}
	}
	
	public static void wearObjs(Chr chr) {
		for (Obj obj : chr.getState().getInventory()) {
			Engine.wearObjIfPossible(chr, obj);
		}
	}

	public static int wearObjIfPossible(Chr chr, Obj obj) {
		if (obj.getType() == Obj.HELMET) {
			if (chr.getState().getArmor(Chr.HEAD_ARMOR) == null) {
				chr.getState().setArmor(Chr.HEAD_ARMOR, obj);
				return Chr.HEAD_ARMOR;
			}
		} else if (obj.getType() == Obj.CHEST_ARMOR) {
			if (chr.getState().getArmor(Chr.BODY_ARMOR) == null) {
				chr.getState().setArmor(Chr.BODY_ARMOR, obj);
				return Chr.BODY_ARMOR;
			}
		} else if (obj.getType() == Obj.SHIELD) {
			if (chr.getState().getArmor(Chr.SHIELD_ARMOR) == null) {
				chr.getState().setArmor(Chr.SHIELD_ARMOR, obj);
				return Chr.SHIELD_ARMOR;
			}
		} else if (obj.getType() == Obj.SPIRITUAL_ARMOR) {
			if (chr.getState().getArmor(Chr.MAGIC_ARMOR) == null) {
				chr.getState().setArmor(Chr.MAGIC_ARMOR, obj);
				return Chr.MAGIC_ARMOR;
			}
		}
		return -1;
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
		boolean success = stateManager.updateState(monster, running, loopCount, aim, opponentAim);
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

		loopCount = world.getCurrentState().getLoopNum();
		monster = world.getCharByHexOffset(world.getCurrentState().getPresCharHexOffset());		
		aim = world.getCurrentState().getAim();
		opponentAim = world.getCurrentState().getOpponentAim();

		callbacks.clearOutput();
		processTurn("look", null);
	}

	private void processTurnInternal(String textInput, Object clickInput) {
		Scene playerScene = world.getPlayerScene();
		if (playerScene == world.getStorageScene())
			return;
		boolean shouldEncounter = false;
		if (playerScene != lastScene) {
			loopCount = 0;
			lastScene = playerScene;
			monster = null;
			running = null;
			offer = null;
			for (Chr chr : playerScene.getState().getChrs()) {
				if (!chr.isPlayerCharacter()) {
					monster = chr;
					shouldEncounter = true;
					break;
				}
			}
		}
		boolean monsterWasNull = (monster == null);
		boolean handled = playerScene.getScript().execute(world, loopCount++, textInput, clickInput, this);
		playerScene = world.getPlayerScene();
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
			callbacks.redrawScene();
			if (shouldEncounter && getMonster() != null) {
				encounter(world.getPlayer(), monster);
			}
		} else if (textInput != null && !handled) {
			if (monsterWasNull && getMonster() != null)
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
			temporarilyHidden = false;
		}
		commandWasQuick = false;
		Scene prevScene = world.getPlayerScene();
		Chr prevMonster = getMonster();
		processTurnInternal(textInput, clickInput);
		Scene playerScene = world.getPlayerScene();
		if (prevScene != playerScene && playerScene != world.getStorageScene()) {
			if (prevMonster != null) {
				boolean followed = false;
				if (getMonster() == null) {
					Set<Scene> scenes = world.getAdjacentScenes(prevMonster.getState().getCurrentScene());
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
		if (monster != null && monster.getState().getCurrentScene() != world.getPlayerScene()) {
			monster = null;
		}
		return monster;
	}

	public Obj getOffer() {
		if (offer != null) {
			Chr owner = offer.getState().getCurrentOwner();
			if (owner == null || owner.isPlayerCharacter() || owner.getState().getCurrentScene() != world.getPlayerScene()) {
				offer = null;
			}
		}
		return offer;
	}
	
	public void playSound(String soundName) {
		if (soundName != null) {
			final Sound sound = world.getSounds().get(soundName.toLowerCase());
			if (sound != null) {
				if (loopCount == 1) {
					sound.play();
				} else {
					new Thread(new Runnable() {
						public void run() {
							sound.play();
						}
					}).start();
				}
			}
		}
	}

	public void setMenu(String menuData) {
		callbacks.setCommandsMenu(menuData);
	}

	public void onMove(MoveEvent event) {
		Chr player = world.getPlayer();
		Scene currentScene = player.getState().getCurrentScene();
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
						// To avoid sleeping twice, return if the above move command would cause a sleep.
						if (scene == currentScene)
							return;
					}
				}
			} else if (event.getTo() == player.getState().getCurrentScene()) {
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
		if (chr.getState().getArmor(Chr.HEAD_ARMOR) != null) {
			Obj obj = chr.getState().getArmor(Chr.HEAD_ARMOR);
			appendText(String.format("%s is wearing %s.", 
				getNameWithDefiniteArticle(chr, true),
				TextUtils.prependIndefiniteArticle(obj.getName())));
		}
		if (chr.getState().getArmor(Chr.BODY_ARMOR) != null) {
			Obj obj = chr.getState().getArmor(Chr.BODY_ARMOR);
			appendText(String.format("%s is protected by %s.",
				TextUtils.getGenderSpecificPronoun(chr.getGender(), true),
				TextUtils.prependGenderSpecificPronoun(obj.getName(), chr.getGender())));
		}
		if (chr.getState().getArmor(Chr.SHIELD_ARMOR) != null) {
			Obj obj = chr.getState().getArmor(Chr.SHIELD_ARMOR);
			appendText(String.format("%s carries %s.",
				TextUtils.getGenderSpecificPronoun(chr.getGender(), true),
				obj.isNamePlural() ? obj.getName() : TextUtils.prependIndefiniteArticle(obj.getName())));
		}
	}

	public void setAim(int aim) {
		this.aim = aim;
	}
	
	public void performCombatAction(Chr npc, Chr player) {
		if (npc.getContext().isFrozen())
			return;
		final int WEAPONS = -400;
		final int MAGIC = -300;
		final int RUN = -200;
		final int OFFER = -100;
		RandomHat<Integer> hat = new RandomHat<Integer>();
		boolean winning = (npc.getState().getCurrentPhysicalHp() > player.getState().getCurrentPhysicalHp());
		int validMoves = getValidMoveDirections(npc);
		// TODO: Figure out under what circumstances we need to add +1
		// for the chance (e.g. only when all values were set to 0?).
		if (winning) {
			if (!world.isWeaponsMenuDisabled()) {
				if (npc.getWeapons(false).length > 0)
					hat.addTokens(WEAPONS, npc.getWinningWeapons() + 1);
				if (npc.getMagicalObjects().length > 0)
					hat.addTokens(MAGIC, npc.getWinningMagic());
			}
			if (validMoves != 0)
				hat.addTokens(RUN, npc.getWinningRun() + 1);
			if (!npc.getState().getInventory().isEmpty())
				hat.addTokens(OFFER, npc.getWinningOffer() + 1);
		} else {
			if (!world.isWeaponsMenuDisabled()) {
				if (npc.getWeapons(false).length > 0)
					hat.addTokens(WEAPONS, npc.getLosingWeapons() + 1);
				if (npc.getMagicalObjects().length > 0)
					hat.addTokens(MAGIC, npc.getLosingMagic());
			}
			if (validMoves != 0)
				hat.addTokens(RUN, npc.getLosingRun() + 1);
			if (!npc.getState().getInventory().isEmpty())
				hat.addTokens(OFFER, npc.getLosingOffer() + 1);
		}
		List<Obj> objs = npc.getState().getCurrentScene().getState().getObjs();
		if (npc.getState().getInventory().size() < npc.getMaximumCarriedObjects()) {
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
				Weapon[] weapons = npc.getWeapons(false);
				Weapon weapon = weapons[(int) (Math.random()*weapons.length)];
				// TODO: I think the monster should choose the "best" weapon.
				performAttack(npc, player, weapon);
				break;
			case MAGIC:
				Obj[] magicalObjects = npc.getMagicalObjects();
				Obj magicalObject = magicalObjects[(int) (Math.random()*magicalObjects.length)];
				// TODO: I think the monster should choose the "best" magic.
				performMagic(npc, player, magicalObject);
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
		Chr player = world.getPlayer();
		int curHp = player.getState().getCurrentPhysicalHp();
		int maxHp = player.getState().getBasePhysicalHp();
		int delta = maxHp - curHp;
		if (delta > 0) {
			int bonus = (int) (delta / (8 + 2 * Math.random()));
			player.getState().setCurrentPhysicalHp(curHp + bonus);
		}
	}

	private void performOffer(Chr attacker, Chr victim) {
		for (Obj o : attacker.getState().getInventory()) {
			/* TODO: choose in a smarter way? */
			appendText("%s offers %s.",
				getNameWithDefiniteArticle(attacker, true),
				o.isNamePlural() ? "some " + o.getName() :
				TextUtils.prependIndefiniteArticle(o.getName()));
			offer = o;
			return;
		}
	}

	private void performHealingMagic(Chr chr, Obj magicalObject) {

		if (!chr.isPlayerCharacter()) {
			appendText("%s %ss %s.",
				getNameWithDefiniteArticle(chr, true),
				magicalObject.getOperativeVerb(),
				TextUtils.prependIndefiniteArticle(magicalObject.getName()));
		}

		int chance = (int) (Math.random() * 255);
		if (chance < magicalObject.getAccuracy()) {
			int type = magicalObject.getAttackType();

			if (type == Obj.HEALS_PHYSICAL_DAMAGE || type == Obj.HEALS_PHYSICAL_AND_SPIRITUAL_DAMAGE) {
				int hp = chr.getState().getCurrentPhysicalHp();
				hp += magicalObject.getDamage();
				chr.getState().setCurrentPhysicalHp(hp);
			}
			
			if (type == Obj.HEALS_SPIRITUAL_DAMAGE || type == Obj.HEALS_PHYSICAL_AND_SPIRITUAL_DAMAGE) {
				int spirit = chr.getState().getCurrentSpiritualHp();
				spirit += magicalObject.getDamage();
				chr.getState().setCurrentSpiritualHp(spirit);
			}

			playSound(magicalObject.getSound());
			appendText(magicalObject.getUseMessage());

			// TODO: what if enemy heals himself?
			if (chr.isPlayerCharacter()) {
				double physicalPercent = (double) chr.getState().getCurrentPhysicalHp() / chr.getState().getBasePhysicalHp();
				double spiritualPercent = (double) chr.getState().getCurrentSpiritualHp() / chr.getState().getBaseSpiritualHp();
				appendText("Your physical condition is " + Script.getPercentMessage(chr, physicalPercent) + ".");
				appendText("Your spiritual condition is " + Script.getPercentMessage(chr, spiritualPercent) + ".");
			}
		}

		decrementUses(magicalObject);
	}

	public void performMagic(Chr attacker, Chr victim, Obj magicalObject) {
		switch (magicalObject.getAttackType()) {
			case Obj.HEALS_PHYSICAL_DAMAGE:
			case Obj.HEALS_SPIRITUAL_DAMAGE:
			case Obj.HEALS_PHYSICAL_AND_SPIRITUAL_DAMAGE:
				performHealingMagic(attacker, magicalObject);
				return;
		}
		performAttack(attacker, victim, (Weapon) magicalObject);
	}

	private int getValidMoveDirections(Chr npc) {
		int directions = 0;
		Scene currentScene = npc.getState().getCurrentScene();
		int dx[] = new int[] { 0, 0, 1, -1 };
		int dy[] = new int[] { -1, 1, 0, 0 };
		for (int dir = 0; dir < 4; dir++) {
			if (!currentScene.isDirBlocked(dir)) {
				int destX = currentScene.getWorldX() + dx[dir];
				int destY = currentScene.getWorldY() + dy[dir];
				Scene scene = world.getSceneAt(destX, destY);
				if (scene != null && scene.getState().getChrs().size() == 0) {
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
		Scene currentScene = chr.getState().getCurrentScene();
		int destX = currentScene.getWorldX() + dx[dir];
		int destY = currentScene.getWorldY() + dy[dir];
		world.move(chr, world.getSceneAt(destX, destY));
	}

	public void performMagicAttack(Chr attacker, Chr victim, Obj weapon) {
		int chance = (int) (Math.random() * 255);
		// TODO: what about object accuracy
		if (chance < attacker.getSpiritualAccuracy()) {
			switch (weapon.getAttackType()) {
			case Obj.FREEZES_OPPONENT:
			case Obj.CAUSES_PHYSICAL_DAMAGE:
			case Obj.CAUSES_SPIRITUAL_DAMAGE:
			case Obj.CAUSES_PHYSICAL_AND_SPIRITUAL_DAMAGE:
				break;
			}			
			appendText("The spell is effective!");
		} else {
			
		}

	}

	public void performAttack(Chr attacker, Chr victim, Weapon weapon) {
		if (world.isWeaponsMenuDisabled())
			return;

		// TODO: verify that a player not aiming will always target the chest??
		int targetIndex = -1;
		String[] targets = new String[] { "head", "chest", "side" };
		if (weapon.getType() != Obj.MAGICAL_OBJECT) {
			targetIndex = (attacker.isPlayerCharacter() ? aim : (opponentAim = 1 + (int) (Math.random()*targets.length))) - 1;
			if (!attacker.isPlayerCharacter()) {
				appendText("%s %ss %s at %s's %s.",
					getNameWithDefiniteArticle(attacker, true),
					weapon.getOperativeVerb(),
					TextUtils.prependGenderSpecificPronoun(weapon.getName(), attacker.getGender()),
					getNameWithDefiniteArticle(victim, false),
					targets[targetIndex]);
			}
		} else if (!attacker.isPlayerCharacter()) {
			appendText("%s %ss %s at %s.",
				getNameWithDefiniteArticle(attacker, true),
				weapon.getOperativeVerb(),
				TextUtils.prependGenderSpecificPronoun(weapon.getName(), attacker.getGender()),
				getNameWithDefiniteArticle(victim, false));
		}

		playSound(weapon.getSound());

		boolean usesDecremented = false;
		int chance = (int) (Math.random() * 255);
		// TODO: what about obj accuracy
		if (chance < attacker.getPhysicalAccuracy()) {
			if (targetIndex != -1) {
				Obj armor = victim.getState().getArmor(targetIndex);
				if (armor != null) {
					// TODO: Absorb some damage.
					appendText("%s's %s weakens the impact of %s's %s.",
						getNameWithDefiniteArticle(victim, true),
						victim.getState().getArmor(targetIndex).getName(),
						getNameWithDefiniteArticle(attacker, false),
						weapon.getName());
					decrementUses(armor);
				} else {
					appendText("A hit to the %s.", targets[targetIndex]);
				}
				playSound(attacker.getScoresHitSound());
				appendText(attacker.getScoresHitComment());
				playSound(victim.getReceivesHitSound());
				appendText(victim.getReceivesHitComment());
			} else if (weapon.getType() == Obj.MAGICAL_OBJECT) {
				appendText(((Obj) weapon).getUseMessage());
				appendText("The spell is effective!");
			}

			boolean causesPhysicalDamage = true;
			boolean causesSpiritualDamage = false;
			boolean freezesOpponent = false;

			if (weapon.getType() == Obj.THROW_WEAPON) {
				world.move((Obj) weapon, victim.getState().getCurrentScene());
			} else if (weapon.getType() == Obj.MAGICAL_OBJECT) {
				int type = (((Obj) weapon).getAttackType());
				causesPhysicalDamage = (type == Obj.CAUSES_PHYSICAL_DAMAGE || type == Obj.CAUSES_PHYSICAL_AND_SPIRITUAL_DAMAGE);
				causesSpiritualDamage = (type == Obj.CAUSES_SPIRITUAL_DAMAGE || type == Obj.CAUSES_PHYSICAL_AND_SPIRITUAL_DAMAGE);
				freezesOpponent = (type == Obj.FREEZES_OPPONENT);
			}

			if (causesPhysicalDamage) {
				int victimHp = victim.getState().getCurrentPhysicalHp();
				victimHp -= weapon.getDamage();
				victim.getState().setCurrentPhysicalHp(victimHp);

				if (weapon instanceof Obj) {
					/* Do it here to get the right order of messages in case of death. */
					decrementUses((Obj) weapon);
					usesDecremented = true;
				}

				if (victimHp < 0) {
					playSound(victim.getDyingSound());
					appendText(victim.getDyingWords());
					appendText("%s is dead!", getNameWithDefiniteArticle(victim, true));
					Context attackerContext = attacker.getContext();
					attackerContext.setKills(attackerContext.getKills() + 1);
					attackerContext.setExperience(attackerContext.getExperience() + 1 + victim.getPhysicalHp());

					List<Obj> inventory = victim.getState().getInventory();
					if (!victim.isPlayerCharacter() && !inventory.isEmpty()) {
						Scene currentScene = victim.getState().getCurrentScene();
						for (int i = inventory.size() - 1; i >= 0; i--) {
							world.move(inventory.get(i), currentScene);
						}
						appendText(Script.getGroundItemsList(currentScene));
					}
					world.move(victim, world.getStorageScene());
				} else if (attacker.isPlayerCharacter()) {
					double physicalPercent = (double) victim.getState().getCurrentPhysicalHp() / victim.getState().getBasePhysicalHp();
					appendText("%s's condition appears to be %s.",
						getNameWithDefiniteArticle(victim, true),
						Script.getPercentMessage(victim, physicalPercent));
				}
			}
			
			if (causesSpiritualDamage) {
				/* TODO */
			}

			if (freezesOpponent) {
				victim.getContext().setFrozen(true);
			}

		} else if (weapon.getType() != Obj.MAGICAL_OBJECT) {
			appendText("A miss!");
		} else if (attacker.isPlayerCharacter()) {
			appendText("The spell has no effect.");
		}

		if (!usesDecremented && (weapon instanceof Obj)) {
			decrementUses((Obj) weapon);
		}
	}
	
	private void decrementUses(Obj obj) {
		int numberOfUses = obj.getState().getNumberOfUses();
		if (numberOfUses != -1) {
			numberOfUses--;
			if (numberOfUses > 0) {
				obj.getState().setNumberOfUses(numberOfUses);
			} else {
				if (obj.getFailureMessage() != null) {
					appendText(obj.getFailureMessage());
				}
				if (obj.getReturnToRandomScene()) {
					world.move(obj, world.getRandomScene());
				} else {
					world.move(obj, world.getStorageScene());
				}
				obj.setState(new Obj.State(obj, obj.getState().getCurrentOwner(), obj.getState().getCurrentScene()));
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
