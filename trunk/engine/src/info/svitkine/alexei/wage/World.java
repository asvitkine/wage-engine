package info.svitkine.alexei.wage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class World {
	public static final String STORAGE = "STORAGE@";

	private String name;
	private String aboutMessage;
	private String soundLibrary1;
	private String soundLibrary2;
	private boolean weaponsMenuDisabled;
	private Script globalScript;
	private Map<String, Scene> scenes;
	private Map<String, Obj> objs;
	private Map<String, Chr> chrs;
	private Map<String, Sound> sounds;
	private List<Scene> orderedScenes;
	private List<Obj> orderedObjs;
	private List<Chr> orderedChrs;
	private List<Sound> orderedSounds;
	private List<byte[]> patterns;
	private Scene storageScene;
	private Chr player;
	private List<MoveListener> moveListeners;

	public World(Script globalScript) {
		this.globalScript = globalScript;
		scenes = new HashMap<String, Scene>();
		objs = new HashMap<String, Obj>();
		chrs = new HashMap<String, Chr>();
		sounds = new HashMap<String, Sound>();
		orderedScenes = new ArrayList<Scene>();
		orderedObjs = new ArrayList<Obj>();
		orderedChrs = new ArrayList<Chr>();
		orderedSounds = new ArrayList<Sound>();
		patterns = new ArrayList<byte[]>();
		storageScene = new Scene();
		storageScene.setName(STORAGE);
		orderedScenes.add(storageScene);
		scenes.put(STORAGE, storageScene);
		moveListeners = new LinkedList<MoveListener>();
	}

	public Scene getStorageScene() {
		return storageScene;
	}
	
	public void addScene(Scene room) {
		scenes.put(room.getName().toLowerCase(), room);
		orderedScenes.add(room);
	}

	public void addObj(Obj obj) {
		objs.put(obj.getName().toLowerCase(), obj);
		obj.setIndex(orderedObjs.size());
		orderedObjs.add(obj);
	}

	public void addChr(Chr chr) {
		chrs.put(chr.getName().toLowerCase(), chr);
		chr.setIndex(orderedChrs.size());
		orderedChrs.add(chr);
	}

	public void addSound(Sound sound) {
		sounds.put(sound.getName().toLowerCase(), sound);
		orderedSounds.add(sound);
	}

	public Context getPlayerContext() {
		return player.getContext();
	}

	public Script getGlobalScript() {
		return globalScript;
	}

	public Map<String, Scene> getScenes() {
		return scenes;
	}

	public Map<String, Obj> getObjs() {
		return objs;
	}

	public Map<String, Chr> getChrs() {
		return chrs;
	}

	public Map<String, Sound> getSounds() {
		return sounds;
	}

	public List<byte[]> getPatterns() {
		return patterns;
	}
	
	public Chr getPlayer() {
		return player;
	}

	public void setPlayer(Chr player) {
		this.player = player;
	}
	
	public Scene getSceneAt(int x, int y) {
		for (Scene scene : scenes.values()) {
			if (scene != storageScene && scene.getWorldX() == x && scene.getWorldY() == y) {
				return scene;
			}
		}
		return null;
	}

	public class MoveEvent {
		private Object what;
		private Object from;
		private Object to;
		public MoveEvent(Object what, Object from, Object to) {
			this.what = what;
			this.from = from;
			this.to = to;
		}
		public Object getWhat() {
			return what;
		}
		public Object getFrom() {
			return from;
		}
		public Object getTo() {
			return to;
		}
	}
	
	public interface MoveListener {
		public void onMove(MoveEvent event);
	}
	
	private void fireMoveEvent(MoveEvent event) {
		for (MoveListener ml : moveListeners)
			ml.onMove(event);
	}
	
	public void addMoveListener(MoveListener ml) {
		moveListeners.add(ml);
	}
	
	public void removeMoveListener(MoveListener ml) {
		moveListeners.remove(ml);
	}

	private Chr removeFromChr(Obj obj) {
		Chr owner = obj.getCurrentOwner();
		if (owner != null) {
			owner.getInventory().remove(obj);
			Obj[] armor = obj.getCurrentOwner().getArmor();
			for (int i = 0; i < armor.length; i++) {
				if (armor[i] == obj) {
					armor[i] = null;
				}
			}
		}
		return owner;
	}
	
	public void move(Obj obj, Chr chr) {
		if (obj == null)
			return;
		Object from = removeFromChr(obj);
		if (obj.getCurrentScene() != null) {
			obj.getCurrentScene().getObjs().remove(obj);
			from = obj.getCurrentScene();
		}
		obj.setCurrentOwner(chr);
		chr.getInventory().add(obj);
		sortObjs(chr.getInventory());
		fireMoveEvent(new MoveEvent(obj, from, chr));
	}

	public void move(Obj obj, Scene scene) {
		if (obj == null)
			return;
		Object from = removeFromChr(obj);
		if (obj.getCurrentScene() != null) {
			obj.getCurrentScene().getObjs().remove(obj);
			from = obj.getCurrentScene();
		}
		obj.setCurrentScene(scene);
		scene.getObjs().add(obj);
		sortObjs(scene.getObjs());
		fireMoveEvent(new MoveEvent(obj, from, scene));
	}

	private void initChrContext(Chr chr) {
		Context context = chr.getContext();
		context.setStatVariable(Context.PHYS_ACC_BAS, player.getPhysicalAccuracy());
		context.setStatVariable(Context.PHYS_ACC_CUR, player.getPhysicalAccuracy());
		context.setStatVariable(Context.PHYS_ARM_BAS, player.getNaturalArmor());
		context.setStatVariable(Context.PHYS_ARM_CUR, player.getNaturalArmor());
		context.setStatVariable(Context.PHYS_HIT_BAS, player.getPhysicalHp());
		context.setStatVariable(Context.PHYS_HIT_CUR, player.getPhysicalHp());
		context.setStatVariable(Context.PHYS_SPE_BAS, player.getRunningSpeed());
		context.setStatVariable(Context.PHYS_SPE_CUR, player.getRunningSpeed());
		context.setStatVariable(Context.PHYS_STR_BAS, player.getPhysicalStrength());
		context.setStatVariable(Context.PHYS_STR_CUR, player.getPhysicalStrength());
		context.setStatVariable(Context.SPIR_ACC_BAS, player.getSpiritualAccuracy());
		context.setStatVariable(Context.SPIR_ACC_CUR, player.getSpiritualAccuracy());
		context.setStatVariable(Context.SPIR_ARM_BAS, player.getResistanceToMagic());
		context.setStatVariable(Context.SPIR_ARM_CUR, player.getResistanceToMagic());
		context.setStatVariable(Context.SPIR_HIT_BAS, player.getSpiritialHp());
		context.setStatVariable(Context.SPIR_HIT_CUR, player.getSpiritialHp());
		context.setStatVariable(Context.SPIR_STR_BAS, player.getSpiritualStength());
		context.setStatVariable(Context.SPIR_STR_CUR, player.getSpiritualStength());
		context.setVisits(1);
		context.setKills(0);
	}

	public void move(Chr chr, Scene scene) {
		if (chr == null)
			return;
		Scene from = chr.getCurrentScene();
		if (from != scene) {
			if (from != null)
				from.getChrs().remove(chr);
			chr.setCurrentScene(scene);
			scene.getChrs().add(chr);
			sortChrs(scene.getChrs());
			if (from == storageScene) {
				initChrContext(chr);
			} else {
				chr.getContext().setVisits(chr.getContext().getVisits() + 1);
			}
			fireMoveEvent(new MoveEvent(chr, from, scene));
		}
	}

	private void sortObjs(List<Obj> objs) {
		Collections.sort(objs, new Comparator<Obj>() {
			public int compare(Obj o1, Obj o2) {
				return o1.getIndex() - o2.getIndex();
			}
		});
	}
	
	private void sortChrs(List<Chr> chrs) {
		Collections.sort(chrs, new Comparator<Chr>() {
			public int compare(Chr c1, Chr c2) {
				return c1.getIndex() - c2.getIndex();
			}
		});
	}

	public List<Chr> getOrderedChrs() {
		return orderedChrs;
	}

	public List<Obj> getOrderedObjs() {
		return orderedObjs;
	}

	public List<Scene> getOrderedScenes() {
		return orderedScenes;
	}

	public List<Sound> getOrderedSounds() {
		return orderedSounds;
	}

	public String getAboutMessage() {
		return aboutMessage;
	}

	public void setAboutMessage(String aboutMessage) {
		this.aboutMessage = aboutMessage;
	}

	public boolean isWeaponsMenuDisabled() {
		return weaponsMenuDisabled;
	}

	public void setWeaponsMenuDisabled(boolean weaponsMenuDisabled) {
		this.weaponsMenuDisabled = weaponsMenuDisabled;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSoundLibrary1() {
		return soundLibrary1;
	}

	public void setSoundLibrary1(String soundLibrary1) {
		this.soundLibrary1 = soundLibrary1;
	}

	public String getSoundLibrary2() {
		return soundLibrary2;
	}

	public void setSoundLibrary2(String soundLibrary2) {
		this.soundLibrary2 = soundLibrary2;
	}
}
