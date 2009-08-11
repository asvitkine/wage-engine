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
	private Scene storageScene;
	private Chr player;
	private Context playerContext;
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
		storageScene = new Scene();
		storageScene.setName(STORAGE);
		orderedScenes.add(storageScene);
		scenes.put(STORAGE, storageScene);
		playerContext = new Context();
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
		return playerContext;
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

	public Chr getPlayer() {
		return player;
	}

	public void setPlayer(Chr player) {
		this.player = player;
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

	public void move(Obj obj, Chr chr) {
		if (obj == null)
			return;
		Object from = null;
		if (obj.getCurrentOwner() != null) {
			obj.getCurrentOwner().getInventory().remove(obj);
			from = obj.getCurrentOwner();
		}
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
		Object from = null;
		if (obj.getCurrentOwner() != null) {
			obj.getCurrentOwner().getInventory().remove(obj);
			from = obj.getCurrentOwner();
		}
		if (obj.getCurrentScene() != null) {
			obj.getCurrentScene().getObjs().remove(obj);
			from = obj.getCurrentScene();
		}
		obj.setCurrentScene(scene);
		scene.getObjs().add(obj);
		sortObjs(scene.getObjs());
		fireMoveEvent(new MoveEvent(obj, from, scene));
	}

	public void move(Chr chr, Scene scene) {
		if (chr == null)
			return;
		Object from = null;
		if (chr.getCurrentScene() != null) {
			chr.getCurrentScene().getChrs().remove(chr);
			from = chr.getCurrentScene();
		}
		chr.setCurrentScene(scene);
		scene.getChrs().add(chr);
		sortChrs(scene.getChrs());
		fireMoveEvent(new MoveEvent(chr, from, scene));
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
