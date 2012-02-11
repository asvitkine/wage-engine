package com.googlecode.wage_engine.engine;

/*
 * Based on code from Apache Sanselan project. Original code can be found at:
 *
 * https://svn.apache.org/repos/asf/commons/proper/sanselan/trunk/src/main/java/org/apache/sanselan/common/PackBits.java
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PackBits {
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
