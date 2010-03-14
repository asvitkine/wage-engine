package info.svitkine.alexei.wage;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;


public class Chr {
	public static final int RETURN_TO_STORAGE = 0;
	public static final int RETURN_TO_RANDOM_SCENE = 1;
	public static final int RETURN_TO_INITIAL_SCENE = 2;

	public static final int HEAD = 1;
	public static final int CHEST = 2;
	public static final int SIDE = 3;
	
	private int index;
	private String name;
	private short resourceID;
	private Design design;
	private Rectangle designBounds;
	private String initialScene;
	private int gender;
	private boolean nameProperNoun;
	private boolean playerCharacter;
	private int maximumCarriedObjects;
	private int returnTo;
	
	private int physicalStrength;
	private int physicalHp;
	private int naturalArmor;
	private int physicalAccuracy;
	private int spiritualStength;
	private int spiritialHp;
	private int resistanceToMagic;
	private int spiritualAccuracy;
	private int runningSpeed;
	private int rejectsOffers;
	private int followsOpponent;
	
	private String initialSound;
	private String scoresHitSound;
	private String receivesHitSound;
	private String dyingSound;

	private String nativeWeapon1;
	private String operativeVerb1;
	private int weaponDamage1;
	private String weaponSound1;
	
	private String nativeWeapon2;
	private String operativeVerb2;
	private int weaponDamage2;
	private String weaponSound2;
	
	private int winningWeapons;
	private int winningMagic;
	private int winningRun;
	private int winningOffer;
	private int losingWeapons;
	private int losingMagic;
	private int losingRun;
	private int losingOffer;
	
	private String initialComment;
	private String scoresHitComment;
	private String receivesHitComment;
	private String makesOfferComment;
	private String rejectsOfferComment;
	private String acceptsOfferComment;
	private String dyingWords;

	public static final int HEAD_ARMOR = 0;
	public static final int BODY_ARMOR = 1;
	public static final int SHIELD_ARMOR = 2;
	public static final int MAGIC_ARMOR = 3;
	public static final int NUMBER_OF_ARMOR_TYPES = 4;

	private State state;
	
	public static class State {
		private Scene currentScene;
		private List<Obj> inventory = new ArrayList<Obj>();
		private Obj[] armor = new Obj[4];

		public State(Chr chr) {
			
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
	}

	private Context context = new Context();

	public Context getContext() {
		return context;
	}
	
