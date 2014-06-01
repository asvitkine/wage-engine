package com.googlecode.wage_engine.engine;

import java.io.ByteArrayOutputStream;

public class ScriptTextConverter {
	private static final int BLOCK_START = 1;
	private static final int BLOCK_END = 2;
	private static final int STATEMENT = 3;
	private static final int OPERATOR = 4;

	private static String[] mapping = new String[256];
	private static int[] types = new int[256];
	static {
		mapping[0x80] = "IF{";
		types[0x80] = STATEMENT;
		mapping[0x81] = "=";
		types[0x81] = OPERATOR;
		mapping[0x82] = "<";
		types[0x82] = OPERATOR;
		mapping[0x83] = ">";
		types[0x83] = OPERATOR;
		mapping[0x84] = "}AND{";
		mapping[0x85] = "}OR{";
		mapping[0x87] = "EXIT\n";
		types[0x87] = BLOCK_END;
		mapping[0x88] = "END\n";
		types[0x88] = BLOCK_END;
		mapping[0x89] = "MOVE{";
		types[0x89] = STATEMENT;
		mapping[0x8A] = "}TO{";
		mapping[0x8B] = "PRINT{";
		types[0x8B] = STATEMENT;
		mapping[0x8C] = "SOUND{";
		types[0x8C] = STATEMENT;
		mapping[0x8E] = "LET{";
		types[0x8E] = STATEMENT;
		mapping[0x8F] = "+";
		types[0x8F] = OPERATOR;
		mapping[0x90] = "-";
		types[0x90] = OPERATOR;
		mapping[0x91] = "*";
		types[0x91] = OPERATOR;
		mapping[0x92] = "/";
		types[0x92] = OPERATOR;
		mapping[0x93] = "==";
		types[0x93] = OPERATOR;
		mapping[0x94] = ">>";
		types[0x94] = OPERATOR;
		mapping[0x95] = "MENU{";
		types[0x95] = STATEMENT;
		mapping[0xA0] = "TEXT$";
		mapping[0xA1] = "CLICK$";
		// The number of scenes the player has visited, including repeated visits.
		mapping[0xB0] = "VISITS#";
		// RANDOM# for Star Trek, but VISITS# for some other games?
		mapping[0xB1] = "RANDOM#";
		// The number of commands the player has given in the current scene.
		mapping[0xB2] = "LOOP#";
		// The number of characters killed.
		mapping[0xB3] = "VICTORY#";
		// ????
		mapping[0xB4] = "BADCOPY#";
		// A random number between 1 and 100.
		mapping[0xB5] = "RANDOM#";
		mapping[0xC0] = "STORAGE@";
		mapping[0xC1] = "SCENE@";
		mapping[0xC2] = "PLAYER@";
		mapping[0xC3] = "MONSTER@";
		mapping[0xC4] = "RANDOMSCN@";
		mapping[0xC5] = "RANDOMCHR@";
		mapping[0xC6] = "RANDOMOBJ@";
		mapping[0xD0] = "PHYS.STR.BAS#";
		mapping[0xD1] = "PHYS.HIT.BAS#";
		mapping[0xD2] = "PHYS.ARM.BAS#";
		mapping[0xD3] = "PHYS.ACC.BAS#";
		mapping[0xD4] = "SPIR.STR.BAS#";
		mapping[0xD5] = "SPIR.HIT.BAS#";
		mapping[0xD6] = "SPIR.ARM.BAS#";
		mapping[0xD7] = "SPIR.ACC.BAS#";
		mapping[0xD8] = "PHYS.SPE.BAS#";
		mapping[0xE0] = "PHYS.STR.CUR#";
		mapping[0xE1] = "PHYS.HIT.CUR#";
		mapping[0xE2] = "PHYS.ARM.CUR#";
		mapping[0xE3] = "PHYS.ACC.CUR#";
		mapping[0xE4] = "SPIR.STR.CUR#";
		mapping[0xE5] = "SPIR.HIT.CUR#";
		mapping[0xE6] = "SPIR.ARM.CUR#";
		mapping[0xE7] = "SPIR.ACC.CUR#";
		mapping[0xE8] = "PHYS.SPE.CUR#";
		mapping[0xFD] = "}\n";
		mapping[0xFE] = "}THEN\n";
		types[0xFE] = BLOCK_START;
	}

