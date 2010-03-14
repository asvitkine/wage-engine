package info.svitkine.alexei.wage;

import java.awt.Rectangle;


public class ObjImpl implements Obj {
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
	private boolean returnToRandomScene;
	private String sceneOrOwner;
	private String clickMessage;
	private String operativeVerb;
	private String failureMessage;
	private String useMessage;
	private String sound;

	private State state;

	public State getState() {
		if (state == null)
			state = new State(this);
		return state;
	}

	public void setState(State state) {
		this.state = state;
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