	public State getState() {
		if (state == null)
			state = new State(this);
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Rectangle getDesignBounds() {
		return designBounds == null ? null : new Rectangle(designBounds);
	}

	public void setDesignBounds(Rectangle bounds) {
		this.designBounds = new Rectangle(bounds);
		design.setBounds(bounds);
	}
	
	public String getAcceptsOfferComment() {
		return acceptsOfferComment;
	}

	public void setAcceptsOfferComment(String acceptsOfferComment) {
		this.acceptsOfferComment = acceptsOfferComment;
	}

	public String getDyingSound() {
		return dyingSound;
	}

	public void setDyingSound(String dyingSound) {
		this.dyingSound = dyingSound;
	}

	public String getDyingWords() {
		return dyingWords;
	}

	public void setDyingWords(String dyingWords) {
		this.dyingWords = dyingWords;
	}

	public int getFollowsOpponent() {
		return followsOpponent;
	}

	public void setFollowsOpponent(int followsOpponent) {
		this.followsOpponent = followsOpponent;
	}

	public String getInitialComment() {
		return initialComment;
	}

	public void setInitialComment(String initialComment) {
		this.initialComment = initialComment;
	}

	public String getInitialSound() {
		return initialSound;
	}

	public void setInitialSound(String initialSound) {
		this.initialSound = initialSound;
	}

	public int getLosingMagic() {
		return losingMagic;
	}

	public void setLosingMagic(int losingMagic) {
		this.losingMagic = losingMagic;
	}

	public int getLosingOffer() {
		return losingOffer;
	}

	public void setLosingOffer(int losingOffer) {
		this.losingOffer = losingOffer;
	}

	public int getLosingRun() {
		return losingRun;
	}

	public void setLosingRun(int losingRun) {
		this.losingRun = losingRun;
	}

	public int getLosingWeapons() {
		return losingWeapons;
	}

	public void setLosingWeapons(int losingWeapons) {
		this.losingWeapons = losingWeapons;
	}

	public String getMakesOfferComment() {
		return makesOfferComment;
	}

	public void setMakesOfferComment(String makesOfferComment) {
		this.makesOfferComment = makesOfferComment;
	}

	public Weapon[] getWeapons() {
		ArrayList<Weapon> weapons = new ArrayList<Weapon>();
		if (hasNativeWeapon1()) {
			weapons.add(new Weapon() {
				public String getName() {
					return getNativeWeapon1();
				}
				public String getOperativeVerb() {
					return getOperativeVerb1();
				}
				public int getType() {
					return Obj.REGULAR_WEAPON;
				}
				public int getAccuracy() {
					return 0;
				}
				public int getDamage() {
					return getWeaponDamage1();
				}
				public String getSound() {
					return getWeaponSound1();
				}
				public String getFailureMessage() {
					return null;
				}
			});
		}
		if (hasNativeWeapon2()) {
			weapons.add(new Weapon() {
				public String getName() {
					return getNativeWeapon2();
				}
				public String getOperativeVerb() {
					return getOperativeVerb2();
				}
				public int getType() {
					return Obj.REGULAR_WEAPON;
				}
				public int getAccuracy() {
					return 0;
				}
				public int getDamage() {
					return getWeaponDamage2();
				}
				public String getSound() {
					return getWeaponSound2();
				}
				public String getFailureMessage() {
					return null;
				}
			});
		}
		for (Obj o : state.getInventory()) {
			switch (o.getType()) {
				case Obj.REGULAR_WEAPON:
				case Obj.THROW_WEAPON:
				case Obj.MAGICAL_OBJECT:
					weapons.add(o);
			}
		}
		return (Weapon[]) weapons.toArray(new Weapon[0]);
	}
	

	public Obj[] getMagicalObjects() {
		ArrayList<Obj> magic = new ArrayList<Obj>();
		for (Obj obj : state.getInventory()) {
			if (obj.getType() == Obj.MAGICAL_OBJECT) {
				magic.add(obj);
			}
		}
		return magic.toArray(new Obj[0]);
	}

	public boolean hasNativeWeapon1() {
		return nativeWeapon1 != null && operativeVerb1 != null &&
			nativeWeapon1.length() > 0 && operativeVerb1.length() > 0;
	}

	public boolean hasNativeWeapon2() {
		return nativeWeapon2 != null && operativeVerb2 != null &&
			nativeWeapon2.length() > 0 && operativeVerb2.length() > 0;
	}

	public String getNativeWeapon1() {
		return nativeWeapon1;
	}

	public void setNativeWeapon1(String nativeWeapon1) {
		this.nativeWeapon1 = nativeWeapon1;
	}

	public String getNativeWeapon2() {
		return nativeWeapon2;
	}

	public void setNativeWeapon2(String nativeWeapon2) {
		this.nativeWeapon2 = nativeWeapon2;
	}

	public int getNaturalArmor() {
		return naturalArmor;
	}

	public void setNaturalArmor(int naturalArmor) {
		this.naturalArmor = naturalArmor;
	}

	public String getOperativeVerb1() {
		return operativeVerb1;
	}

	public void setOperativeVerb1(String operativeVerb1) {
		this.operativeVerb1 = operativeVerb1;
	}

	public String getOperativeVerb2() {
		return operativeVerb2;
	}

	public void setOperativeVerb2(String operativeVerb2) {
		this.operativeVerb2 = operativeVerb2;
	}

	public int getPhysicalAccuracy() {
		return physicalAccuracy;
	}

	public void setPhysicalAccuracy(int physicalAccuracy) {
		this.physicalAccuracy = physicalAccuracy;
	}

	public int getPhysicalHp() {
		return physicalHp;
	}

	public void setPhysicalHp(int physicalHp) {
		this.physicalHp = physicalHp;
	}

	public int getPhysicalStrength() {
		return physicalStrength;
	}

	public void setPhysicalStrength(int physicalStrength) {
		this.physicalStrength = physicalStrength;
	}

	public String getReceivesHitComment() {
		return receivesHitComment;
	}

	public void setReceivesHitComment(String receivesHitComment) {
		this.receivesHitComment = receivesHitComment;
	}

	public String getReceivesHitSound() {
		return receivesHitSound;
	}

	public void setReceivesHitSound(String receivesHitSound) {
		this.receivesHitSound = receivesHitSound;
	}

	public String getRejectsOfferComment() {
		return rejectsOfferComment;
	}

	public void setRejectsOfferComment(String rejectsOfferComment) {
		this.rejectsOfferComment = rejectsOfferComment;
	}

	public int getRejectsOffers() {
		return rejectsOffers;
	}

	public void setRejectsOffers(int rejectsOffers) {
		this.rejectsOffers = rejectsOffers;
	}

	public int getResistanceToMagic() {
		return resistanceToMagic;
	}

	public void setResistanceToMagic(int resistanceToMagic) {
		this.resistanceToMagic = resistanceToMagic;
	}

	public int getRunningSpeed() {
		return runningSpeed;
	}

	public void setRunningSpeed(int runningSpeed) {
		this.runningSpeed = runningSpeed;
	}

	public String getScoresHitComment() {
		return scoresHitComment;
	}

	public void setScoresHitComment(String scoresHitComment) {
		this.scoresHitComment = scoresHitComment;
	}

	public String getScoresHitSound() {
		return scoresHitSound;
	}

	public void setScoresHitSound(String scoresHitSound) {
		this.scoresHitSound = scoresHitSound;
	}

	public int getSpiritialHp() {
		return spiritialHp;
	}

	public void setSpiritialHp(int spiritialHp) {
		this.spiritialHp = spiritialHp;
	}

	public int getSpiritualAccuracy() {
		return spiritualAccuracy;
	}

	public void setSpiritualAccuracy(int spiritualAccuracy) {
		this.spiritualAccuracy = spiritualAccuracy;
	}

	public int getSpiritualStength() {
		return spiritualStength;
	}

	public void setSpiritualStength(int spiritualStength) {
		this.spiritualStength = spiritualStength;
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

	public String getWeaponSound1() {
		return weaponSound1;
	}

	public void setWeaponSound1(String weaponSound1) {
		this.weaponSound1 = weaponSound1;
	}

	public String getWeaponSound2() {
		return weaponSound2;
	}

	public void setWeaponSound2(String weaponSound2) {
		this.weaponSound2 = weaponSound2;
	}

	public int getWinningMagic() {
		return winningMagic;
	}

	public void setWinningMagic(int winningMagic) {
		this.winningMagic = winningMagic;
	}

	public int getWinningOffer() {
		return winningOffer;
	}

	public void setWinningOffer(int winningOffer) {
		this.winningOffer = winningOffer;
	}

	public int getWinningRun() {
		return winningRun;
	}

	public void setWinningRun(int winningRun) {
		this.winningRun = winningRun;
	}

	public int getWinningWeapons() {
		return winningWeapons;
	}

	public void setWinningWeapons(int winningWeapons) {
		this.winningWeapons = winningWeapons;
	}

	public Design getDesign() {
		return design;
	}

	public void setDesign(Design design) {
		this.design = design;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public short getResourceID() {
		return resourceID;
	}
	
	public void setResourceID(short resourceID) {
		this.resourceID = resourceID;
	}

	public String toString() {
		return name;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getInitialScene() {
		return initialScene;
	}

	public void setInitialScene(String initialScene) {
		this.initialScene = initialScene;
	}

	public int getMaximumCarriedObjects() {
		return maximumCarriedObjects;
	}

	public void setMaximumCarriedObjects(int maximumCarriedObjects) {
		this.maximumCarriedObjects = maximumCarriedObjects;
	}

	public boolean isNameProperNoun() {
		return nameProperNoun;
	}

	public void setNameProperNoun(boolean nameProperNoun) {
		this.nameProperNoun = nameProperNoun;
	}

	public boolean isPlayerCharacter() {
		return playerCharacter;
	}

	public void setPlayerCharacter(boolean playerCharacter) {
		this.playerCharacter = playerCharacter;
	}

	public int getReturnTo() {
		return returnTo;
	}

	public void setReturnTo(int returnTo) {
		this.returnTo = returnTo;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
