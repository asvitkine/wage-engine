package info.svitkine.alexei.wage;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;


public class Sound {
	private String name;
	private byte[] data;

	public Sound(byte[] data) {
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void play() {
		try {
			AudioFormat audioFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_UNSIGNED,  // Encoding technique
					11000.0F,                           // Sample Rate
					8,                                  // Number of bits in each channel
					1,                                  // Number of channels (2=stereo)
					1,                                  // Number of bytes in each frame
					22257.0F,                           // Number of frames per second
					true);                              // Big-endian (true) or little-endian (false)
			SourceDataLine line = AudioSystem.getSourceDataLine(audioFormat);
			line.open();
			line.start();
			byte[] deltas = new byte[] {0,-49,-36,-25,-16,-9,-4,-1,0,1,4,9,16,25,36,49};
			byte[] buf = new byte[2*(data.length - 20)];
			int prevValue = (byte) 128;
			for (int i = 0; i < buf.length; i++) {
				int index = 20 + i/2;
				int value = prevValue;
				if (i % 2 == 0) {
					value += deltas[data[index] & 0xf];
				} else {
					value += deltas[(data[index] >> 4) & 0xf];
				}
				buf[i] = (byte) value;
				prevValue = value;
			}
			line.write(buf, 0, buf.length);
			line.drain();
			line.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return name;
	}
}
