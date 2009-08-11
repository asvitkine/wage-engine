package info.svitkine.alexei.wage;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;


public class PackBits {
	// https://svn.apache.org/repos/asf/incubator/sanselan/trunk/src/main/java/org/apache/sanselan/common/PackBits.java
	public static byte[] unpack(DataInputStream in, int expected) throws IOException {
		int total = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while (total < expected) {
			int n = in.readByte();
			// If n is between 0 and 127 inclusive, copy the next n+1 bytes literally.
			if ((n >= 0) && (n <= 127)) {
				int count = n + 1;

				total += count;
				for (int i = 0; i < count; i++)
					out.write(in.readByte());
			// Else if n is between -127 and -1 inclusive, copy the next byte -n+1 times.
			} else if ((n >= -127) && (n <= -1)) {
				int b = in.readByte();
				int count = -n + 1;

				total += count;
				for (int i = 0; i < count; i++)
					out.write(b);
			}
			// Else if n is -128, noop.
		}
		return out.toByteArray();
	}
}
