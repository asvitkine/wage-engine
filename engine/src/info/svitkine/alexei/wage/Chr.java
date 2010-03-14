package info.svitkine.alexei.wage;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;


public interface Chr {
	public static final int RETURN_TO_STORAGE = 0;
	public static final int RETURN_TO_RANDOM_SCENE = 1;
	public static final int RETURN_TO_INITIAL_SCENE = 2;

	public static final int HEAD = 1;
	public static final int CHEST = 2;
	public static final int SIDE = 3;

	public static final int HEAD_ARMOR = 0;
	public static final int BODY_ARMOR = 1;
	public static final int SHIELD_ARMOR = 2;
	public static final int MAGIC_ARMOR = 3;
	public static final int NUMBER_OF_ARMOR_TYPES = 4;
	
	public static class State {
		private int basePhysicalStrength;
		private int currentPhysicalStrength;
		private int basePhysicalHp;
		private int currentPhysicalHp;
		private int baseNaturalArmor; // aka physicalArmor
		private int currentNaturalArmor; // aka physicalArmor
		private int basePhysicalAccuracy;
		private int currentPhysicalAccuracy;
		private int baseSpiritualStrength;
		private int currentSpiritualStrength;
		private int baseSpiritualHp;
		private int currentSpiritualHp;
		private int baseResistanceToMagic; // aka spiritualArmor
		private int currentResistanceToMagic; // aka spiritualArmor
		private int baseSpiritualAccuracy;
		private int currentSpiritualAccuracy;
		private int baseRunningSpeed;
		private int currentRunningSpeed;

		private int rejectsOffers;
		private int followsOpponent;

		private int weaponDamage1;
		private int weaponDamage2;

		private Scene currentScene;
		private List<Obj> inventory = new ArrayList<Obj>();
		private Obj[] armor = new Obj[4];
		
		public State(Chr chr) {
			basePhysicalStrength = currentPhysicalStrength = chr.getPhysicalStrength();
			basePhysicalHp = currentPhysicalHp = chr.getPhysicalHp();
			baseNaturalArmor = currentNaturalArmor = chr.getNaturalArmor();
			basePhysicalAccuracy = currentPhysicalAccuracy = chr.getPhysicalAccuracy();
			baseSpiritualStrength = currentSpiritualStrength = chr.getSpiritualStrength();
			baseSpiritualHp = currentSpiritualHp = chr.getSpiritualHp();
			baseResistanceToMagic = currentResistanceToMagic = chr.getResistanceToMagic();
			baseSpiritualAccuracy = currentSpiritualAccuracy = chr.getSpiritualAccuracy();
			baseRunningSpeed = currentRunningSpeed = chr.getRunningSpeed();
			rejectsOffers = chr.getRejectsOffers();
			followsOpponent = chr.getFollowsOpponent();
			weaponDamage1 = chr.getWeaponDamage1();
			weaponDamage2 = chr.getWeaponDamage2();
		}
		
		public List<Obj> getInventory() {
			return inventory;
		}

		public Scene getCurrentScene() {
			return currentScene;
		}

		public void setCurrentScene(Scene currentScene) {
			this.currentScene = currentScene;
		}

		public Obj getArmor(int type) {
			return armor[type];
		}

		public void setArmor(int type, Obj obj) {
			armor[type] = obj;
		}

		public int getBasePhysicalStrength() {
			return basePhysicalStrength;
		}

		public void setBasePhysicalStrength(int basePhysicalStrength) {
			this.basePhysicalStrength = basePhysicalStrength;
		}

		public int getCurrentPhysicalStrength() {
			return currentPhysicalStrength;
		}

		public void setCurrentPhysicalStrength(int currentPhysicalStrength) {
			this.currentPhysicalStrength = currentPhysicalStrength;
		}

		public int getBasePhysicalHp() {
			return basePhysicalHp;
		}

		public void setBasePhysicalHp(int basePhysicalHp) {
			this.basePhysicalHp = basePhysicalHp;
		}

		public int getCurrentPhysicalHp() {
			return currentPhysicalHp;
		}

		public void setCurrentPhysicalHp(int currentPhysicalHp) {
			this.currentPhysicalHp = currentPhysicalHp;
		}

		public int getBaseNaturalArmor() {
			return baseNaturalArmor;
		}

		public void setBaseNaturalArmor(int baseNaturalArmor) {
			this.baseNaturalArmor = baseNaturalArmor;
		}

		public int getCurrentNaturalArmor() {
			return currentNaturalArmor;
		}

		public void setCurrentNaturalArmor(int currentNaturalArmor) {
			this.currentNaturalArmor = currentNaturalArmor;
		}

		public int getBasePhysicalAccuracy() {
			return basePhysicalAccuracy;
		}

		public void setBasePhysicalAccuracy(int basePhysicalAccuracy) {
			this.basePhysicalAccuracy = basePhysicalAccuracy;
		}

		public int getCurrentPhysicalAccuracy() {
			return currentPhysicalAccuracy;
		}

		public void setCurrentPhysicalAccuracy(int currentPhysicalAccuracy) {
			this.currentPhysicalAccuracy = currentPhysicalAccuracy;
		}

