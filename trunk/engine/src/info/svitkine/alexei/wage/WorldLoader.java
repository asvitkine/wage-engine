package info.svitkine.alexei.wage;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;

import org.freeshell.gbsmith.rescafe.resourcemanager.*;
import org.freeshell.gbsmith.rescafe.MacBinaryHeader;

import com.fizzysoft.sdu.RecentDocumentsManager;


public class WorldLoader {
	private static WorldLoader instance;

	private RecentDocumentsManager rdm;
	
	protected WorldLoader() {
		rdm = new RecentDocumentsManager() {
			private Preferences getPreferences() {
				return Preferences.userNodeForPackage(WorldLoader.class);
			}

			@Override
			protected byte[] readRecentDocs() {
				return getPreferences().getByteArray("RecentDocuments", null);
			}

			@Override
			protected void writeRecentDocs(byte[] data) {
				getPreferences().putByteArray("RecentDocuments", data);
			}

			@Override
			protected void openFile(File file, ActionEvent event) {
				// TODO Auto-generated method stub
			}
		};
	}

	public static WorldLoader getInstance() {
		if (instance == null)
			instance = new WorldLoader();
		return instance;
	}
	
	public RecentDocumentsManager getRecentDocumentsManager() {
		return rdm;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		System.setProperty("apple.awt.graphics.UseQuartz", "true");
		//System.setProperty("apple.laf.useScreenMenuBar", "true");
		FileDialog dialog = new FileDialog(new Frame(), "Open File", FileDialog.LOAD);
		dialog.setVisible(true);
		if (dialog.getFile() == null)
			return;
		File file = new File(dialog.getDirectory() + "/" + dialog.getFile());
		ResourceModel model = loadResources(file);
		JFrame f = new JFrame();
		//JMenuBar menubar = new JMenuBar();
		//JMenu menu = new JMenu("File");
		//menu.add(WorldLoader.getInstance().getRecentDocumentsManager().createOpenRecentMenu());
		//menubar.add(menu);
		//f.setJMenuBar(menubar);
		Utils.setupCloseWindowKeyStrokes(f, f.getRootPane());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getRootPane().putClientProperty("Window.documentFile", file);
		f.setTitle(dialog.getFile());
		f.setContentPane(new WorldBrowser(WorldLoader.getInstance().loadWorld(model, file)));
		f.setSize(640, 480);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	private static ResourceModel loadResources(File file) throws IOException {
		ResourceModel model = new ResourceModel(file.getName());
		RandomAccessFile raf = new RandomAccessFile(file.getPath(), "r");
		MacBinaryHeader mbh = new MacBinaryHeader();
		mbh.read(raf);
		if (mbh.validate()) {
			raf.seek(mbh.getResForkOffset());
			model.read(raf, mbh.getResForkOffset());
		} else {
			raf = new RandomAccessFile(file.getPath() + "/rsrc", "r");
			model.read(raf);
		}
		return model;
	}

	public World loadWorld(ResourceModel model, File file) throws UnsupportedEncodingException {
		rdm.addDocument(file, null);
		World world = new World(new Script(model.getResource("GCOD", (short) 0).getData()));
		world.setName(file.getName());
		ResourceType vers = model.getResourceType("VERS");
		if (vers != null) {
			Resource r = vers.getResArray()[0];
			byte[] data = r.getData();
			try {
				DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
				in.skip(10);
				byte b = in.readByte();
				world.setWeaponsMenuDisabled(b != 0);
				if (b != 1 && b != 0)
					System.err.println("Unexpected value for weapons menu!");
				in.skip(3);
				String about = readPascalString(in);
				about.replace((char) 0x0D, '\n');
				world.setAboutMessage(about);
				if (file.getName().equals("Scepters"))
					in.skip(1); // ????
				world.setSoundLibrary1(readPascalString(in));
				world.setSoundLibrary2(readPascalString(in));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ResourceType scenes = model.getResourceType("ASCN");
		if (scenes != null) {
			for (Resource r : scenes.getResArray()) {
				Scene scene = parseSceneData(r.getName(), r.getData());
				Resource code = model.getResource("ACOD", r.getID());
				if (code != null) {
					scene.setScript(new Script(code.getData()));
				}
				Resource text = model.getResource("ATXT", r.getID());
				if (text != null) {
					byte[] data = text.getData();
					try {
						DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
						Rectangle bounds = readRectangle(in);
						scene.setTextBounds(bounds);
						scene.setFontType(in.readShort());
						scene.setFontSize(in.readShort());
					} catch (IOException e) {
						e.printStackTrace();
					}
					for (int i = 12; i < data.length; i++)
						if (data[i] == (byte) 0x0D)
							data[i] = '\n';
					scene.setText(new String(data, 12, data.length-12, "MacRoman"));
				}
				world.addScene(scene);
			}
		}
		ResourceType objs = model.getResourceType("AOBJ");
		if (objs != null) {
			for (Resource r : objs.getResArray())
				world.addObj(parseObjData(r.getName(), r.getData()));
		}
		ResourceType chrs = model.getResourceType("ACHR");
		if (chrs != null) {
			for (Resource r : chrs.getResArray()) {
				Chr chr = parseChrData(r.getName(), r.getData());
				world.addChr(chr);
				// TODO: What if there's more than one player character?
				if (chr.isPlayerCharacter())
					world.setPlayer(chr);
			}
		}
		ResourceType sounds = model.getResourceType("ASND");
		if (sounds != null) {
			for (Resource r : sounds.getResArray()) {
				Sound sound = new Sound(r.getData());
				sound.setName(r.getName());
				world.addSound(sound);
			}
		}
		if (world.getSoundLibrary1() != null && world.getSoundLibrary1().length() > 0) {
			loadExternalSounds(world, file, world.getSoundLibrary1());
		}
		if (world.getSoundLibrary2() != null && world.getSoundLibrary2().length() > 0) {
			loadExternalSounds(world, file, world.getSoundLibrary2());
		}
		ResourceType patterns = model.getResourceType("PAT#");
		if (patterns != null) {
			Resource r = patterns.getResource((short) 900);
			if (r != null) {
				DataInputStream in = new DataInputStream(new ByteArrayInputStream(r.getData()));
				try {
					short count = in.readShort();
					for (int i = 0; i < count; i++) {
						byte[] pattern = new byte[8];
						in.readFully(pattern);
						world.getPatterns().add(pattern);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return world;
	}

	private void loadExternalSounds(World world, File worldFile, String soundsFileName) {
		ResourceModel soundLibraryModel = null;
		File file = new File(worldFile.getParent() + "/" + soundsFileName);
		if (file.exists()) {
			try {
				soundLibraryModel = loadResources(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (soundLibraryModel != null) {
				ResourceType sounds = soundLibraryModel.getResourceType("ASND");
				if (sounds != null) {
					for (Resource r : sounds.getResArray()) {
						Sound sound = new Sound(r.getData());
						sound.setName(r.getName());
						world.addSound(sound);
					}
				}
			}
		}
	}
	
	private Chr parseChrData(String chrName, byte[] data) {
		Chr chr = new Chr();
		chr.setName(chrName);
		chr.setDesign(new Design(data));
		try {
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
			in.skip(in.readShort() - 2); // Skip design.
			chr.setDesignBounds(readRectangle(in));
			
			chr.setPhysicalStrength(readUnsignedByte(in));
			chr.setPhysicalHp(readUnsignedByte(in));
			chr.setNaturalArmor(readUnsignedByte(in));
			chr.setPhysicalAccuracy(readUnsignedByte(in));
			
			chr.setSpiritualStength(readUnsignedByte(in));
			chr.setSpiritialHp(readUnsignedByte(in));
			chr.setResistanceToMagic(readUnsignedByte(in));
			chr.setSpiritualAccuracy(readUnsignedByte(in));
			
			chr.setRunningSpeed(readUnsignedByte(in));
			chr.setRejectsOffers(readUnsignedByte(in));
			chr.setFollowsOpponent(readUnsignedByte(in));

			in.readByte(); // TODO: ???
			in.readInt(); // TODO: ???

			chr.setWeaponDamage1(readUnsignedByte(in));
			chr.setWeaponDamage2(readUnsignedByte(in));
			
			in.readByte(); // TODO: ???

			if (in.readByte() == 1)
				chr.setPlayerCharacter(true);
			chr.setMaximumCarriedObjects(readUnsignedByte(in));
			chr.setReturnTo(in.readByte());

			chr.setWinningWeapons(readUnsignedByte(in));
			chr.setWinningMagic(readUnsignedByte(in));
			chr.setWinningRun(readUnsignedByte(in));
			chr.setWinningOffer(readUnsignedByte(in));
			chr.setLosingWeapons(readUnsignedByte(in));
			chr.setLosingMagic(readUnsignedByte(in));
			chr.setLosingRun(readUnsignedByte(in));
			chr.setLosingOffer(readUnsignedByte(in));
			
			chr.setGender(in.readByte());
			if (in.readByte() == 1)
				chr.setNameProperNoun(true);
			
			chr.setInitialScene(readPascalString(in));
			chr.setNativeWeapon1(readPascalString(in));
			chr.setOperativeVerb1(readPascalString(in));
			chr.setNativeWeapon2(readPascalString(in));
			chr.setOperativeVerb2(readPascalString(in));
			
			chr.setInitialComment(readPascalString(in));
			chr.setScoresHitComment(readPascalString(in));
			chr.setReceivesHitComment(readPascalString(in));
			chr.setMakesOfferComment(readPascalString(in));
			chr.setRejectsOfferComment(readPascalString(in));
			chr.setAcceptsOfferComment(readPascalString(in));
			chr.setDyingWords(readPascalString(in));
			
			chr.setInitialSound(readPascalString(in));
			chr.setScoresHitSound(readPascalString(in));
			chr.setReceivesHitSound(readPascalString(in));
			chr.setDyingSound(readPascalString(in));
			
			chr.setWeaponSound1(readPascalString(in));
			chr.setWeaponSound2(readPascalString(in));

		} catch (IOException e) {
			System.err.println("Reading " + chrName);
			e.printStackTrace();
		}
		return chr;
	}
	
	private Obj parseObjData(String objName, byte[] data) {
		Obj obj = new Obj();
		obj.setName(objName);
		obj.setDesign(new Design(data));
		try {
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
			in.skip(in.readShort() - 2); // Skip design.
			obj.setDesignBounds(readRectangle(in));
			short namePlural = in.readShort();
			if (namePlural == 256)
				obj.setNamePlural(true); // TODO: other flags?
			else if (namePlural != 0)
				System.err.println(objName + " had weird namePlural set!");
			if (in.readShort() != 0)
				System.err.println(objName + " had short set!");
			if (in.readByte() != 0)
				System.err.println(objName + " had byte set!");
			obj.setAccuracy(readUnsignedByte(in));
			obj.setValue(readUnsignedByte(in));
			obj.setType(in.readByte());
			obj.setDamage(readUnsignedByte(in));
			obj.setAttackType(in.readByte());
			obj.setNumberOfUses(in.readShort());
			short returnTo = in.readShort();
			if (returnTo == 256) // TODO any other possibilities?
				obj.setReturnToRandomScene(true);
			else if (returnTo != 0)
				System.err.println(objName + " had weird returnTo set!");
			obj.setSceneOrOwner(readPascalString(in));
			obj.setClickMessage(readPascalString(in));
			obj.setOperativeVerb(readPascalString(in));
			obj.setFailureMessage(readPascalString(in));
			obj.setUseMessage(readPascalString(in));
			obj.setSound(readPascalString(in));
		} catch (IOException e) {
			System.err.println("Reading " + objName);
			e.printStackTrace();
		}
		return obj;
	}

	private Scene parseSceneData(String sceneName, byte[] data) {
		Scene scene = new Scene();
		scene.setName(sceneName);
		scene.setDesign(new Design(data));
		try {
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
			in.skip(in.readShort() - 2); // Skip design.
			scene.setDesignBounds(readRectangle(in));
			scene.setWorldY(in.readShort());
			scene.setWorldX(in.readShort());
			scene.setDirBlocked(Scene.NORTH, (in.readByte() != 0));
			scene.setDirBlocked(Scene.SOUTH, (in.readByte() != 0));
			scene.setDirBlocked(Scene.EAST, (in.readByte() != 0));
			scene.setDirBlocked(Scene.WEST, (in.readByte() != 0));
			scene.setSoundFrequency(in.readShort());
			scene.setSoundType(in.readByte());
			in.readByte(); // unknown
			scene.setDirMessage(Scene.NORTH, readPascalString(in));
			scene.setDirMessage(Scene.SOUTH, readPascalString(in));
			scene.setDirMessage(Scene.EAST, readPascalString(in));
			scene.setDirMessage(Scene.WEST, readPascalString(in));
			scene.setSoundName(readPascalString(in));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return scene;
	}

	private static int readUnsignedByte(DataInputStream in) throws IOException {
		int value = in.readByte();
		return (value < 0 ? 256 + value : value);
	}
	
	private static Rectangle readRectangle(DataInputStream in) throws IOException {
		Rectangle bounds = new Rectangle();
		bounds.y = in.readShort();
		bounds.x = in.readShort();
		bounds.height = in.readShort() - bounds.y + 4;
		bounds.width = in.readShort() - bounds.x + 4;
		return bounds;
	}
	
	private static String readPascalString(DataInputStream in) throws IOException {
		if (in.available() == 0) return "";
		int length = in.readByte();
		if (length < 0)
			length += 256;
		byte[] data = new byte[length];
		in.read(data);
		for (int i = 0; i < data.length; i++)
			if (data[i] == (byte) 0x0D)
				data[i] = '\n';
		return new String(data, "MacRoman");
	}
}
