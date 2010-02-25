package info.svitkine.alexei.wage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class StateManager {

	private World world;
	private State state;
				
	public StateManager(World world){
		this.world = world;
		this.state = world.getCurrentState();				
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public void readSaveData(File file) throws IOException {
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		
		// Counters
		state.setNumScenes(in.readShort());
		state.setNumChars(in.readShort());
		state.setNumObjs(in.readShort());
		 
		// Hex Offsets
		in.skipBytes(2);
		state.setCharsHexOffset(in.readShort());		 
		in.skipBytes(2);
		state.setObjsHexOffset(in.readShort());

		// Unique 8-byte World Signature
		state.setWorldSig(in.readInt());
		
		// More Counters
		in.skipBytes(2);
		state.setVisitNum(in.readShort());
		in.skipBytes(2);
		state.setLoopNum(in.readShort());
		in.skipBytes(2);
		state.setKillNum(in.readShort());
		
		// Hex offset to player character
		in.skipBytes(2);
		state.setPlayerHexOffset(in.readShort());

		// character in this scene?
		if ((in.readShort() & 0xffff) != 0xffff)
			state.setPresCharHexOffset(in.readShort());
		else
			in.skipBytes(2);

		// Hex offset to current scene
		in.skipBytes(2);
		state.setCurSceneHexOffset(in.readShort());

		// wearing a helmet?
		if ((in.readShort() & 0xffff) != 0xffff)
			state.setHelmetIndex(in.readShort());
		else
			in.skipBytes(2);

		// holding a shield?
		if ((in.readShort() & 0xffff) != 0xffff)
			state.setShieldIndex(in.readShort());
		else
			in.skipBytes(2);

		// wearing chest armor?
		if ((in.readShort() & 0xffff) != 0xffff)
			state.setChestArmIndex(in.readShort());
		else
			in.skipBytes(2);

		// wearing spiritual armor?
		if ((in.readShort() & 0xffff) != 0xffff)
			state.setSprtArmIndex(in.readShort());
		else
			in.skipBytes(2);
		
		// TODO: 
		System.out.println("UNKNOWN 1:" + in.readShort());	// Usually = FFFF
		System.out.println("UNKNOWN 2:" + in.readShort());	// Usually = FFFF
		System.out.println("UNKNOWN 3:" + in.readShort());	// Usually = FFFF
		System.out.println("UNKNOWN 4:" + in.readShort());	// Usually = FFFF
		
		// is a character running away?
		if ((in.readShort() & 0xffff) != 0xffff)
			state.setRunCharHexOffset(in.readShort());
		else
			in.skipBytes(2);

		// players experience
		in.skipBytes(2);
		state.setExp(in.readShort());
		
		// TODO: 
		System.out.println("UNKNOWN 5:" + in.readShort());	// Usually = 0002
		System.out.println("UNKNOWN 6:" + in.readShort());	// Usually = 0002
		System.out.println("UNKNOWN 7:" + in.readShort());	// Usually = 0000
		System.out.println("UNKNOWN 8:" + in.readShort());	// Usually = 0000, but was 0002 when i froze an enemy
		System.out.println("UNKNOWN 9:" + in.readShort());	// Usually = 0000
		
		// Base character stats
		state.setBasePhysStr(in.readUnsignedByte());
		state.setBasePhysHp(in.readUnsignedByte());
		state.setBasePhysArm(in.readUnsignedByte());
		state.setBasePhysAcc(in.readUnsignedByte());
		state.setBaseSprtStr(in.readUnsignedByte());
		state.setBaseSprtHp(in.readUnsignedByte());
		state.setBaseSprtArm(in.readUnsignedByte());
		state.setBaseSprtAcc(in.readUnsignedByte());
		state.setBaseRunSpeed(in.readUnsignedByte());
		
		// TODO:
		System.out.println("UNKNOWN 10: " + in.readUnsignedByte());	// Usually = 0A
		
		// read user variables
		short[] userVars = parseUserVars(in);
		state.setUserVars(userVars);		
		
		// read update info for every scene
		int sceneSize = state.getNumScenes() * State.SCENE_SIZE;
		byte[] sceneData = new byte[sceneSize];

		if (in.read(sceneData) == sceneSize);
			state.setSceneData(sceneData);
		
		// read update info for ever character
		int chrSize = state.getNumChars() * State.CHAR_SIZE;
		byte[] chrData = new byte[chrSize];
			
		if (in.read(chrData) == chrSize);
			state.setChrData(chrData);

		// read update info for ever object
		int objSize = state.getNumObjs() * State.OBJ_SIZE;
		byte[] objData = new byte[objSize];
			
		if (in.read(objData) == objSize);
			state.setObjData(objData);	
		
		// EOF reached, let state manager know our state is complete and valid
		state.setValid(true);
		in.close();
	}

	public void writeSaveData(File file) throws IOException {
		
		DataOutputStream os = new DataOutputStream(new FileOutputStream(file));
		
		// Counters
		os.writeShort(state.getNumScenes());
		os.writeShort(state.getNumChars());
		os.writeShort(state.getNumObjs());
		 
		// Hex Offsets
		os.writeShort(0);
		os.writeShort(state.getCharsHexOffset());		 
		os.writeShort(0);
		os.writeShort(state.getObjsHexOffset());

		// Unique 8-byte World Signature
		os.writeInt(state.getWorldSig());
		
		// More Counters
		os.writeShort(0);
		os.writeShort(state.getVisitNum());
		os.writeShort(0);
		os.writeShort(state.getLoopNum());
		os.writeShort(0);
		os.writeShort(state.getKillNum());
		
		// Hex offset to player character
		os.writeShort(0);
		os.writeShort(state.getPlayerHexOffset());
				
		// character in this scene?
		if (state.getPresCharHexOffset() != 0xffff) {
			os.writeShort(0);
			os.writeShort(state.getPresCharHexOffset());
		} else {
			os.writeShort(0xffff);
			os.writeShort(0xffff);
		}
		
		// Hex offset to current scene
		os.writeShort(0);
		os.writeShort(state.getCurSceneHexOffset());
		
		// wearing a helmet?
		if (state.getHelmetIndex() != 0xffff) {
			os.writeShort(0);
			os.writeShort(state.getHelmetIndex());
		} else {
			os.writeShort(0xffff);
			os.writeShort(0xffff);
		}
		
		// holding a shield?
		if (state.getShieldIndex() != 0xffff) {
			os.writeShort(0x0000);
			os.writeShort(state.getShieldIndex());
		} else {
			os.writeShort(0xffff);
			os.writeShort(0xffff);
		}
		
		// wearing chest armor?
		if (state.getChestArmIndex() != 0xffff) {
			os.writeShort(0);
			os.writeShort(state.getChestArmIndex());
		} else {
			os.writeShort(0xffff);
			os.writeShort(0xffff);
		}
	
		// wearing spiritual armor?
		if (state.getSprtArmIndex() != 0xffff) {
			os.writeShort(0);
			os.writeShort(state.getSprtArmIndex());
		} else {
			os.writeShort(0xffff);
			os.writeShort(0xffff);
		}
		
		// TODO: 
		os.writeShort(0xffff);	// ???? - always FFFF
		os.writeShort(0xffff);	// ???? - always FFFF
		os.writeShort(0xffff);	// ???? - always FFFF
		os.writeShort(0xffff);	// ???? - always FFFF
		
		// did a character just escape?
		if (state.getRunCharHexOffset() != 0xffff) {
			os.writeShort(0);
			os.writeShort(state.getRunCharHexOffset());
		} else {
			os.writeShort(0xffff);
			os.writeShort(0xffff);
		}

		// players experience points
		os.writeShort(0);
		os.writeShort(state.getExp());
		
		// TODO:
		os.writeShort(0x0002);	// anything but 2 seems to report "enhanced physical/spiritual conditions to "status"
		os.writeShort(0x0002);	// this is usually 2, but i've seen 1 and 3 as well
		os.writeShort(0x0000);	// always 0
		os.writeShort(0x0000);	// always 0
		os.writeShort(0x0000);	// always 0

		// Base character stats
		os.writeByte(state.getBasePhysStr());
		os.writeByte(state.getBasePhysHp());
		os.writeByte(state.getBasePhysArm());
		os.writeByte(state.getBasePhysAcc());
		os.writeByte(state.getBaseSprtStr());
		os.writeByte(state.getBaseSprtHp());
		os.writeByte(state.getBaseSprtArm());
		os.writeByte(state.getBaseSprtAcc());
		os.writeByte(state.getBaseRunSpeed());
		
		// TODO:
		os.writeByte(0x0A);		// ???? - always seems to be 0x0A
		
		// write user vars		
		for (short var : state.getUserVars())
			os.writeShort(var);

		// write updated info for all scenes
		os.write(state.getSceneData());
			
		// write updated info for all characters
		os.write(state.getChrData());
		
		// write updated info for all objects
		os.write(state.getObjData());
		
		// close file for writing
		os.close();
	}
	
	public boolean updateState(Chr monster, Chr running, int loopNum) {
		// The base initial state that was created when the world was loaded
		// initialized a lot of the state variables (number of scenes/chrs/objs, hex offsets, etc.)
		// so we only have to initialize the ones that could possibly change
		
		Chr player = world.getPlayer();
		Scene curScene = player.getCurrentScene();
		Context playerContext = world.getPlayerContext();
		
		// update player stats
		state.setBasePhysStr(playerContext.getStatVariable(Context.PHYS_STR_BAS));
		state.setBasePhysHp(playerContext.getStatVariable(Context.PHYS_HIT_BAS));
		state.setBasePhysArm(playerContext.getStatVariable(Context.PHYS_ARM_BAS));
		state.setBasePhysAcc(playerContext.getStatVariable(Context.PHYS_ACC_BAS));
		state.setBaseSprtStr(playerContext.getStatVariable(Context.SPIR_STR_BAS));
		state.setBaseSprtHp(playerContext.getStatVariable(Context.SPIR_HIT_BAS));
		state.setBaseSprtArm(playerContext.getStatVariable(Context.SPIR_ARM_BAS));
		state.setBaseSprtAcc(playerContext.getStatVariable(Context.SPIR_ACC_BAS));
		state.setBaseRunSpeed(playerContext.getStatVariable(Context.PHYS_SPE_BAS));
		
		// set current scene
		state.setCurSceneHexOffset(curScene.getHexOffset(state));
		System.out.println("Current Scene Offset == " + Integer.toHexString(state.getCurSceneHexOffset()));
		
		// set visit#
		state.setVisitNum(playerContext.getVisits());
		
		// set loop#
		state.setLoopNum((short)loopNum);
		
		// set monsters killed
		state.setKillNum(playerContext.getKills());
		
		// set experience
		state.setExp(playerContext.getExperience());
		
		// Current Monster
		if (monster != null)
			state.setPresCharHexOffset(monster.getHexOffset(state));
		else
			state.setPresCharHexOffset(0xffff);
		
		// Running Monster
		if (running != null)
			state.setRunCharHexOffset(running.getHexOffset(state));
		else
			state.setRunCharHexOffset(0xffff);
		
		// Helmet		
		Obj helmet = player.getArmor()[Chr.HEAD_ARMOR];
				
		if (helmet != null)
			state.setHelmetIndex(helmet.getHexOffset(state));
		else
			state.setHelmetIndex(0xffff);
		
		// Shield
		Obj shield = player.getArmor()[Chr.SHIELD_ARMOR];
		
		if (shield != null)
			state.setShieldIndex(shield.getHexOffset(state));
		else
			state.setShieldIndex(0xffff);

		// Chest Armor
		Obj armor = player.getArmor()[Chr.BODY_ARMOR];
		
		if (armor != null)
			state.setChestArmIndex(armor.getHexOffset(state));
		else
			state.setChestArmIndex(0xffff);

		// Spiritual Armor
		Obj magicArmor = player.getArmor()[Chr.MAGIC_ARMOR];
		
		if (magicArmor != null)
			state.setSprtArmIndex(magicArmor.getHexOffset(state));
		else
			state.setSprtArmIndex(0xffff);
		
		// update user vars
		updateStateUserVars();
		
		// update scenes
		updateStateSceneData();
		
		// update characters
		updateStateCharData();
		
		// update objects
		updateStateObjData();
		
		// we're done
		state.setValid(true);
		
		return true;
	}

	public boolean updateWorld() {
		// make sure we have a valid state object
		if (state.isValid()) {
			// make sure save file is for this game
			if (world.getSignature() == state.getWorldSig()) {

				// set player character
				short playerOffset = (short) state.getPlayerHexOffset();
				Chr player = world.getCharByHexOffset(playerOffset);

				if (player == null) {
					System.err.println("Invalid Character!  Aborting load.");
					return false;
				}

				world.setPlayer(player);
				player.setPlayerCharacter(true);

				// set current scene
				Scene s = world.getSceneByHexOffset((short) state.getCurSceneHexOffset());

				if (s == null) {
					System.err.println("Invalid Scene!  Aborting load.");
					return false;
				}

				player.setCurrentScene(s);

				// clear the players inventory list
				player.getInventory().clear();

				// set player stats
				player.setPhysicalStrength(state.getBasePhysStr());
				player.setPhysicalHp(state.getBasePhysHp());
				player.setNaturalArmor(state.getBasePhysArm());
				player.setPhysicalAccuracy(state.getBasePhysAcc());
				player.setSpiritualStength(state.getBaseSprtStr());
				player.setSpiritialHp(state.getBaseSprtHp());
				player.setResistanceToMagic(state.getBaseSprtArm());
				player.setSpiritualAccuracy(state.getBaseSprtAcc());
				player.setRunningSpeed(state.getBaseRunSpeed());

				world.getPlayerContext().setStatVariable(Context.PHYS_STR_BAS,
						state.getBasePhysStr());
				world.getPlayerContext().setStatVariable(Context.PHYS_HIT_BAS,
						state.getBasePhysHp());
				world.getPlayerContext().setStatVariable(Context.PHYS_ARM_BAS,
						state.getBasePhysArm());
				world.getPlayerContext().setStatVariable(Context.PHYS_ACC_BAS,
						state.getBasePhysAcc());
				world.getPlayerContext().setStatVariable(Context.SPIR_STR_BAS,
						state.getBaseSprtStr());
				world.getPlayerContext().setStatVariable(Context.SPIR_HIT_BAS,
						state.getBaseSprtHp());
				world.getPlayerContext().setStatVariable(Context.SPIR_ARM_BAS,
						state.getBaseSprtArm());
				world.getPlayerContext().setStatVariable(Context.SPIR_ACC_BAS,
						state.getBaseSprtAcc());
				world.getPlayerContext().setStatVariable(Context.PHYS_SPE_BAS,
						state.getBaseRunSpeed());

				// set visit#
				world.getPlayerContext().setVisits(state.getVisitNum());

				// set monsters killed
				world.getPlayerContext().setKills(state.getKillNum());

				// set experience
				world.getPlayerContext().setExperience(state.getExp());

				// if a character is present, move it to this scene
				// TODO: This is done in the engine object, would it be cleaner
				// to move it here?

				// if a character just ran away, let our engine know
				// TODO: The current engine doesn't have a case for this, we
				// should update it

				// move all worn helmets, shields, chest armors and spiritual
				// armors to player
				if (state.getHelmetIndex() != 0xffff) {
					Obj helmet = world.getObjByHexOffset((short) state
							.getHelmetIndex());
					if (helmet == null) {
						System.err.println("Invalid Object!  Aborting load.");
						return false;
					}

					world.move(helmet, player);
					Obj[] armor = player.getArmor();
					armor[Chr.HEAD_ARMOR] = helmet;
				}

				if (state.getShieldIndex() != 0xffff) {
					Obj shield = world.getObjByHexOffset((short) state.getShieldIndex());
					System.out.println("Shield found!");
					if (shield == null) {
						System.err.println("Invalid Object!  Aborting load.");
						return false;
					}

					world.move(shield, player);
					Obj[] armor = player.getArmor();
					armor[Chr.SHIELD_ARMOR] = shield;
				}

				if (state.getChestArmIndex() != 0xffff) {
					Obj ca = world.getObjByHexOffset((short) state.getChestArmIndex());
					System.out.println("Physical Armor found!");
					if (ca == null) {
						System.err.println("Invalid Object!  Aborting load.");
						return false;
					}

					world.move(ca, player);
					Obj[] armor = player.getArmor();
					armor[Chr.BODY_ARMOR] = ca;
				}

				if (state.getSprtArmIndex() != 0xffff) {
					Obj sa = world.getObjByHexOffset((short) state.getSprtArmIndex());
					System.out.println("Spiritual Armor found!");
					if (sa == null) {
						System.err.println("Invalid Object!  Aborting load.");
						return false;
					}

					world.move(sa, player);
					Obj[] armor = player.getArmor();
					armor[Chr.MAGIC_ARMOR] = sa;
				}

				// set all user variables
				updateWorldUserVars();

				// update all scene stats
				updateScenesWithBinaryData(state.getSceneData());

				// update all char locations and stats
				updateChrsWithBinaryData(state.getChrData());

				// update all object locations and stats
				updateObjsWithBinaryData(state.getObjData());

				// we're done -- restart our game engine
				return true;

			} else {
				System.err.println("This saved game is for a different world, please select another one.");
				return false;
			}
		} else {
			System.err.println("Invalid save file!");
		}

		System.err.println("Invalid state object!");
		return false;
	}
	
	private void updateStateUserVars() {
		short[] vars = state.getUserVars();
		for (int i = 0; i < vars.length; i++){
			vars[i] = world.getPlayerContext().getUserVariable(i);
		}
	}

	private void updateStateSceneData() {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bout);

		try {
			for (Scene scene : world.getOrderedScenes()) {
				if (scene != world.getStorageScene()) {
					stream.writeShort(scene.getResourceID());
					stream.writeShort(scene.getWorldY());
					stream.writeShort(scene.getWorldX());
					stream.writeByte(scene.isDirBlocked(Scene.NORTH) ? 0x01 : 0x00);
					stream.writeByte(scene.isDirBlocked(Scene.SOUTH) ? 0x01 : 0x00);
					stream.writeByte(scene.isDirBlocked(Scene.EAST) ? 0x01 : 0x00);
					stream.writeByte(scene.isDirBlocked(Scene.WEST) ? 0x01 : 0x00);
					stream.writeShort(scene.getSoundFrequency());
					stream.writeByte(scene.getSoundType());
					// the following two bytes are currently unknown
					stream.writeByte(0);
					stream.writeByte(0);
					stream.writeByte(scene.wasVisited() ? 0x01 : 0x00);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		state.setSceneData(bout.toByteArray());
	}
	
	private void updateStateCharData() {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bout);

		try {
			for (Chr chr : world.getOrderedChrs()) {
				stream.writeShort(chr.getResourceID());

				Scene scene = chr.getCurrentScene();

				if (scene == world.getStorageScene()) {
					stream.writeShort(0);
				} else {
					stream.writeShort(scene.getResourceID());
				}

				// TODO: Moving characters is a little poorly designed -- it was coded with only an
				// initial state in mind, so that moving a character triggers initializing both the 
				// base and current stat values for the character to one value.  this should be fixed,
				// as what i have below only works for the player character because the base stats for
				// other characters are not stored in the save file.

				stream.writeByte(chr.getContext().getStatVariable(Context.PHYS_STR_CUR));
				stream.writeByte(chr.getContext().getStatVariable(Context.PHYS_HIT_CUR));
				stream.writeByte(chr.getContext().getStatVariable(Context.PHYS_ARM_CUR));
				stream.writeByte(chr.getContext().getStatVariable(Context.PHYS_ACC_CUR));
				stream.writeByte(chr.getContext().getStatVariable(Context.SPIR_STR_CUR));
				stream.writeByte(chr.getContext().getStatVariable(Context.SPIR_HIT_CUR));
				stream.writeByte(chr.getContext().getStatVariable(Context.SPIR_ARM_CUR));
				stream.writeByte(chr.getContext().getStatVariable(Context.SPIR_ACC_CUR));
				stream.writeByte(chr.getContext().getStatVariable(Context.PHYS_SPE_CUR));

				stream.writeByte(chr.getRejectsOffers());
				stream.writeByte(chr.getFollowsOpponent());

				// bytes 16-20 are unknown
				stream.writeByte(0);
				stream.writeByte(0);
				stream.writeByte(0);
				stream.writeByte(0);
				stream.writeByte(0);

				stream.writeByte(chr.getWeaponDamage1());
				stream.writeByte(chr.getWeaponDamage2());
			}

			stream.flush();
		
		} catch (IOException e) {
			e.printStackTrace();
		}

		state.setChrData(bout.toByteArray());
	}
	
	private void updateStateObjData() {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bout);

		try {
			for (Obj obj : world.getOrderedObjs()) {
				stream.writeShort(obj.getResourceID());

				Scene location = obj.getCurrentScene();
				Chr owner = obj.getCurrentOwner();

				stream.writeShort(location == null ? 0 : location.getResourceID());
				stream.writeShort(owner == null ? 0 : owner.getResourceID());
				stream.writeByte(obj.getAccuracy());
				stream.writeByte(obj.getValue());
				stream.writeByte(obj.getType());
				stream.writeByte(obj.getDamage());
				stream.writeByte(obj.getAttackType());
				stream.writeShort(obj.getNumberOfUses());
			}

			stream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		state.setObjData(bout.toByteArray());
	}
	
	private void updateWorldUserVars(){
		short[] vars = world.getPlayerContext().getUserVariables();
		for (int i = 0; i < vars.length; i++) {
			vars[i] = state.getUserVars()[i];
		}
	}
	
	public void updateScenesWithBinaryData(byte[] data) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

		try {
			for (Scene scene : world.getOrderedScenes()) {
				if (scene != world.getStorageScene()) {
					short id = in.readShort();

					if (scene.getResourceID() != id) {
						System.err.printf("updateScenesWithBinaryData(): Expected %d but got %d!\n\n", scene.getResourceID(), id);
						return;
					}

					scene.setWorldY(in.readShort());
					scene.setWorldX(in.readShort());
					scene.setDirBlocked(Scene.NORTH, in.readByte() != 0);
					scene.setDirBlocked(Scene.SOUTH, in.readByte() != 0);
					scene.setDirBlocked(Scene.EAST, in.readByte() != 0);
					scene.setDirBlocked(Scene.WEST, in.readByte() != 0);
					scene.setSoundFrequency(in.readUnsignedShort());
					scene.setSoundType(in.readUnsignedByte());
					// below are unknown
					in.readByte();
					in.readByte();
					scene.setVisited(in.readByte() != 0);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateChrsWithBinaryData(byte[] data) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

		try {
			for (Chr chr : world.getOrderedChrs()) {			
				short id = in.readShort();

				if (chr.getResourceID() != id) {
					System.err.printf("updateChrsWithBinaryData(): Expected %d but got %d!\n\n", chr.getResourceID(), id);
					return;
				}

				short sceneLoc = in.readShort();

				if (sceneLoc != 0x0000)
					world.move(chr, world.getSceneByID(sceneLoc));
				else
					world.move(chr, world.getStorageScene());

				// TODO: When does a context get created?  For non-player characters?  It looks like it's
				// only when a character is moved from storage

				chr.getContext().setStatVariable(Context.PHYS_STR_CUR, in.readUnsignedByte());
				chr.getContext().setStatVariable(Context.PHYS_HIT_CUR, in.readUnsignedByte());
				chr.getContext().setStatVariable(Context.PHYS_ARM_CUR, in.readUnsignedByte());
				chr.getContext().setStatVariable(Context.PHYS_ACC_CUR, in.readUnsignedByte());
				chr.getContext().setStatVariable(Context.SPIR_STR_CUR, in.readUnsignedByte());
				chr.getContext().setStatVariable(Context.SPIR_HIT_CUR, in.readUnsignedByte());
				chr.getContext().setStatVariable(Context.SPIR_ARM_CUR, in.readUnsignedByte());
				chr.getContext().setStatVariable(Context.SPIR_ACC_CUR, in.readUnsignedByte());
				chr.getContext().setStatVariable(Context.PHYS_SPE_CUR, in.readUnsignedByte());
				chr.setRejectsOffers(in.readUnsignedByte());
				chr.setFollowsOpponent(in.readUnsignedByte());

				// bytes 16-20 are unknown
				in.readByte();
				in.readByte();
				in.readByte();
				in.readByte();
				in.readByte();

				chr.setWeaponDamage1(in.readUnsignedByte());
				chr.setWeaponDamage2(in.readUnsignedByte());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public void updateObjsWithBinaryData(byte[] data) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

		try {
			for (Obj obj : world.getOrderedObjs()) {
				short id = in.readShort();

				if (obj.getResourceID() != id) {
					System.err.printf("updateObjsWithBinaryData(): Expected %d but got %d!\n\n", obj.getResourceID(), id);
					return;
				}

				short sceneLoc = in.readShort();
				short charLoc = in.readShort();

				if (charLoc > 0x0000) {
					world.move(obj, world.getCharByID(charLoc));
				} else if (sceneLoc == 0x0000) {
					world.move(obj, world.getStorageScene());
				} else {
					world.move(obj, world.getSceneByID(sceneLoc));
				}

				// bytes 7-9 are unknown (always = 0)
				in.readByte();
				in.readByte();

				// update object stats
				obj.setAccuracy(in.readUnsignedByte());
				obj.setValue(in.readUnsignedByte());
				obj.setType(in.readUnsignedByte());
				obj.setDamage(in.readUnsignedByte());
				obj.setAttackType(in.readUnsignedByte());
				obj.setNumberOfUses(in.readShort());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private short[] parseUserVars(DataInputStream in) throws IOException {
		short[] vars = new short[26 * 9];
		
		for (int i = 0; i < vars.length; i++){
			vars[i] = in.readShort();
		}
		
		return vars;
	}
	
	public void printAll(String filePath) throws IOException {
		if (state != null) {
			if (filePath == null)
				state.printState(world, null);
			else
				state.printState(world, filePath);
		}
	}
}
