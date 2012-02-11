package com.googlecode.wage_engine;

import java.util.Timer;
import java.util.TimerTask;

public class SoundManager {
	private Timer soundTimer;
	private World world;

	public SoundManager(World world) {
		this.world = world;
	}
	
	private class PlaySoundTask extends TimerTask {
		private Scene scene;
		private Sound sound;

		public PlaySoundTask(Scene scene, Sound sound) {
			this.scene = scene;
			this.sound = sound;
		}

		public void run() {
			if (world.getPlayerScene() == scene) {
				sound.play();
			}
		}
	}

	private class UpdateSoundTimerTask extends TimerTask {
		private Scene scene;

		public UpdateSoundTimerTask(Scene scene) {
			this.scene = scene;
		}

		public void run() {
			updateSoundTimerForScene(scene, false);
		}
	}

	public void updateSoundTimerForScene(Scene scene, boolean firstTime) {
		if (soundTimer != null) {
			soundTimer.cancel();
			soundTimer = null;
		}
		if (world.getPlayerScene() != scene)
			return;
		if (scene.getSoundFrequency() > 0 && scene.getSoundName() != null && scene.getSoundName().length() > 0) {
			final Sound sound = world.getSounds().get(scene.getSoundName().toLowerCase());
			if (sound != null) {
				soundTimer = new Timer();
				switch (scene.getSoundType()) {
					case Scene.PERIODIC:
						if (firstTime)
							soundTimer.schedule(new PlaySoundTask(scene, sound), 0);
						int delay = 60000 / scene.getSoundFrequency();
						soundTimer.schedule(new PlaySoundTask(scene, sound), delay);
						soundTimer.schedule(new UpdateSoundTimerTask(scene), delay + 1);
						break;
					case Scene.RANDOM:
						for (int i = 0; i < scene.getSoundFrequency(); i++)
							soundTimer.schedule(new PlaySoundTask(scene, sound), (int) (Math.random() * 60000));
						soundTimer.schedule(new UpdateSoundTimerTask(scene), 60000);
						break;
				}
			}
		}
	}

}
