package info.svitkine.alexei.wage;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;

import org.freeshell.gbsmith.rescafe.resourcemanager.*;
import org.freeshell.gbsmith.rescafe.MacBinaryHeader;

//import com.fizzysoft.sdu.RecentDocumentsManager;


public class WorldLoader {
	private static WorldLoader instance;

	//private RecentDocumentsManager rdm;
	
	protected WorldLoader() {
		/*
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
		*/
	}

	public static WorldLoader getInstance() {
		if (instance == null)
			instance = new WorldLoader();
		return instance;
	}
	
	/*
	public RecentDocumentsManager getRecentDocumentsManager() {
		return rdm;
	}*/

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

	private String loadStringFromDITL(ResourceModel model, int resourceId, int itemIndex) {
		Resource ditl = model.getResource("DITL", (short) resourceId);
		if (ditl != null) {
			byte[] data = ditl.getData();
			try {
				DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));			
				int itemCount = in.readShort();
				for (int i = 0; i <= itemCount; i++) {
					// int placeholder; short rect[4]; byte flags; pstring str;
					in.skip(13);
					String message = readPascalString(in);
					if (i == itemIndex) {
						return message;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public World loadWorld(ResourceModel model, File file) throws UnsupportedEncodingException {
		//rdm.addDocument(file, null);

		World world = new World(new Script(model.getResource("GCOD", (short) 0).getData()));
		State initialState = new State();

		String[] typeAndCreator = Utils.getFileTypeAndCreator(file.getAbsolutePath());
		if (typeAndCreator != null) {
			world.setCreatorCode(typeAndCreator[1]);
		}

		world.setName(file.getName());
		ResourceType vers = model.getResourceType("VERS");
		if (vers != null) {
			Resource r = vers.getResArray()[0];
			byte[] data = r.getData();
			try {
				DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
				
				int signature = in.readInt();
				world.setSignature(signature);	// unique world ID
				initialState.setWorldSig(signature);
				
				in.skip(6);
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
		
		String message;
		if ((message = loadStringFromDITL(model, 2910, 1)) != null) {
			world.setGameOverMessage(message.trim());
		}
		if ((message = loadStringFromDITL(model, 2480, 3)) != null) {
			world.setSaveBeforeQuitMessage(message.trim());
		}
		if ((message = loadStringFromDITL(model, 2490, 3)) != null) {
			world.setSaveBeforeCloseMessage(message.trim());
		}
		if ((message = loadStringFromDITL(model, 2940, 2)) != null) {
			world.setRevertMessage(message);
		}
		
		ResourceType scenes = model.getResourceType("ASCN");
		short sceneCount = 0;
		
		if (scenes != null) {
			for (Resource r : scenes.getResArray()) {
				SceneImpl scene = parseSceneData(r.getName(), r.getData());
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
					scene.setText(MacRoman.toString(data, 12, data.length-12));
				}
				
				scene.setResourceID(r.getID());
				world.addScene(scene);
				sceneCount++;
			}
			
			// store global info in state object for use with save/load actions
			initialState.setNumScenes(sceneCount);								// total scene count
			int charHex = (State.SCENES_INDEX+(sceneCount*State.SCENE_SIZE));	// hex offset to start of char data in save file
			initialState.setChrsHexOffset((short)charHex);
		}
		ResourceType chrs = model.getResourceType("ACHR");
		short charCount = 0;
		if (chrs != null) {
			for (Resource r : chrs.getResArray()) {
				ChrImpl chr = parseChrData(r.getName(), r.getData());
				chr.setResourceID(r.getID());
				world.addChr(chr);
				// TODO: What if there's more than one player character?
				if (chr.isPlayerCharacter()) {
					world.setPlayer(chr);
					
					// hex offset to player character in save file
					initialState.setPlayerHexOffset(initialState.getHexOffsetForChr(chr));
				}
				charCount++;
			}
			
			// store global info in state object for use with save/load actions
			initialState.setNumChars(charCount);											// total char count
			int objHex = (initialState.getChrsHexOffset()+(charCount*State.CHR_SIZE));	// hex offset to start of obj date in save file
			initialState.setObjsHexOffset((short)objHex);
		}
		ResourceType objs = model.getResourceType("AOBJ");
		short objCount = 0;

		if (objs != null) {
			for (Resource r : objs.getResArray()) {
				ObjImpl obj = parseObjData(r.getName(), r.getData());
				obj.setResourceID(r.getID());

				world.addObj(obj);
				objCount++;
			}
			
			// store global info in state object for use with save/load actions
			initialState.setNumObjs(objCount);		// total obj count
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
		} else {
			/* Enchanted Scepters did not use the PAT# resource for the textures. */
			ResourceType code = model.getResourceType("CODE");
			if (code != null) {
				Resource r = code.getResource((short) 1);
				if (r != null) {
					try {
						DataInputStream in = new DataInputStream(new ByteArrayInputStream(r.getData()));
						in.skip(0x55ac);
						for (int i = 0; i < 29; i++) {
							byte[] pattern = new byte[8];
							in.readFully(pattern);
							world.getPatterns().add(pattern);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		ResourceType menus = model.getResourceType("MENU");
		if (menus != null) {
			String[] appleMenu = readMenu(menus, 2001);
			if (appleMenu != null) {
				String aboutMenuItemName = appleMenu[1].split(";")[0];
				world.setAboutMenuItemName(aboutMenuItemName);
			}
			String[] commandsMenu = readMenu(menus, 2004);
			if (commandsMenu != null) {
				world.setCommandsMenuName(commandsMenu[0]);
				world.setDefaultCommandsMenu(commandsMenu[1]);
			}
			String[] weaponsMenu = readMenu(menus, 2005);
			if (weaponsMenu != null) {
				world.setWeaponsMenuName(weaponsMenu[0]);
			}
			// Read Apple menu and get the name of that menu item..
		}

		// store global info in state object for use with save/load actions
		world.setCurrentState(initialState);	// pass off the state object to the world
		
		return world;
	}
	
	private String[] readMenu(ResourceType menus, int resourceId) {
		Resource r = menus.getResource((short) resourceId);
		if (r != null) {
			try {
				// open related mctb for custom colors!
				DataInputStream in = new DataInputStream(new ByteArrayInputStream(r.getData()));
				return readMenu(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private String[] readMenu(DataInputStream in) throws IOException {
		in.skip(10);
		int enableFlags = in.readInt();
		String menuName = readPascalString(in);
		String menuItem = readPascalString(in);
		int menuItemNumber = 1;
		StringBuilder sb = new StringBuilder();
		byte[] itemData = new byte[4];
		while (menuItem.length() > 0) {
			if (sb.length() > 0) {
				sb.append(';');
			}
			if ((enableFlags & (1 << menuItemNumber)) == 0) {
				sb.append('(');
			}
			sb.append(menuItem);
			in.readFully(itemData);
			char[] styles = new char[] {'B', 'I', 'U', 'O', 'S', 'C', 'E'};
			for (int i = 0; i < styles.length; i++) {
				if ((itemData[3] & (1 << i)) != 0) {
					sb.append('<');
					sb.append(styles[i]);
				}
			}
			if (itemData[1] != 0) {
				sb.append('/');
				sb.append((char)itemData[1]);
			}
			menuItem = readPascalString(in);
			menuItemNumber++;
		}
		return new String[] { menuName, sb.toString() }; 
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
	
	private ChrImpl parseChrData(String chrName, byte[] data) {
		ChrImpl chr = new ChrImpl();
		chr.setName(chrName);
		chr.setDesign(new Design(data));
		try {
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
			in.skip(in.readShort() - 2); // Skip design.
			chr.setDesignBounds(readRectangle(in));
			
			chr.setPhysicalStrength(in.readUnsignedByte());
			chr.setPhysicalHp(in.readUnsignedByte());
			chr.setNaturalArmor(in.readUnsignedByte());
			chr.setPhysicalAccuracy(in.readUnsignedByte());
			
			chr.setSpiritualStrength(in.readUnsignedByte());
			chr.setSpiritialHp(in.readUnsignedByte());
			chr.setResistanceToMagic(in.readUnsignedByte());
			chr.setSpiritualAccuracy(in.readUnsignedByte());
			
			chr.setRunningSpeed(in.readUnsignedByte());
			chr.setRejectsOffers(in.readUnsignedByte());
			chr.setFollowsOpponent(in.readUnsignedByte());

			int b = in.readByte();
			if (b != 0)
				System.err.println("Chr unknown 1: " + b); // unknown
			b = in.readInt();
			if (b != 0)
				System.err.println("Chr unknown 2: " + b); // unknown
			
			chr.setWeaponDamage1(in.readUnsignedByte());
			chr.setWeaponDamage2(in.readUnsignedByte());
			
			b = in.readByte();
			if (b != 0)
				System.err.println("Chr unknown 3: " + b); // unknown

			if (in.readByte() == 1)
				chr.setPlayerCharacter(true);
			chr.setMaximumCarriedObjects(in.readUnsignedByte());
			chr.setReturnTo(in.readByte());

			chr.setWinningWeapons(in.readUnsignedByte());
			chr.setWinningMagic(in.readUnsignedByte());
			chr.setWinningRun(in.readUnsignedByte());
			chr.setWinningOffer(in.readUnsignedByte());
			chr.setLosingWeapons(in.readUnsignedByte());
			chr.setLosingMagic(in.readUnsignedByte());
			chr.setLosingRun(in.readUnsignedByte());
			chr.setLosingOffer(in.readUnsignedByte());
			
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

	private ObjImpl parseObjData(String objName, byte[] data) {
		ObjImpl obj = new ObjImpl();
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
			obj.setAccuracy(in.readUnsignedByte());
			obj.setValue(in.readUnsignedByte());
			obj.setType(in.readByte());
			obj.setDamage(in.readUnsignedByte());
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

	private SceneImpl parseSceneData(String sceneName, byte[] data) {
		SceneImpl scene = new SceneImpl();
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
			scene.setSoundType(in.readUnsignedByte());
			int b = in.readByte();
			if (b != 0)
				System.err.println("Scene unknown: " + b); // unknown
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
		int length = in.readUnsignedByte();
		byte[] data = new byte[length];
		in.read(data);
		for (int i = 0; i < data.length; i++)
			if (data[i] == (byte) 0x0D)
				data[i] = '\n';
		return MacRoman.toString(data);
	}
}
