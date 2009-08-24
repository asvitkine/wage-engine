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

	private short visits; // Number of scenes visited, including repeated visits
	private short kills;  // Number of characters killed
	private short experience;
	private short[] userVariables;
	private short[] statVariables;

	public Context() {
		userVariables = new short[26 * 9];
		for (int i = 0; i < userVariables.length; i++)
			userVariables[i] = -1;
		statVariables = new short[18];
	}

	public short getUserVariable(int index) {
		return userVariables[index];
	}

	public void setUserVariable(int index, short value) {
		userVariables[index] = value;
	}

	public short getVisits() {
		return visits;
	}

	public void setVisits(int visits) {
		this.visits = (short) visits;
	}

	public short getKills() {
		return kills;
	}

	public void setKills(int kills) {
		this.kills = (short) kills;
	}
	
	public short getExperience() {
		return experience;
	}

	public void setExperience(int experience) {
		this.experience = (short) experience;
	}
	
	
	public short getStatVariable(int index) {
		return statVariables[index];
	}

	public void setStatVariable(int index, short value) {
		statVariables[index] = value;
	}

	public void setStatVariable(int index, int value) {
		statVariables[index] = (short) value;
	}
}