	private static void indent(StringBuilder sb, int indentLevel) {
		for (int i = 0; i < indentLevel; i++)
			sb.append(' ');
	}

	public static String convertScriptText(byte[] data, int offset, int length) {
		StringBuilder sb = new StringBuilder();
		int indentLevel = 0;
		for (int i = offset; i - offset < length && i < data.length; i++) {
			int index = data[i] & 0xFF;
			String keyword = mapping[index];
			if (keyword != null) {
				int type = types[index];
				if (type == STATEMENT) {
					indent(sb, indentLevel);
				} else if (type == BLOCK_START) {
					indentLevel += 2;
				} else if (type == BLOCK_END) {
					indentLevel -= 2;
					indent(sb, indentLevel);
				}
				sb.append(keyword);
			} else if (data[i] == (byte) 0xFF) {
				i++;
				int value = data[i];
				if (value < 0) value += 256;
				value -= 1;
				sb.append((char) ('A' + (value / 9)));
				sb.append((value % 9) + 1);
				sb.append("#");
			} else if (Character.isDefined(data[i])) {
				do {
					sb.append((char) data[i++]);
				} while (Character.isDefined(data[i]));
				i--;
			} else {
				System.err.printf("What is!! %x at %s\n", data[i], sb.toString());
			}
		}
		return sb.toString();
	}

	private static int findKeyword(String scriptText) {
		// Script may start with multiple keyword (e.g. == and =), so search
		// from the end, since == and >> are later in the array than = and >.
		for (int i = mapping.length - 1; i >= 0; i--) {
			String keyword = mapping[i];
			if (keyword != null && scriptText.startsWith(keyword)) {
				return i == 0xb5 ? 0xb1 : i;
			}
		}
		return -1;
	}
	
	private static boolean isBetween(char c, char min, char max) {
		return c >= min && c <= max;
	}

	private static String trimLeft(String s) {
	    return s.replaceAll("^\\s+", "");
	}

	// TODO: This seems to produce the same script text. Verify
	//       that the actual bytes are identical too.
	public static byte[] parseScript(String scriptText) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		scriptText = trimLeft(scriptText);
		int lastIndex = 0;
		while (!scriptText.isEmpty()) {
			int index = findKeyword(scriptText);
			if (index != -1) {
				lastIndex = index;
				String keyword = mapping[index];
				out.write(index);
				scriptText = scriptText.substring(keyword.length());
				if (keyword.endsWith("\n"))
					scriptText = trimLeft(scriptText);
			} else if (scriptText.length() >= 3 && scriptText.charAt(2) == '#' &&
				isBetween(scriptText.charAt(0), 'A', 'Z') &&
				isBetween(scriptText.charAt(1), '0', '9')) {
				out.write(0xFF);
				int letter = scriptText.charAt(0) - 'A';
				int digit = scriptText.charAt(1) - '0';
				out.write(letter * 9 + digit);
				scriptText = scriptText.substring(3);
			} else if (lastIndex != 0x80 &&
					(types[lastIndex] == STATEMENT || types[lastIndex] == OPERATOR)) {
				// Consume the whole string until } for all non-IF statements,
				// without looking for keywords inside it.
				// TODO: This doesn't still work correctly for things like:
				//   IF{EZ-SNAP=PLAYER@}THEN
				// ... since the '-' in EZ-SNAP will be parsed as an operator.
				int i;
				for (i = 0; scriptText.charAt(i) != '}'; i++) {
					out.write(scriptText.charAt(i));
				}
				scriptText = scriptText.substring(i);
			} else {
				out.write(scriptText.charAt(0));
				scriptText = scriptText.substring(1);
			}
		}
		return out.toByteArray();
	}
}
