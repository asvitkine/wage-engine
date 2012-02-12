package com.googlecode.wage_engine.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

	public void readSaveData(InputStream stream) throws IOException {
		DataInputStream in = new DataInputStream(stream);
		
		// Counters
		state.setNumScenes(in.readShort());
		state.setNumChars(in.readShort());
		state.setNumObjs(in.readShort());
		 
		// Hex Offsets
		state.setChrsHexOffset(in.readInt());		 
		state.setObjsHexOffset(in.readInt());

		// Unique 8-byte World Signature
		state.setWorldSig(in.readInt());
		
		// More Counters
		state.setVisitNum(in.readInt());
		state.setLoopNum(in.readInt());
		state.setKillNum(in.readInt());
		
		// Hex offset to player character
		state.setPlayerHexOffset(in.readInt());

		// character in this scene?
		state.setPresCharHexOffset(in.readInt());

		// Hex offset to current scene
		state.setCurSceneHexOffset(in.readInt());

		// wearing a helmet?
		state.setHelmetIndex(in.readInt());

		// holding a shield?
		state.setShieldIndex(in.readInt());

		// wearing chest armor?
		state.setChestArmIndex(in.readInt());

		// wearing spiritual armor?
		state.setSprtArmIndex(in.readInt());
		
		// TODO: 
		System.out.println("UNKNOWN 1:" + in.readShort());	// Usually = FFFF
		System.out.println("UNKNOWN 2:" + in.readShort());	// Usually = FFFF
		System.out.println("UNKNOWN 3:" + in.readShort());	// Usually = FFFF
		System.out.println("UNKNOWN 4:" + in.readShort());	// Usually = FFFF
		
		// is a character running away?
		state.setRunCharHexOffset(in.readInt());

		// players experience
		state.setExp(in.readInt());

		state.setAim(in.readShort());
		state.setOpponentAim(in.readShort());
		
		// TODO: 
		System.out.println("UNKNOWN 5:" + in.readShort());	// Usually = 0000
		System.out.println("UNKNOWN 6:" + in.readShort());	// Usually = 0000, but was 0002 (0003?) when i froze an enemy
		System.out.println("UNKNOWN 7:" + in.readShort());	// Usually = 0000
		
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
		System.out.println("UNKNOWN 8: " + in.readUnsignedByte());	// Usually = 0A or FF
		
		// read user variables
		short[] userVars = parseUserVars(in);
		state.setUserVars(userVars);		
		
		// read update info for every scene
		int sceneSize = state.getNumScenes() * State.SCENE_SIZE;
		byte[] sceneData = new byte[sceneSize];

		if (in.read(sceneData) == sceneSize);
			state.setSceneData(sceneData);
		
		// read update info for ever character
		int chrSize = state.getNumChars() * State.CHR_SIZE;
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

	public void writeSaveData(OutputStream stream) throws IOException {		
		DataOutputStream out = new DataOutputStream(stream);

		// Counters
		out.writeShort(state.getNumScenes());
		out.writeShort(state.getNumChars());
		out.writeShort(state.getNumObjs());
		 
		// Hex Offsets
		out.writeInt(state.getChrsHexOffset());		 
		out.writeInt(state.getObjsHexOffset());

		// Unique 8-byte World Signature
		out.writeInt(state.getWorldSig());

		// More Counters
		out.writeInt(state.getVisitNum());
		out.writeInt(state.getLoopNum());
		out.writeInt(state.getKillNum());

		// Hex offset to player character
		out.writeInt(state.getPlayerHexOffset());

		// character in this scene?
		out.writeInt(state.getPresCharHexOffset());

		// Hex offset to current scene
		out.writeInt(state.getCurSceneHexOffset());

		// wearing a helmet?
		out.writeInt(state.getHelmetIndex());

		// holding a shield?
		out.writeInt(state.getShieldIndex());

		// wearing chest armor?
		out.writeInt(state.getChestArmIndex());

		// wearing spiritual armor?
		out.writeInt(state.getSprtArmIndex());

		// TODO: 
		out.writeShort(0xffff);	// ???? - always FFFF
		out.writeShort(0xffff);	// ???? - always FFFF
		out.writeShort(0xffff);	// ???? - always FFFF
		out.writeShort(0xffff);	// ???? - always FFFF
		
		// did a character just escape?
		out.writeInt(state.getRunCharHexOffset());

		// players experience points
		out.writeInt(state.getExp());

		out.writeShort(state.getAim());
		out.writeShort(state.getOpponentAim());

		// TODO:
		out.writeShort(0x0000);	// always 0
		out.writeShort(0x0000);	// always 0
		out.writeShort(0x0000);	// always 0

		// Base character stats
		out.writeByte(state.getBasePhysStr());
		out.writeByte(state.getBasePhysHp());
		out.writeByte(state.getBasePhysArm());
		out.writeByte(state.getBasePhysAcc());
		out.writeByte(state.getBaseSprtStr());
		out.writeByte(state.getBaseSprtHp());
		out.writeByte(state.getBaseSprtArm());
		out.writeByte(state.getBaseSprtAcc());
		out.writeByte(state.getBaseRunSpeed());

		// TODO:
		out.writeByte(0x0A);		// ???? - always seems to be 0x0A

		// write user vars		
		for (short var : state.getUserVars())
			out.writeShort(var);

		// write updated info for all scenes
		out.write(state.getSceneData());

		// write updated info for all characters
		out.write(state.getChrData());

		// write updated info for all objects
		out.write(state.getObjData());

		// close file for writing
		out.close();
	}
	
	public boolean updateState(Chr monster, Chr running, int loopNum, int aim, int opponentAim) {
		// The base initial state that was created when the world was loaded
		// initialized a lot of the state variables (number of scenes/chrs/objs, hex offsets, etc.)
		// so we only have to initialize the ones that could possibly change
		
		Chr player = world.getPlayer();
		Scene curScene = player.getState().getCurrentScene();
		Chr.State playerState = player.getState();
		Context playerContext = world.getPlayerContext();
		
		// update player stats
		state.setBasePhysStr(playerState.getBasePhysicalStrength());
		state.setBasePhysHp(playerState.getBasePhysicalHp());
		state.setBasePhysArm(playerState.getBaseNaturalArmor());
		state.setBasePhysAcc(playerState.getBasePhysicalAccuracy());
		state.setBaseSprtStr(playerState.getBaseSpiritualStrength());
		state.setBaseSprtHp(playerState.getBaseSpiritualHp());
		state.setBaseSprtArm(playerState.getBaseResistanceToMagic());
		state.setBaseSprtAcc(playerState.getBaseSpiritualAccuracy());
		state.setBaseRunSpeed(playerState.getBaseRunningSpeed());
		
		// set current scene
		state.setCurSceneHexOffset(state.getHexOffsetForScene(curScene));
		System.out.println("Current Scene Offset == " + Integer.toHexString(state.getCurSceneHexOffset()));
		
		// set visit#
		state.setVisitNum(playerContext.getVisits());
		
		// set loop#
		state.setLoopNum(loopNum);
		
		// set monsters killed
		state.setKillNum(playerContext.getKills());

		// set experience
		state.setExp(playerContext.getExperience());

		state.setAim(aim);
		state.setOpponentAim(opponentAim);
		
		// Current Monster
		state.setPresCharHexOffset(state.getHexOffsetForChr(monster));

		// Running Monster
		state.setRunCharHexOffset(state.getHexOffsetForChr(running));

		// Helmet		
		state.setHelmetIndex(state.getHexOffsetForObj(player.getState().getArmor(Chr.HEAD_ARMOR)));

		// Shield
		state.setShieldIndex(state.getHexOffsetForObj(player.getState().getArmor(Chr.SHIELD_ARMOR)));

		// Chest Armor
		state.setChestArmIndex(state.getHexOffsetForObj(player.getState().getArmor(Chr.BODY_ARMOR)));

		// Spiritual Armor
		state.setSprtArmIndex(state.getHexOffsetForObj(player.getState().getArmor(Chr.MAGIC_ARMOR)));
		
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
				Chr player = world.getCharByHexOffset(state.getPlayerHexOffset());

				if (player == null) {
					System.err.println("Invalid Character!  Aborting load.");
					return false;
				}

				world.setPlayer(player);

				// set current scene
				Scene s = world.getSceneByHexOffset(state.getCurSceneHexOffset());

				if (s == null) {
					System.err.println("Invalid Scene!  Aborting load.");
					return false;
				}

				player.getState().setCurrentScene(s);

				// clear the players inventory list
				player.getState().getInventory().clear();

				// set player stats
				player.getState().setBasePhysicalStrength(state.getBasePhysStr());
				player.getState().setBasePhysicalHp(state.getBasePhysHp());
				player.getState().setBaseNaturalArmor(state.getBasePhysArm());
				player.getState().setBasePhysicalAccuracy(state.getBasePhysAcc());
				player.getState().setBaseSpiritualStrength(state.getBaseSprtStr());
				player.getState().setBaseSpiritualHp(state.getBaseSprtHp());
				player.getState().setBaseResistanceToMagic(state.getBaseSprtArm());
				player.getState().setBaseSpiritualAccuracy(state.getBaseSprtAcc());
				player.getState().setBaseRunningSpeed(state.getBaseRunSpeed());

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

				// set all user variables
				updateWorldUserVars();

				// update all scene stats
				updateScenesWithBinaryData(state.getSceneData());

				// update all char locations and stats
				updateChrsWithBinaryData(state.getChrData());

				// update all object locations and stats
				updateObjsWithBinaryData(state.getObjData());

				// update inventories and scene contents
				for (Obj obj : world.getOrderedObjs()) {
					Chr chr = obj.getState().getCurrentOwner();
					if (chr != null) {
						chr.getState().getInventory().add(obj);
					} else {
						Scene scene = obj.getState().getCurrentScene();
						scene.getState().getObjs().add(obj);
					}
				}

				// update scene chrs
				for (Chr chr : world.getOrderedChrs()) {
					Scene scene = chr.getState().getCurrentScene();
					scene.getState().getChrs().add(chr);
					if (chr != player) {
						Engine.wearObjs(chr);
					}
				}

				// move all worn helmets, shields, chest armors and spiritual
				// armors to player
				for (int type : new int[] { Chr.HEAD_ARMOR, Chr.SHIELD_ARMOR, Chr.BODY_ARMOR, Chr.MAGIC_ARMOR } ) {
					Obj armor;

					if (type == Chr.HEAD_ARMOR)
						armor = world.getObjByHexOffset(state.getHelmetIndex());
					else if (type == Chr.SHIELD_ARMOR)
						armor = world.getObjByHexOffset(state.getShieldIndex());
					else if (type == Chr.BODY_ARMOR)
						armor = world.getObjByHexOffset(state.getChestArmIndex());
					else
						armor = world.getObjByHexOffset(state.getSprtArmIndex());

					if (armor != null) {
						world.move(armor, player);
						player.getState().setArmor(type, armor);
					}
				}

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
	
	private static void writeSceneState(DataOutputStream stream, Scene.State state) throws IOException {
		stream.writeShort(state.getWorldY());
		stream.writeShort(state.getWorldX());
		stream.writeByte(state.isDirBlocked(Scene.NORTH) ? 0x01 : 0x00);
		stream.writeByte(state.isDirBlocked(Scene.SOUTH) ? 0x01 : 0x00);
		stream.writeByte(state.isDirBlocked(Scene.EAST) ? 0x01 : 0x00);
		stream.writeByte(state.isDirBlocked(Scene.WEST) ? 0x01 : 0x00);
		stream.writeShort(state.getSoundFrequency());
		stream.writeByte(state.getSoundType());
		// the following two bytes are currently unknown
		stream.writeByte(0);
		stream.writeByte(0);
		stream.writeByte(state.wasVisited() ? 0x01 : 0x00);
	}

	private void updateStateSceneData() {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bout);

		try {
			for (Scene scene : world.getOrderedScenes()) {
				if (scene != world.getStorageScene()) {
					stream.writeShort(scene.getResourceID());
					writeSceneState(stream, scene.getState());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		state.setSceneData(bout.toByteArray());
	}
	
	private static void writeChrState(DataOutputStream stream, Chr.State state) throws IOException {
		stream.writeShort(state.getCurrentScene().getResourceID());
		stream.writeByte(state.getCurrentPhysicalStrength());
		stream.writeByte(state.getCurrentPhysicalHp());
		stream.writeByte(state.getCurrentNaturalArmor());
		stream.writeByte(state.getCurrentPhysicalAccuracy());
		stream.writeByte(state.getCurrentSpiritualStrength());
		stream.writeByte(state.getCurrentSpiritualHp());
		stream.writeByte(state.getCurrentResistanceToMagic());
		stream.writeByte(state.getCurrentSpiritualAccuracy());
		stream.writeByte(state.getCurrentRunningSpeed());
		stream.writeByte(state.getRejectsOffers());
		stream.writeByte(state.getFollowsOpponent());
		// bytes 16-20 are unknown
		stream.writeByte(0);
		stream.writeByte(0);
		stream.writeByte(0);
		stream.writeByte(0);
		stream.writeByte(0);
		stream.writeByte(state.getWeaponDamage1());
		stream.writeByte(state.getWeaponDamage2());
	}
	
	private void updateStateCharData() {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bout);

		try {
			for (Chr chr : world.getOrderedChrs()) {
				stream.writeShort(chr.getResourceID());
				writeChrState(stream, chr.getState());
			}

			stream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		state.setChrData(bout.toByteArray());
	}
	
	private static void writeObjState(DataOutputStream stream, Obj.State state) throws IOException {
		Scene location = state.getCurrentScene();
		Chr owner = state.getCurrentOwner();

		stream.writeShort(location == null ? 0 : location.getResourceID());
		stream.writeShort(owner == null ? 0 : owner.getResourceID());

		// bytes 7-9 are unknown (always = 0)
		stream.writeByte(0);
		stream.writeByte(0);
		stream.writeByte(0);

		stream.writeByte(state.getAccuracy());
		stream.writeByte(state.getValue());
		stream.writeByte(state.getType());
		stream.writeByte(state.getDamage());
		stream.writeByte(state.getAttackType());
		stream.writeShort(state.getNumberOfUses());
	}
	
	private void updateStateObjData() {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bout);

		try {
			for (Obj obj : world.getOrderedObjs()) {
				stream.writeShort(obj.getResourceID());
				writeObjState(stream, obj.getState());
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
	
	private static Scene.State readSceneState(DataInputStream in, Scene scene) throws IOException {
		Scene.State state = new Scene.State(scene);
		state.setWorldY(in.readShort());
		state.setWorldX(in.readShort());
		state.setDirBlocked(Scene.NORTH, in.readByte() != 0);
		state.setDirBlocked(Scene.SOUTH, in.readByte() != 0);
		state.setDirBlocked(Scene.EAST, in.readByte() != 0);
		state.setDirBlocked(Scene.WEST, in.readByte() != 0);
		state.setSoundFrequency(in.readUnsignedShort());
		state.setSoundType(in.readUnsignedByte());
		// below are unknown
		in.readByte();
		in.readByte();
		state.setVisited(in.readByte() != 0);
		return state;
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

					scene.setState(readSceneState(in, scene));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Chr.State readChrState(DataInputStream in, Chr chr) throws IOException {
		Chr.State state = new Chr.State(chr);
		state.setCurrentScene(world.getSceneByID(in.readShort()));
		state.setCurrentPhysicalStrength(in.readUnsignedByte());
		state.setCurrentPhysicalHp(in.readUnsignedByte());
		state.setCurrentNaturalArmor(in.readUnsignedByte());
		state.setCurrentPhysicalAccuracy(in.readUnsignedByte());
		state.setCurrentSpiritualStrength(in.readUnsignedByte());
		state.setCurrentSpiritualHp(in.readUnsignedByte());
		state.setCurrentResistanceToMagic(in.readUnsignedByte());
		state.setCurrentSpiritualAccuracy(in.readUnsignedByte());
		state.setCurrentRunningSpeed(in.readUnsignedByte());
		state.setRejectsOffers(in.readUnsignedByte());
		state.setFollowsOpponent(in.readUnsignedByte());
		// bytes 16-20 are unknown
		in.readByte();
		in.readByte();
		in.readByte();
		in.readByte();
		in.readByte();
		state.setWeaponDamage1(in.readUnsignedByte());
		state.setWeaponDamage2(in.readUnsignedByte());
		return state;
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

				chr.setState(readChrState(in, chr));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Obj.State readObjState(DataInputStream in, Obj obj) throws IOException {
		Obj.State state = new Obj.State(obj);
		short sceneLoc = in.readShort();
		short charOwner = in.readShort();

		// TODO: Verify that things get properly removed without using world.move()
		if (charOwner != 0) {
			state.setCurrentOwner(world.getCharByID(charOwner));
		} else {
			state.setCurrentScene(world.getSceneByID(sceneLoc));			
		}

		// bytes 7-9 are unknown (always = 0)
		in.readByte();
		in.readByte();
		in.readByte();

		// update object stats
		state.setAccuracy(in.readUnsignedByte());
		state.setValue(in.readUnsignedByte());
		state.setType(in.readUnsignedByte());
		state.setDamage(in.readUnsignedByte());
		state.setAttackType(in.readUnsignedByte());
		state.setNumberOfUses(in.readShort());
		return state;
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

				obj.setState(readObjState(in, obj));
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
