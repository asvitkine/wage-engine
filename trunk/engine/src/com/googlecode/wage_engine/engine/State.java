package com.googlecode.wage_engine.engine;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class State {

	public static final int VARS_INDEX = 0x005E;
	public static final int SCENES_INDEX = 0x0232;
	
	public static final int SCENE_SIZE = 0x0010;
	public static final int CHR_SIZE = 0x0016;
	public static final int OBJ_SIZE = 0x0010;
	
	// important global info
	private short numScenes;
	private short numChars;
	private short numObjs;
	
	// unique world id (int)
	private int worldSignature;
	
	// global status vars
	private int visitNum;
	private int loopNum;
	private int killNum;
	private int exp;
	private int aim;
	private int opponentAim;

	// information about player character
	private int basePhysStr;
	private int basePhysHp;
	private int basePhysArm;
	private int basePhysAcc;
	private int baseSprtStr;
	private int baseSprtHp;
	private int baseSprtArm;
	private int baseSprtAcc;
	private int baseRunSpeed;
	
	// hex offsets within the save file
	private int chrsHexOffset;
	private int objsHexOffset;
	private int playerHexOffset;	
	private int curSceneHexOffset = 0;
	
	// info about non-player characters related to the current scene
	private int presCharHexOffset = -1;		// resource id of character present in current scene
	private int runCharHexOffset = -1;		// hex index to character who just ran away
	
	// are we wearing anything?
	private int helmetHexOffset = -1;
	private int shieldHexOffset = -1;
	private int chestArmHexOffset = -1;
	private int sprtArmHexOffset = -1;
	
	private short[] userVars;
	
	private byte[] sceneData;
	private byte[] chrData;
	private byte[] objData;
	
	private boolean valid;
	
	public State() {
		this.valid = false;
		userVars = new short[26 * 9];
	}

	public short getNumScenes() {
		return numScenes;
	}
	public void setNumScenes(short numScenes) {
		this.numScenes = numScenes;
	}
	public short getNumChars() {
		return numChars;
	}
	public void setNumChars(short numChars) {
		this.numChars = numChars;
	}
	public short getNumObjs() {
		return numObjs;
	}
	public void setNumObjs(short numObjs) {
		this.numObjs = numObjs;
	}
	public int getWorldSig() {
		return worldSignature;
	}
	public void setWorldSig(int worldSig) {
		this.worldSignature = worldSig;
	}
	public int getVisitNum() {
		return visitNum;
	}
	public void setVisitNum(int visitNum) {
		this.visitNum = visitNum;
	}
	public int getLoopNum() {
		return loopNum;
	}
	public void setLoopNum(int loopNum) {
		this.loopNum = loopNum;
	}
	public int getKillNum() {
		return killNum;
	}
	public void setKillNum(int killNum) {
		this.killNum = killNum;
	}
	public int getExp() {
		return exp;
	}
	public void setExp(int exp) {
		this.exp = exp;
	}
	public int getAim() {
		return aim;
	}
	public void setAim(int aim) {
		this.aim = aim;
	}
	public int getOpponentAim() {
		return opponentAim;
	}
	public void setOpponentAim(int opponentAim) {
		this.opponentAim = opponentAim;
	}
	public int getBasePhysStr() {
		return basePhysStr;
	}
	public void setBasePhysStr(int physStr) {
		this.basePhysStr = physStr;
	}
	public int getBasePhysHp() {
		return basePhysHp;
	}
	public void setBasePhysHp(int physHp) {
		this.basePhysHp = physHp;
	}
	public int getBasePhysArm() {
		return basePhysArm;
	}
	public void setBasePhysArm(int physArm) {
		this.basePhysArm = physArm;
	}
	public int getBasePhysAcc() {
		return basePhysAcc;
	}
	public void setBasePhysAcc(int physAcc) {
		this.basePhysAcc = physAcc;
	}
	public int getBaseSprtStr() {
		return baseSprtStr;
	}
	public void setBaseSprtStr(int sprtStr) {
		this.baseSprtStr = sprtStr;
	}
	public int getBaseSprtHp() {
		return baseSprtHp;
	}
	public void setBaseSprtHp(int sprtHit) {
		this.baseSprtHp = sprtHit;
	}
	public int getBaseSprtArm() {
		return baseSprtArm;
	}
	public void setBaseSprtArm(int sprtArm) {
		this.baseSprtArm = sprtArm;
	}
	public int getBaseSprtAcc() {
		return baseSprtAcc;
	}
	public void setBaseSprtAcc(int sprtAcc) {
		this.baseSprtAcc = sprtAcc;
	}
	public int getBaseRunSpeed() {
		return baseRunSpeed;
	}
	public void setBaseRunSpeed(int runSpeed) {
		this.baseRunSpeed = runSpeed;
	}
	public int getChrsHexOffset() {
		return chrsHexOffset;
	}
	public void setChrsHexOffset(int chrsHexOffset) {
		this.chrsHexOffset = chrsHexOffset;
	}
	public int getObjsHexOffset() {
		return objsHexOffset;
	}
	public void setObjsHexOffset(int objsHexOffset) {
		this.objsHexOffset = objsHexOffset;
	}
	public int getPlayerHexOffset() {
		return playerHexOffset;
	}
	public void setPlayerHexOffset(int playerHexOffset) {
		this.playerHexOffset = playerHexOffset;
	}
	public int getCurSceneHexOffset() {
		return curSceneHexOffset;
	}
	public void setCurSceneHexOffset(int curSceneHexOffset) {
		this.curSceneHexOffset = curSceneHexOffset;
	}
	public int getPresCharHexOffset() {
		return presCharHexOffset;
	}
	public void setPresCharHexOffset(int presCharOffset) {
		this.presCharHexOffset = presCharOffset;
	}
	public int getRunCharHexOffset() {
		return runCharHexOffset;
	}
	public void setRunCharHexOffset(int runCharIndex) {
		this.runCharHexOffset = runCharIndex;
	}
	public int getHelmetIndex() {
		return helmetHexOffset;
	}
	public void setHelmetIndex(int helmetIndex) {
		this.helmetHexOffset = helmetIndex;
	}
	public int getShieldIndex() {
		return shieldHexOffset;
	}
	public void setShieldIndex(int shieldIndex) {
		this.shieldHexOffset = shieldIndex;
	}
	public int getChestArmIndex() {
		return chestArmHexOffset;
	}
	public void setChestArmIndex(int chestArmIndex) {
		this.chestArmHexOffset = chestArmIndex;
	}
	public int getSprtArmIndex() {
		return sprtArmHexOffset;
	}
	public void setSprtArmIndex(int sprtArmIndex) {
		this.sprtArmHexOffset = sprtArmIndex;
	}
	public short[] getUserVars() {
		return userVars;
	}
	public void setUserVars(short[] userVars) {
		this.userVars = userVars;
	}
	public byte[] getSceneData() {
		return sceneData;
	}
	public void setSceneData(byte[] sceneData) {
		this.sceneData = sceneData;
	}
	public byte[] getChrData() {
		return chrData;
	}
	public void setChrData(byte[] charData) {
		this.chrData = charData;
	}
	public byte[] getObjData() {
		return objData;
	}
	public void setObjData(byte[] objData) {
		this.objData = objData;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	public int getHexOffsetForObj(Obj obj) {
		if (obj == null)
			return -1;
		return ((obj.getIndex() * State.OBJ_SIZE) + getObjsHexOffset());
	}

	public int getHexOffsetForChr(Chr chr) {
		if (chr == null)
			return -1;
		return ((chr.getIndex() * State.CHR_SIZE) + getChrsHexOffset());
	}

	public int getHexOffsetForScene(Scene scene) {
		if (scene == null)
			return -1;
		return ((scene.getIndex() * State.SCENE_SIZE) + State.SCENES_INDEX);
	}

	// For Debugging Purposes:
	public void printState(World world, String filePath) throws IOException {
		PrintWriter stream;
		
		if (filePath != null)
			stream = new PrintWriter(new FileWriter(filePath));
		else
			stream = new PrintWriter(System.out, true);
		
		stream.println("Number of Scenes: " + this.numScenes);
		stream.println("Number of Characters: " + this.numChars);
		stream.println("Number of Object: " + this.numObjs);
		stream.println("==============================================");
		stream.println("Hex Offset to start of Characters: " + Integer.toHexString(this.chrsHexOffset));
		stream.println("Hex Offset to start of Objects: " + Integer.toHexString(this.objsHexOffset));
		stream.println("==============================================");
		stream.println("World Signature: " + Integer.toHexString(this.worldSignature));
		stream.println("==============================================");
		stream.println("Visit# (Total scenes visited including repeats): " + this.visitNum);
		stream.println("Loop# (Commands executed in the current scene): " + this.loopNum);
		stream.println("Monster# (Total monsters killed): " + this.killNum);
		stream.println("==============================================");
		stream.println("Hex Offset to Player: " + Integer.toHexString(this.playerHexOffset));
		stream.println("Player: " + world.getCharByHexOffset(this.playerHexOffset));
		stream.println("==============================================");
		stream.println("Hex Offset to character in current scene: " + Integer.toHexString(this.presCharHexOffset));
		stream.println("Name of Character in current scene: " + world.getCharByHexOffset((short)this.presCharHexOffset));
		stream.println("==============================================");
		stream.println("Hex Offset to Current Scene: " + Integer.toHexString(this.curSceneHexOffset));
		stream.println("Current Scene: " + world.getSceneByHexOffset(this.curSceneHexOffset));
		stream.println("==============================================");
		stream.println("Hex Offset to Worn Helmet: " + Integer.toHexString(this.helmetHexOffset));
		if (helmetHexOffset != 0xffff)
			stream.println("Helmet: " + world.getObjByHexOffset((short)this.helmetHexOffset));
		stream.println("Hex Offset to Worn Shield: " + Integer.toHexString(this.shieldHexOffset));
		if (shieldHexOffset != 0xffff)
			stream.println("Shield: " + world.getObjByHexOffset((short)this.shieldHexOffset));
		stream.println("Hex Offset to Worn Chest Armor: " + Integer.toHexString(this.chestArmHexOffset));
		if (chestArmHexOffset != 0xffff)
			stream.println("Chest Armor: " + world.getObjByHexOffset((short)this.chestArmHexOffset));
		stream.println("Hex Offset to Worn Spiritual Armor: " + Integer.toHexString(this.sprtArmHexOffset));
		if (sprtArmHexOffset != 0xffff)
			stream.println("Spiritual Armor: " + world.getObjByHexOffset((short)this.sprtArmHexOffset));
		stream.println("==============================================");
		stream.println("Hex Offset to Running Character: " + Integer.toHexString(this.runCharHexOffset));
		if (runCharHexOffset != 0xffff)
			stream.println("Running Character: " + world.getCharByHexOffset((short)this.runCharHexOffset));
		stream.println("==============================================");
		stream.println("Base Physical Strength Value: " + this.basePhysStr );
		stream.println("Base Physical Hit Point Value: " + this.basePhysHp);
		stream.println("Base Physical Armor Value: " + this.basePhysArm);
		stream.println("Base Physical Accuracy Value: " + this.basePhysAcc);
		stream.println("Base Spiritual Strength Value: " + this.baseSprtStr);
		stream.println("Base Spiritual Hit Point Value: " + this.baseSprtHp);
		stream.println("Base Spiritual Armor Value:"+ this.baseSprtArm);
		stream.println("Base Spiritual Accuracy Value: " + this.baseSprtAcc);
		stream.println("Base Run Speed Value: " + this.baseRunSpeed);
		stream.println("==============================================");
		stream.println("Player Experience: " + this.exp);
		stream.println("==============================================");

		// print variables
		int varCount = 0;
		
		for (char letter = 'A'; letter <= 'Z'; letter++) {
			for (int num = 1; num <= 9; num++) {
				stream.print(letter);
				stream.print(num + "#:" + this.userVars[varCount] + " | ");
				varCount++;
			}
			stream.println();
		}
		
		// print scenes
		printScenes(world, stream);
		
		// print characters
		printCharacters(world, stream);
		
		// print objects
		printObjects(world, stream);
		
		stream.close();
	}
	
	private void printScenes(World world, PrintWriter stream) {
		int offset = 0;
		
		for (Scene scn : world.getOrderedScenes()) {
			if (scn != world.getStorageScene()) {
				
				short id = bytesToShort(sceneData[offset], sceneData[offset+1]);

				if (scn.getResourceID() != id)
					return;
				
				stream.println("Scene: " + scn.getName());
				stream.println("ID: " + id );
				stream.println("World Y: " + bytesToShort(sceneData[offset+2],sceneData[offset+3]));
				stream.println("World X: " + bytesToShort(sceneData[offset+4],sceneData[offset+5]));
				stream.println("Blocked North: " + sceneData[offset+6]);
				stream.println("Blocked South: " + sceneData[offset+7]);
				stream.println("Blocked East: " + sceneData[offset+8]);
				stream.println("Blocked West: " + sceneData[offset+9]);
				
				stream.println("Sound Frequency: " + bytesToShort(sceneData[offset+10],sceneData[offset+11]));
				stream.println("Sound Type: " + sceneData[offset+12]);
				
				// rest of the scene data (bytes 14-16) are unknown
				stream.println("UNKNOWN: " + sceneData[offset+13]);
				stream.println("UNKNOWN: " + sceneData[offset+14]);
				stream.println("Visited: " + sceneData[offset+15]);
				
				offset += State.SCENE_SIZE;
			}
		}
	}
	
	private void printCharacters(World world, PrintWriter stream) {
		ByteArrayInputStream bin = new ByteArrayInputStream(chrData);
		DataInputStream in = new DataInputStream(bin);
		try {
			for (Chr chr : world.getOrderedChrs()) {			
				short id = in.readShort();

				if (chr.getResourceID() != id)
					return;

				short sceneLoc = in.readShort();

				stream.println("Character: " + chr.getName());
				stream.println("ID: " + id );
				stream.println("Location: " + sceneLoc);

				stream.println("Current Physical Strength: " + in.readByte());
				stream.println("Current Physical Hit: " + in.readByte());
				stream.println("Current Physical Armor: " + in.readByte());
				stream.println("Current Physical Accuracy: " + in.readByte());
				stream.println("Current Spiritual Strength: " + in.readByte());
				stream.println("Current Spiritual Hit: " + in.readByte());
				stream.println("Current Spiritual Armor: " + in.readByte());
				stream.println("Current Spiritual Accuracy: " + in.readByte());
				stream.println("Current Physical Speed: " + in.readByte());

				stream.println("Rejects Offers: " + in.readByte());
				stream.println("Follows Opponents: " + in.readByte());

				// bytes 16-20 are unknown
				stream.println("UNKNOWN: " + in.readByte());
				stream.println("UNKNOWN: " + in.readByte());
				stream.println("UNKNOWN: " + in.readByte());
				stream.println("UNKNOWN: " + in.readByte());
				stream.println("UNKNOWN: " + in.readByte());

				stream.println("Weapon Damage 1: "+ in.readByte());
				stream.println("Weapon Damage 2: " + in.readByte());
			}
		} catch (IOException e) { }
	}
	
	private void printObjects(World world, PrintWriter stream){
		int offset = 0;
		
		for (Obj obj : world.getOrderedObjs()) {
			short id = bytesToShort(objData[offset],objData[offset+1]);

			if (obj.getResourceID() != id)
				return;
			
			short sceneLoc = bytesToShort(objData[offset+2],objData[offset+3]);
			short charLoc = bytesToShort(objData[offset+4],objData[offset+5]);
			
			stream.println("Object: " + obj.getName());
			stream.println("ID: " + id);
			stream.println("Scene Location: " + sceneLoc);
			stream.println("Char Location: " + charLoc);
			
			stream.println("UNKNOWN: " + objData[offset+6]);
			stream.println("UNKNOWN: " + objData[offset+7]);
			stream.println("UNKNOWN: " + objData[offset+8]);
			
			// bytes 7-9 are unknown (always = 0)
			
			// update object stats
			stream.println("Accuracy: " + objData[offset+9]);
			stream.println("Value: " + objData[offset+10]);
			stream.println("Type: " + objData[offset+11]);
			stream.println("Damage: " + objData[offset+12]);
			stream.println("Attack Type: " + objData[offset+13]);
			stream.println("Number of Uses: " + bytesToShort(objData[offset+14],objData[offset+15]));
			
			offset += State.OBJ_SIZE;
		}
	}
	
	private static short bytesToShort(byte low, byte high) {
		return (short)((0xff & high) | (0xff & low) << 8 );
	}
}
