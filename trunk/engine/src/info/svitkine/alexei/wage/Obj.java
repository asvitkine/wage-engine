package info.svitkine.alexei.wage;

import java.awt.Rectangle;


public class Obj implements Weapon {
	// object types:
	public static final int REGULAR_WEAPON = 1;
	public static final int THROW_WEAPON = 2;
	public static final int MAGICAL_OBJECT = 3;
	public static final int HELMET = 4;
	public static final int SHIELD = 5;
	public static final int CHEST_ARMOR = 6;
	public static final int SPIRITUAL_ARMOR = 7;
	public static final int MOBILE_OBJECT = 8;
	public static final int IMMOBILE_OBJECT = 9;

	// attack types:
	public static final int CAUSES_PHYSICAL_DAMAGE = 0;
	public static final int CAUSES_SPIRITUAL_DAMAGE = 1;
	public static final int CAUSES_PHYSICAL_AND_SPIRITUAL_DAMAGE = 2;
	public static final int HEALS_PHYSICAL_DAMAGE = 3;
	public static final int HEALS_SPIRITUAL_DAMAGE = 4;
	public static final int HEALS_PHYSICAL_AND_SPIRITUAL_DAMAGE = 5;
	public static final int FREEZES_OPPONENT = 6;

	private int index;
	private String name;
	private short resourceID;
	private boolean namePlural;
	private Design design;
	private Rectangle designBounds;
	private int type;
	private int value;
	private int damage; // or protection, if armor / helmet. etc
	private int accuracy;
	private int attackType;
	private int numberOfUses;
	private int currentNumberOfUses;
	private boolean returnToRandomScene;
	private String sceneOrOwner;
	private String clickMessage;
	private String operativeVerb;
	private String failureMessage;
	private String useMessage;
	private String sound;
	
	private Scene currentScene;
	private Chr currentOwner;

	public Chr getCurrentOwner() {
		return currentOwner;
	}

	public void setCurrentOwner(Chr currentOwner) {
		this.currentOwner = currentOwner;
		if (currentOwner != null)
			currentScene = null;
	}

	public Scene getCurrentScene() {
		return currentScene;
	}

	public void setCurrentScene(Scene currentScene) {
		this.currentScene = currentScene;
		if (currentScene != null)
			currentOwner = null;
	}

	public Design getDesign() {
		return design;
	}

	public void setDesign(Design design) {
		this.design = design;
	}

	public Rectangle getDesignBounds() {
		return designBounds == null ? null : new Rectangle(designBounds);
	}

	public void setDesignBounds(Rectangle bounds) {
		this.designBounds = new Rectangle(bounds);
		design.setBounds(bounds);
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

	public int getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}

	public int getAttackType() {
		return attackType;
	}

	public void setAttackType(int attackType) {
		this.attackType = attackType;
	}

	public String getClickMessage() {
		return clickMessage;
	}

	public void setClickMessage(String clickMessage) {
		this.clickMessage = clickMessage;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public String getFailureMessage() {
		return failureMessage;
	}

	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}

	public int getNumberOfUses() {
		return numberOfUses;
	}

	public void setNumberOfUses(int numberOfUses) {
		this.numberOfUses = numberOfUses;
	}

	public int getCurrentNumberOfUses() {
		return currentNumberOfUses;
	}

	public void setCurrentNumberOfUses(int numberOfUses) {
		this.currentNumberOfUses = numberOfUses;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getOperativeVerb() {
		return operativeVerb;
	}

	public void setOperativeVerb(String operativeVerb) {
		this.operativeVerb = operativeVerb;
	}

	public boolean getReturnToRandomScene() {
		return returnToRandomScene;
	}

	public void setReturnToRandomScene(boolean returnToRandomScene) {
		this.returnToRandomScene = returnToRandomScene;
	}

	public String getSceneOrOwner() {
		return sceneOrOwner;
	}

	public void setSceneOrOwner(String sceneOrOwner) {
		this.sceneOrOwner = sceneOrOwner;
	}

	public String getSound() {
		return sound;
	}

	public void setSound(String sound) {
		this.sound = sound;
	}

	public String getUseMessage() {
		return useMessage;
	}

	public void setUseMessage(String useMessage) {
		this.useMessage = useMessage;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean isNamePlural() {
		return namePlural;
	}

	public void setNamePlural(boolean namePlural) {
		this.namePlural = namePlural;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
