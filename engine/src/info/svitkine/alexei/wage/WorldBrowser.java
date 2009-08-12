package info.svitkine.alexei.wage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

public class WorldBrowser extends JPanel {
	private static final long serialVersionUID = 4725978893229240988L;
	private World world;
	private TexturePaint[] patterns;
	private JTabbedPane tabs;
	private JList sceneList;
	private JButton playButton;

	public WorldBrowser(final World world) {
		this.world = world;
		patterns = loadPatterns(world);
		tabs = new JTabbedPane();
		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);
		tabs.add("Scenes", createSceneBrowser());
		tabs.add("Objects", createObjBrowser());
		tabs.add("Characters", createChrBrowser());
		tabs.add("Sounds", createSoundBrowser());
		tabs.add("Global Code", new JScrollPane(new JTextArea(world.getGlobalScript().toString())));
		playButton = new JButton("Play!");
		add(playButton, BorderLayout.SOUTH);
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new GameWindow(world, patterns).setVisible(true);
			}
		});
	}

	private static TexturePaint[] loadPatterns(World world) {
		int numPatterns = world.getPatterns().size();
		TexturePaint[] patterns = new TexturePaint[numPatterns];
		for (int i = 0; i < numPatterns; i++) {
			byte[] pattern = world.getPatterns().get(i);
			BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_BINARY);
			Graphics2D g2d = image.createGraphics();
			for (int y = 0; y < 8; y++) {
				for (int x = 0; x < 8; x++) {
					if ((pattern[y] & (1 << (7 - x))) != 0) {
						g2d.setColor(Color.BLACK);
					} else {
						g2d.setColor(Color.WHITE);
					}
					g2d.drawRect(x, y, 0, 0);
				}
			}
			patterns[i] = new TexturePaint(image, new Rectangle(0, 0, 8, 8));
		}
		return patterns;
	}
	
	public JPanel createSceneBrowser() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		DefaultListModel model = new DefaultListModel();
		for (Scene scene : world.getOrderedScenes())
			model.addElement(scene);
		sceneList = new JList(model);
		sceneList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		GridBagConstraints c = new GridBagConstraints();
		c.insets.right = 10;
		c.gridy = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		c.gridheight = 6;
		c.fill = GridBagConstraints.BOTH;
		panel.add(new JScrollPane(sceneList), c);
		c.insets.right = 0;
		c.gridx = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.anchor = GridBagConstraints.SOUTH;
		JButton openDesignButton = new JButton("View Design");
		openDesignButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Scene scene = (Scene) sceneList.getSelectedValue();
				if (scene != null) {
					createAndShowWindowWithContent(new ObjectViewer(scene.getDesign(), patterns), scene.getDesignBounds());
				}
			}
		});
		JButton openTextButton = new JButton("View Text");
		openTextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Scene scene = (Scene) sceneList.getSelectedValue();
				if (scene != null) {
					JTextArea textArea = new JTextArea(scene.getText());
					textArea.setColumns(0);
					textArea.setLineWrap(true);
					textArea.setWrapStyleWord(true);
					JScrollPane scrollPane = new JScrollPane(textArea);
					scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
					createAndShowWindowWithContent(scrollPane, scene.getTextBounds());
				}
			}
		});
		JButton openScriptButton = new JButton("View Script");
		openScriptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Scene scene = (Scene) sceneList.getSelectedValue();
				if (scene != null) {
					createAndShowWindowWithContent(new JScrollPane(new JTextArea(scene.getScript().toString())), null);
				}
			}
		});
		panel.add(openDesignButton, c);
		panel.add(openTextButton, c);
		panel.add(openScriptButton, c);
		c.weighty = 1.0;
		panel.add(new JLabel(), c);
		return panel;
	}

	public JPanel createObjBrowser() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		DefaultListModel model = new DefaultListModel();
		for (Obj obj : world.getOrderedObjs())
			model.addElement(obj);
		final JList sceneList = new JList(model);
		sceneList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		GridBagConstraints c = new GridBagConstraints();
		c.insets.right = 10;
		c.gridy = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		c.gridheight = 6;
		c.fill = GridBagConstraints.BOTH;
		panel.add(new JScrollPane(sceneList), c);
		c.insets.right = 0;
		c.gridx = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.anchor = GridBagConstraints.SOUTH;
		JButton openDesignButton = new JButton("View Design");
		openDesignButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Obj obj = (Obj) sceneList.getSelectedValue();
				if (obj != null) {
					createAndShowWindowWithContent(new ObjectViewer(obj.getDesign(), patterns), obj.getDesignBounds());
				}
			}
		});
		panel.add(openDesignButton, c);
		JButton openDesignMaskButton = new JButton("View Design Mask");
		openDesignMaskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Obj obj = (Obj) sceneList.getSelectedValue();
				if (obj != null) {
					ObjectViewer viewer = new ObjectViewer(obj.getDesign(), patterns);
					viewer.setMaskMode(true);
					createAndShowWindowWithContent(viewer, obj.getDesignBounds());
				}
			}
		});
		panel.add(openDesignMaskButton, c);
		c.weighty = 1.0;
		panel.add(new JLabel(), c);
		return panel;
	}
	
	public JPanel createChrBrowser() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		DefaultListModel model = new DefaultListModel();
		for (Chr chr : world.getOrderedChrs())
			model.addElement(chr);
		final JList sceneList = new JList(model);
		sceneList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		GridBagConstraints c = new GridBagConstraints();
		c.insets.right = 10;
		c.gridy = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		c.gridheight = 6;
		c.fill = GridBagConstraints.BOTH;
		panel.add(new JScrollPane(sceneList), c);
		c.insets.right = 0;
		c.gridx = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.anchor = GridBagConstraints.SOUTH;
		JButton openDesignButton = new JButton("View Design");
		openDesignButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Chr chr = (Chr) sceneList.getSelectedValue();
				if (chr != null) {
					createAndShowWindowWithContent(new ObjectViewer(chr.getDesign(), patterns), null);
				}
			}
		});
		panel.add(openDesignButton, c);
		JButton openDesignMaskButton = new JButton("View Design Mask");
		openDesignMaskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Chr chr = (Chr) sceneList.getSelectedValue();
				if (chr != null) {
					ObjectViewer viewer = new ObjectViewer(chr.getDesign(), patterns);
					viewer.setMaskMode(true);
					createAndShowWindowWithContent(viewer, chr.getDesignBounds());
				}
			}
		});
		panel.add(openDesignMaskButton, c);
		c.weighty = 1.0;
		panel.add(new JLabel(), c);
		return panel;
	}

	public JPanel createSoundBrowser() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		DefaultListModel model = new DefaultListModel();
		for (Sound sound : world.getOrderedSounds())
			model.addElement(sound);
		final JList soundList = new JList(model);
		soundList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		GridBagConstraints c = new GridBagConstraints();
		c.insets.right = 10;
		c.gridy = GridBagConstraints.RELATIVE;
		c.weightx = 1.0;
		c.gridheight = 6;
		c.fill = GridBagConstraints.BOTH;
		panel.add(new JScrollPane(soundList), c);
		c.insets.right = 0;
		c.gridx = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.anchor = GridBagConstraints.SOUTH;
		JButton playSoundButton = new JButton("Play");
		playSoundButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Sound sound = (Sound) soundList.getSelectedValue();
				if (sound != null) {
					System.out.println("Playing sound " + sound.getName());
					sound.play();
				}
			}
		});
		panel.add(playSoundButton, c);
		c.weighty = 1.0;
		panel.add(new JLabel(), c);
		return panel;
	}

	public static void createAndShowWindowWithContent(JComponent content, Rectangle bounds) {
		JFrame f = new JFrame();
		Utils.setupCloseWindowKeyStrokes(f, f.getRootPane());
		content.setBorder(new WindowBorder());
		f.setContentPane(content);
		f.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		if (bounds != null) {
			content.setPreferredSize(new Dimension(bounds.width, bounds.height));
			f.setLocation(bounds.x, bounds.y);
			f.pack();
		} else {
			f.setSize(444, 333);
			f.setLocationRelativeTo(null);
		}
		f.setVisible(true);
	}
}
