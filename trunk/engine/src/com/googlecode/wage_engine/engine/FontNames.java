package com.googlecode.wage_engine.engine;

public class FontNames {
	// Source: Apple IIGS Technical Note #41, "Font Family Numbers"
	// http://apple2.boldt.ca/?page=til/tn.iigs.041
	private static final String[] FONTS = {
		"Chicago", // system font
		"Geneva",  // application font
		"New York",
		"Geneva",
		"Monaco",
		"Venice",
		"London",
		"Athens",
		"San Francisco",
		"Toronto",
		"Cairo",
		"Los Angeles",
		"Zapf Dingbats",
		"Bookman",
		"Helvetica Narrow",
		"Palatino",
		"Zapf Chancery",
		"Times",
		"Helvetica",
		"Courier",
		"Symbol",
		"Taliesin"
	};
	
	public static String getFontName(int fontType) {
		if (fontType >= 0 && fontType < FONTS.length && FONTS[fontType] != null) {
			return FONTS[fontType];
		}
		return "Unknown";
	}
}
