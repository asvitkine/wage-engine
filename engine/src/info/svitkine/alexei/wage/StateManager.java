package info.svitkine.alexei.wage;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

// for editing type and creator info on mac platforms
// import com.apple.eio.FileManager;

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
		RandomAccessFile raf = new RandomAccessFile(file.getPath(), "r");

		byte[] data = new byte[(int)raf.length()];	// set byte array to size of file
		raf.readFully(data);						// read data
		
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
		
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
		state.setBasePhysStr(readUnsignedByte(in));
		state.setBasePhysHp(readUnsignedByte(in));
		state.setBasePhysArm(readUnsignedByte(in));
		state.setBasePhysAcc(readUnsignedByte(in));
		state.setBaseSprtStr(readUnsignedByte(in));
		state.setBaseSprtHp(readUnsignedByte(in));
		state.setBaseSprtArm(readUnsignedByte(in));
		state.setBaseSprtAcc(readUnsignedByte(in));
		state.setBaseRunSpeed(readUnsignedByte(in));
		
		// TODO:
		System.out.println("UNKNOWN 10: " + readUnsignedByte(in));	// Usually = 0A
		
		// read user variables
		short[] userVars = parseUserVars(in);
		state.setUserVars(userVars);		
		
		// read update info for every scene
		int sceneSize = state.getNumScenes() * State.SCENE_SIZE;
		byte[] sceneData = new byte[sceneSize];
		
		if(in.read(sceneData) == sceneSize);
			state.setSceneData(sceneData);
		
		// read update info for ever character
		int chrSize = state.getNumChars() * State.CHAR_SIZE;
		byte[] chrData = new byte[chrSize];
			
		if(in.read(chrData) == chrSize);
			state.setCharData(chrData);
		
		// read update info for ever object
		int objSize = state.getNumObjs() * State.OBJ_SIZE;
		byte[] objData = new byte[objSize];
			
		if (in.read(objData) == objSize);
			state.setObjData(objData);	
		
		// EOF reached, let state manager know our state is complete and valid
		state.setIsValid(true);
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
		for (int var = 0; var < 234; var++)
			os.writeShort(state.getUserVars()[var]);
		
		// write updated info for all scenes
		os.write(state.getSceneData());
			
		// write updated info for all characters
		os.write(state.getCharData());
		
		// write updated info for all objects
		os.write(state.getObjData());
		
		// close file for writing
		os.close();
		
		// NOTE: The FileManager class only works on macs
		// Not sure how much good this does as Snow Leopard won't write to HFS volumes anymore,
		// but it changes the icon so i do it anyways...
		//FileManager.setFileCreator(file.getPath(),FileManager.OSTypeToInt("WEDT"));
		//FileManager.setFileType(file.getPath(),FileManager.OSTypeToInt("WDOC"));
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
		
		// Armor
		// TODO: Discuss a fix for this -- as of now spiritual armor is not considered a type of armor
		
		Obj armor = player.getArmor()[Chr.BODY_ARMOR];
		
		if (armor != null) {
			if (armor.getType() == Obj.CHEST_ARMOR)
				state.setChestArmIndex(armor.getHexOffset(state));
			else
				state.setChestArmIndex(0xffff);
			
			if (armor.getType() == Obj.SPIRITUAL_ARMOR)
				state.setSprtArmIndex(armor.getHexOffset(state));
			else
				state.setSprtArmIndex(0xffff);
		} else {
			state.setChestArmIndex(0xffff);
			state.setSprtArmIndex(0xffff);
		}
		
		// update user vars
		updateStateUserVars();
		
		// update scenes
		updateStateSceneData();
		
		// update characters
		updateStateCharData();
		
		// update objects
		updateStateObjData();
		
		// we're done
		state.setIsValid(true);
		
		return true;
	}

	public boolean updateWorld() {
		// make sure we have a valid state object
		if (state.getIsValid()) {
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
					// TODO: there is no slot for magic protection, we should
					// add it
					// Obj[] armor = player.getArmor();
					// armor[Chr.BODY_ARMOR] = sa;
				}

				// set all user variables
				updateWorldUserVars();

				// update all scene stats
				updateScenesWithBinaryData(state.getSceneData());

				// update all char locations and stats
				updateCharsWithBinaryData(state.getCharData());

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
		byte[] data = new byte[state.getNumScenes() * State.SCENE_SIZE];
		int offset = 0;
		
		for (Scene scn : world.getOrderedScenes()) {
			if (scn != world.getStorageScene()) {
				
				byte[] id = shortToBytes(scn.getResourceID());
				
				data[offset] = id[0];
				data[offset+1] = id[1];
				
				byte[] worldY = shortToBytes((short)scn.getWorldY());
				
				data[offset+2] = worldY[0];
				data[offset+3] = worldY[1];
				
				byte[] worldX = shortToBytes((short)scn.getWorldX());
				
				data[offset+4] = worldX[0];
				data[offset+5] = worldX[1];
				
				data[offset+6] = (byte) ((scn.isDirBlocked(Scene.NORTH)) ? 0x01 : 0x00);
				data[offset+7] = (byte) ((scn.isDirBlocked(Scene.SOUTH)) ? 0x01 : 0x00);
				data[offset+8] = (byte) ((scn.isDirBlocked(Scene.EAST)) ? 0x01 : 0x00);
				data[offset+9] = (byte) ((scn.isDirBlocked(Scene.WEST)) ? 0x01 : 0x00);
				
				byte[] soundFreq = shortToBytes((short)scn.getSoundFrequency());

				data[offset+10] = soundFreq[0];
				data[offset+11] = soundFreq[1];
				
				data[offset+12] = (byte) scn.getSoundType();
				
				// rest of the scene data (bytes 12-16) are unknown (i've never seen them anything but 0)
				data[offset+13] = 0x00;
				data[offset+14] = 0x00;
				data[offset+15] = 0x00;	// this byte is set to 0x01 if the player has been to this scene -- do we want to keep track of this?
				
				offset += State.SCENE_SIZE;
			}
		}

		state.setSceneData(data);
	}
	
	private void updateStateCharData() {
		byte[] data = new byte[state.getNumChars() * State.CHAR_SIZE];
		int offset = 0;

		for (Chr chr : world.getOrderedChrs()){
			byte[] id = shortToBytes(chr.getResourceID());
			
			data[offset] = id[0];
			data[offset+1] = id[1];
			
			Scene scn = chr.getCurrentScene();
			
			if (scn == world.getStorageScene()){
				data[offset+2] = 0;
				data[offset+3] = 0;
			} else {
				byte[] loc = shortToBytes(scn.getResourceID());
			
				data[offset+2] = loc[0];
				data[offset+3] = loc[1];
			}
			
			// TODO: Moving characters is a little poorly designed -- it was coded with only an
			// initial state in mind, so that moving a character triggers initializing both the 
			// base and current stat values for the character to one value.  this should be fixed,
			// as what i have below only works for the player character because the base stats for
			// other characters are not stored in the save file.
			
			data[offset+4] = (byte)chr.getContext().getStatVariable(Context.PHYS_STR_CUR);
			data[offset+5] = (byte)chr.getContext().getStatVariable(Context.PHYS_HIT_CUR);
			data[offset+6] = (byte)chr.getContext().getStatVariable(Context.PHYS_ARM_CUR);
			data[offset+7] = (byte)chr.getContext().getStatVariable(Context.PHYS_ACC_CUR);
			data[offset+8] = (byte)chr.getContext().getStatVariable(Context.SPIR_STR_CUR);
			data[offset+9] = (byte)chr.getContext().getStatVariable(Context.SPIR_HIT_CUR);
			data[offset+10] = (byte)chr.getContext().getStatVariable(Context.SPIR_ARM_CUR);
			data[offset+11] = (byte)chr.getContext().getStatVariable(Context.SPIR_ACC_CUR);
			data[offset+12] = (byte)chr.getContext().getStatVariable(Context.PHYS_SPE_CUR);
				
			data[offset+13] = (byte)chr.getRejectsOffers();
			data[offset+14] = (byte)chr.getFollowsOpponent();
			
			// bytes 16-20 are unknown
			data[offset+15] = 0x00;
			data[offset+16] = 0x00;
			data[offset+17] = 0x00;
			data[offset+18] = 0x00;
			data[offset+19] = 0x00;
			
			data[offset+20] = (byte)chr.getWeaponDamage1();
			data[offset+21] = (byte)chr.getWeaponDamage2();
			
			offset += State.CHAR_SIZE;
		}
		
		state.setCharData(data);
	}
	
	private void updateStateObjData() {
		byte[] data = new byte[state.getNumObjs() * State.OBJ_SIZE];
		int offset = 0;

		for (Obj obj : world.getOrderedObjs()) {
				byte[] id = shortToBytes(obj.getResourceID());
				
				data[offset] = id[0];
				data[offset+1] = id[1];
				
				Scene location = obj.getCurrentScene();
				Chr owner = obj.getCurrentOwner();
								
				if(location != null){
					byte[] loc = shortToBytes(location.getResourceID());
					data[offset+2] = loc[0];
					data[offset+3] = loc[1];
					data[offset+4] = 0x00;
					data[offset+5] = 0x00;
				}
				if(owner != null){
					byte[] own = shortToBytes(owner.getResourceID());
					data[offset+2] = 0x00;
					data[offset+3] = 0x00;
					data[offset+4] = own[0];
					data[offset+5] = own[1];
				}

				// update object stats
				data[offset+9] = (byte) obj.getAccuracy();
				data[offset+10] = (byte) obj.getValue();
				data[offset+11] = (byte) obj.getType();
				data[offset+12] = (byte) obj.getDamage();
				data[offset+13] = (byte) obj.getAttackType();
				
				byte[] uses = shortToBytes((short)obj.getNumberOfUses());
				data[offset+14] = uses[0];
				data[offset+15] = uses[1];
				
				offset += State.OBJ_SIZE;
		}
		
		state.setObjData(data);
	}
	
	private void updateWorldUserVars(){
		short[] vars = world.getPlayerContext().getUserVariables();
		for (int i = 0; i < vars.length; i++) {
			vars[i] = state.getUserVars()[i];
		}
	}
	
	public void updateScenesWithBinaryData(byte[] data) {
		int offset = 0;

		for (Scene scn : world.getOrderedScenes()) {
			if (scn != world.getStorageScene()) {
				
				short id = bytesToShort(data[offset], data[offset+1]);

				if (scn.getResourceID() != id)
					return;
				
				scn.setWorldY(bytesToShort(data[offset+2],data[offset+3]));
				scn.setWorldX(bytesToShort(data[offset+4],data[offset+5]));
				scn.setDirBlocked(Scene.NORTH, (unsigned(data[offset+6])==0x01));
				scn.setDirBlocked(Scene.SOUTH, (unsigned(data[offset+7])==0x01));
				scn.setDirBlocked(Scene.EAST, (unsigned(data[offset+8])==0x01));
				scn.setDirBlocked(Scene.WEST, (unsigned(data[offset+9])==0x01));
				
				scn.setSoundFrequency(bytesToShort(data[offset+10],data[offset+11]));
				scn.setSoundType(unsigned(data[offset+12]));
				
				// rest of the scene data (bytes 14-15) are unknown
				
				// byte 16 is set to 0x01 if the player has been to this scene -- do we want to keep track of this?
				
				offset += State.SCENE_SIZE;
			}
		}
	}

	public void updateCharsWithBinaryData(byte[] data) {
		int offset = 0;
		
		for (Chr chr : world.getOrderedChrs()) {
			
			short id = bytesToShort(data[offset],data[offset+1]);
			
			if (chr.getResourceID() != id)
				return;
			
			short sceneLoc = bytesToShort(data[offset+2],data[offset+3]);
			
			if (sceneLoc != 0x0000)
				world.move(chr, world.getSceneByID(sceneLoc));
			else
				world.move(chr, world.getStorageScene());
			
			// TODO: When does a context get created?  For non-player characters?  It looks like it's
			// only when a character is moved from storage

			chr.getContext().setStatVariable(Context.PHYS_STR_CUR, unsigned(data[offset+4]));
			chr.getContext().setStatVariable(Context.PHYS_HIT_CUR, unsigned(data[offset+5]));
			chr.getContext().setStatVariable(Context.PHYS_ARM_CUR, unsigned(data[offset+6]));
			chr.getContext().setStatVariable(Context.PHYS_ACC_CUR, unsigned(data[offset+7]));
			chr.getContext().setStatVariable(Context.SPIR_STR_CUR, unsigned(data[offset+8]));
			chr.getContext().setStatVariable(Context.SPIR_HIT_CUR, unsigned(data[offset+9]));
			chr.getContext().setStatVariable(Context.SPIR_ARM_CUR, unsigned(data[offset+10]));
			chr.getContext().setStatVariable(Context.SPIR_ACC_CUR, unsigned(data[offset+11]));
			chr.getContext().setStatVariable(Context.PHYS_SPE_CUR, unsigned(data[offset+12]));
			chr.setRejectsOffers(unsigned(data[offset+13]));
			chr.setFollowsOpponent(unsigned(data[offset+14]));
			
			// bytes 16-20 are unknown
			
			chr.setWeaponDamage1(unsigned(data[offset+20]));
			chr.setWeaponDamage2(unsigned(data[offset+21]));
			
			offset += State.CHAR_SIZE;
		}
	}
		
	public void updateObjsWithBinaryData(byte[] data) {
		int offset = 0;
		
		for (Obj obj : world.getOrderedObjs()) {
			short id = bytesToShort(data[offset], data[offset+1]);

			if(obj.getResourceID() != id)
				return;
			
			short sceneLoc = bytesToShort(data[offset+2], data[offset+3]);
			short charLoc = bytesToShort(data[offset+4], data[offset+5]);
			
			if(charLoc > 0x0000)
				world.move(obj, world.getCharByID(charLoc));
			else{
				if(sceneLoc == 0x0000)
					world.move(obj, world.getStorageScene());
				else
					world.move(obj, world.getSceneByID(sceneLoc));
			}
				
			// bytes 7-9 are unknown (always = 0)
			
			// update object stats
			obj.setAccuracy(unsigned(data[offset+9]));
			obj.setValue(unsigned(data[offset+10]));
			obj.setType(unsigned(data[offset+11]));
			obj.setDamage(unsigned(data[offset+12]));
			obj.setAttackType(unsigned(data[offset+13]));
			obj.setNumberOfUses(bytesToShort(data[offset+14],data[offset+15]));
			
			offset += State.OBJ_SIZE;
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
	
	private int readUnsignedByte(DataInputStream in) throws IOException {
		int value = in.readByte();
		return (value < 0 ? 256 + value : value);
	}
	
	private static short bytesToShort(byte low, byte high) {
		return (short)((0xff & high) | (0xff & low) << 8 );
	}
	
	private static byte[] shortToBytes(short s) {
		return new byte[] { (byte) ((s & 0xFF00) >> 8), (byte) (s & 0x00FF) };
	}

	public static int unsigned(byte b) {
		return b & 0xFF;
	}
}
