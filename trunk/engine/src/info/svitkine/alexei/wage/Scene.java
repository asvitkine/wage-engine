package info.svitkine.alexei.wage;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Scene {
	public static final int NORTH = 0;
	public static final int SOUTH = 1;
	public static final int EAST = 2;
	public static final int WEST = 3;
	
	public static final int PERIODIC = 0;
	public static final int RANDOM = 1;

	private int index;
	private String name;
	private short resourceID;
	private Script script;
	private Design design;
	private Rectangle designBounds;
	private String text;
	private Rectangle textBounds;
	private int fontSize;
	private int fontType; // 3 => Geneva, 22 => Courier, param to TextFont() function
	private boolean[] blocked = new boolean[4];
	private String[] messages = new String[4];
	private int soundFrequency; // times a minute, max 3600
	private int soundType;
	private String soundName;
	private int worldX;
	private int worldY;
	
	private List<Obj> objs = new ArrayList<Obj>();
	private List<Chr> chrs = new ArrayList<Chr>();

	public int getSoundFrequency() {
		return soundFrequency;
	}

	public void setSoundFrequency(int soundFrequency) {
		this.soundFrequency = soundFrequency;
	}

	public Rectangle getDesignBounds() {
		return designBounds == null ? null : new Rectangle(designBounds);
	}

	public void setDesignBounds(Rectangle bounds) {
		this.designBounds = new Rectangle(bounds);
		design.setBounds(bounds);
	}

	public Rectangle getTextBounds() {
		return textBounds == null ? null : new Rectangle(textBounds);
	}

	public void setTextBounds(Rectangle bounds) {
		this.textBounds = new Rectangle(bounds);
	}
	
	public void setDirMessage(int dir, String message) {
		messages[dir] = message;
	}
	
	public String getDirMessage(int dir) {
		return messages[dir];
	}

	public void setDirBlocked(int dir, boolean blocked) {
		this.blocked[dir] = blocked;
	}
	
	public boolean isDirBlocked(int dir) {
		return blocked[dir];
	}

	public Design getDesign() {
		return design;
	}

	public void setDesign(Design design) {
		this.design = design;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
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

	public short getHexOffset(State state) {
		return (short) ((index * State.SCENE_SIZE) + State.SCENES_INDEX);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
	}

	public int getSoundType() {
		return soundType;
	}

	public void setSoundType(int soundType) {
		this.soundType = soundType;
	}

	public int getWorldX() {
		return worldX;
	}

	public void setWorldX(int worldX) {
		this.worldX = worldX;
	}

	public int getWorldY() {
		return worldY;
	}

	public void setWorldY(int worldY) {
		this.worldY = worldY;
	}

	public String getSoundName() {
		return soundName;
	}

	public void setSoundName(String soundName) {
		this.soundName = soundName;
	}

	public String toString() {
		return getName();
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public int getFontType() {
		return fontType;
	}
	
	public String getFontName() {
		String[] fonts = {
			"Chicago",	// system font
			"Geneva",	// application font
			"New York",
			"Geneva",

			"Monaco",
			"Venice",
			"London",
			"Athens",
	
			"San Francisco",
			"Toronto",
			"Cairo",
			"Los Angeles", // 12

			null, null, null, null, null, null, null, // not in Inside Macintosh

			"Times", // 20
			"Helvetica",
			"Courier",
			"Symbol",
			"Taliesin" // mobile?
		};
		/*
mappings found on some forums:
systemFont(0):System(Swiss)
times(20):Times New Roman(Roman)
helvetica(21):Arial(Modern)
courier(22):Courier New(Modern)
symbol(23):Symbol(Decorative)
applFont(1):Arial(Swiss)
newYork(2):Times New Roman(Roman)
geneva(3):Arial(Swiss)
monaco(4):Courier New(Modern)
venice(5):Times New Roman(Roman)
london(6):Times New Roman(Roman)
athens(7):Times New Roman(Roman)
sanFran(8):Times New Roman(Roman)
toronto(9):Times New Roman(Roman)
cairo(11):Wingdings(Decorative)
losAngeles(12):Times New Roman(Roman)
taliesin(24):Wingdings(Decorative)
		 */
		if (fontType >= 0 && fontType < fonts.length && fonts[fontType] != null) {
			return fonts[fontType];
		}
		return "Unknown";
	}

	public void setFontType(int fontType) {
		this.fontType = fontType;
	}

	public List<Chr> getChrs() {
		return chrs;
	}

	public List<Obj> getObjs() {
		return objs;
	}
}
