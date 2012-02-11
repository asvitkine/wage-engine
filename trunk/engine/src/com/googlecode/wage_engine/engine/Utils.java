package com.googlecode.wage_engine.engine;

import java.lang.reflect.Method;

public abstract class Utils {

	public static int fourCharsToInt(String str) {
		return (str.charAt(0) << 24) + (str.charAt(1) << 16) + (str.charAt(2) << 8) + str.charAt(3);
	}
	
	public static String intToFourChars(int value) {
		char[] chars = {
			(char) (0xFF & (value >> 24)),
			(char) (0xFF & (value >> 16)),
			(char) (0xFF & (value >> 8)),
			(char) (0xFF & value)
		};
		return new String(chars);
	}
	
	public static void setFileTypeAndCreator(String path, String type, String creator) {
		int typeInt = fourCharsToInt(type);
		int creatorInt = fourCharsToInt(creator);
		try {
			Class<?> fileManagerClass = Class.forName("com.apple.eio.FileManager");
			Class<?>[] argTypes = new Class[] { String.class, int.class };
			Method setFileTypeMethod = fileManagerClass.getDeclaredMethod("setFileType", argTypes);
			Method setFileCreatorMethod = fileManagerClass.getDeclaredMethod("setFileCreator", argTypes);
			setFileTypeMethod.invoke(null, new Object[] { path, new Integer(typeInt) });
			setFileCreatorMethod.invoke(null, new Object[] { path, new Integer(creatorInt) });
		} catch (Throwable e) {
		}
	}
	
	public static String[] getFileTypeAndCreator(String path) {
		// TODO: What about MacBinary files?
		try {
			Class<?> fileManagerClass = Class.forName("com.apple.eio.FileManager");
			Class<?>[] argTypes = new Class[] { String.class };
			Method getFileTypeMethod = fileManagerClass.getDeclaredMethod("getFileType", argTypes);
			Method getFileCreatorMethod = fileManagerClass.getDeclaredMethod("getFileCreator", argTypes);
			Integer type = (Integer) getFileTypeMethod.invoke(null, new Object[] { path });
			Integer creator = (Integer) getFileCreatorMethod.invoke(null, new Object[] { path });
			return new String[] { intToFourChars(type), intToFourChars(creator) };
		} catch (Throwable e) {
		}
		return null;
	}
}
