package info.svitkine.alexei.wage;

public class Context {
	/** The base physical accuracy of the player. */
	public static final int PHYS_ACC_BAS = 0;
	/** The current physical accuracy of the player. */
	public static final int PHYS_ACC_CUR = 1;
	/** The base physical armor of the player. */
	public static final int PHYS_ARM_BAS = 2;
	/** The current physical armor of the player. */
	public static final int PHYS_ARM_CUR = 3;
	/** The base physical hit points of the player. */
	public static final int PHYS_HIT_BAS = 4;
	/** The current physical hit points of the player. */
	public static final int PHYS_HIT_CUR = 5;
	/** The base physical speed of the player. */
	public static final int PHYS_SPE_BAS = 6;
	/** The current physical speed of the player. */
	public static final int PHYS_SPE_CUR = 7;
	/** The base physical strength of the player. */
	public static final int PHYS_STR_BAS = 8;
	/** The current physical strength of the player. */
	public static final int PHYS_STR_CUR = 9;
	/** The base spiritual accuracy of the player. */
	public static final int SPIR_ACC_BAS = 10;
	/** The current spiritual accuracy of the player. */
	public static final int SPIR_ACC_CUR = 11;
	/** The base spiritual armor of the player. */
	public static final int SPIR_ARM_BAS = 12;
	/** The current spiritual armor of the player. */
	public static final int SPIR_ARM_CUR = 13;
	/** The base spiritual hit points of the player. */
	public static final int SPIR_HIT_BAS = 14;
	/** The current spiritual hit points of the player. */
	public static final int SPIR_HIT_CUR = 15;
	/** The base spiritual strength of the player. */
	public static final int SPIR_STR_BAS = 16;
	/** The current spiritual strength of the player. */
	public static final int SPIR_STR_CUR = 17;

	private short loopCount;
	private short[] userVariables;
	private short[] playerVariables;

	public Context() {
		userVariables = new short[26 * 9];
		for (int i = 0; i < userVariables.length; i++)
			userVariables[i] = -1;
		playerVariables = new short[18];
	}

	public short getUserVariable(int index) {
		return userVariables[index];
	}

	public void setUserVariable(int index, short value) {
		userVariables[index] = value;
	}

	public short getLoopCount() {
		return loopCount;
	}
	
	public void resetLoopCount() {
		loopCount = 0;
	}

	public short getPlayerVariable(int index) {
		return playerVariables[index];
	}

	public void setPlayerVariable(int index, short value) {
		playerVariables[index] = value;
	}

	public void setPlayerVariable(int index, int value) {
		playerVariables[index] = (short) value;
	}
}