		public int getBaseSpiritualStrength() {
			return baseSpiritualStrength;
		}

		public void setBaseSpiritualStrength(int baseSpiritualStrength) {
			this.baseSpiritualStrength = baseSpiritualStrength;
		}

		public int getCurrentSpiritualStrength() {
			return currentSpiritualStrength;
		}

		public void setCurrentSpiritualStrength(int currentSpiritualStrength) {
			this.currentSpiritualStrength = currentSpiritualStrength;
		}

		public int getBaseSpiritualHp() {
			return baseSpiritualHp;
		}

		public void setBaseSpiritualHp(int baseSpiritualHp) {
			this.baseSpiritualHp = baseSpiritualHp;
		}

		public int getCurrentSpiritualHp() {
			return currentSpiritualHp;
		}

		public void setCurrentSpiritualHp(int currentSpiritualHp) {
			this.currentSpiritualHp = currentSpiritualHp;
		}

		public int getBaseResistanceToMagic() {
			return baseResistanceToMagic;
		}

		public void setBaseResistanceToMagic(int baseResistanceToMagic) {
			this.baseResistanceToMagic = baseResistanceToMagic;
		}

		public int getCurrentResistanceToMagic() {
			return currentResistanceToMagic;
		}

		public void setCurrentResistanceToMagic(int currentResistanceToMagic) {
			this.currentResistanceToMagic = currentResistanceToMagic;
		}

		public int getBaseSpiritualAccuracy() {
			return baseSpiritualAccuracy;
		}

		public void setBaseSpiritualAccuracy(int baseSpiritualAccuracy) {
			this.baseSpiritualAccuracy = baseSpiritualAccuracy;
		}

		public int getCurrentSpiritualAccuracy() {
			return currentSpiritualAccuracy;
		}

		public void setCurrentSpiritualAccuracy(int currentSpiritualAccuracy) {
			this.currentSpiritualAccuracy = currentSpiritualAccuracy;
		}

		public int getBaseRunningSpeed() {
			return baseRunningSpeed;
		}

		public void setBaseRunningSpeed(int baseRunningSpeed) {
			this.baseRunningSpeed = baseRunningSpeed;
		}

		public int getCurrentRunningSpeed() {
			return currentRunningSpeed;
		}

		public void setCurrentRunningSpeed(int currentRunningSpeed) {
			this.currentRunningSpeed = currentRunningSpeed;
		}

		public int getRejectsOffers() {
			return rejectsOffers;
		}

		public void setRejectsOffers(int rejectsOffers) {
			this.rejectsOffers = rejectsOffers;
		}

		public int getFollowsOpponent() {
			return followsOpponent;
		}

		public void setFollowsOpponent(int followsOpponent) {
			this.followsOpponent = followsOpponent;
		}

		public int getWeaponDamage1() {
			return weaponDamage1;
		}

		public void setWeaponDamage1(int weaponDamage1) {
			this.weaponDamage1 = weaponDamage1;
		}

		public int getWeaponDamage2() {
			return weaponDamage2;
		}

		public void setWeaponDamage2(int weaponDamage2) {
			this.weaponDamage2 = weaponDamage2;
		}
	}

	public Context getContext();
	public State getState();
	public void setState(State state);
	public Rectangle getDesignBounds();
	public String getAcceptsOfferComment();
	public String getDyingSound();
	public String getDyingWords();
	public int getFollowsOpponent();
	public String getInitialComment();
	public String getInitialSound();
	public int getLosingMagic();
	public int getLosingOffer();
	public int getLosingRun();
	public int getLosingWeapons();
	public String getMakesOfferComment();
	public Weapon[] getWeapons();
	public Obj[] getMagicalObjects();
	public boolean hasNativeWeapon1();
	public boolean hasNativeWeapon2();
	public String getNativeWeapon1();
	public String getNativeWeapon2();
	public int getNaturalArmor();
	public String getOperativeVerb1();
	public String getOperativeVerb2();
	public int getPhysicalAccuracy();
	public int getPhysicalHp();
	public int getPhysicalStrength();
	public void setPhysicalStrength(int physicalStrength);
	public String getReceivesHitComment();
	public String getReceivesHitSound();
	public String getRejectsOfferComment();
	public int getRejectsOffers();
	public int getResistanceToMagic();
	public int getRunningSpeed();
	public String getScoresHitComment();
	public String getScoresHitSound();
	public int getSpiritualHp();
	public int getSpiritualAccuracy();
	public int getSpiritualStrength();
	public int getWeaponDamage1();
	public int getWeaponDamage2();
	public String getWeaponSound1();
	public String getWeaponSound2();
	public int getWinningMagic();
	public int getWinningOffer();
	public int getWinningRun();
	public int getWinningWeapons();
	public Design getDesign();
	public String getName();
	public short getResourceID();
	public int getGender();
	public String getInitialScene();
	public int getMaximumCarriedObjects();
	public boolean isNameProperNoun();
	public void setNameProperNoun(boolean nameProperNoun);
	public boolean isPlayerCharacter();
	public int getReturnTo();
	public int getIndex();
}
