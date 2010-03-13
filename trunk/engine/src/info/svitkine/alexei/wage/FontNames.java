package info.svitkine.alexei.wage;

public class FontNames {
	private static final String[] FONTS = {
			"Chicago",	// system font
			"Geneva",	// application font
			"New York",
			"Geneva",

			"Monaco",
			"Venice",
			"London",
			"Athens",
	
			"San Francisco",
			"Toronto",
			"Cairo",
			"Los Angeles", // 12

			null, null, null, null, null, null, null, // not in Inside Macintosh

			"Times", // 20
			"Helvetica",
			"Courier",
			"Symbol",
			"Taliesin" // mobile?
		};

	/*
	mappings found on some forums:
	systemFont(0):System(Swiss)
	times(20):Times New Roman(Roman)
	helvetica(21):Arial(Modern)
	courier(22):Courier New(Modern)
	symbol(23):Symbol(Decorative)
	applFont(1):Arial(Swiss)
	newYork(2):Times New Roman(Roman)
	geneva(3):Arial(Swiss)
	monaco(4):Courier New(Modern)
	venice(5):Times New Roman(Roman)
	london(6):Times New Roman(Roman)
	athens(7):Times New Roman(Roman)
	sanFran(8):Times New Roman(Roman)
	toronto(9):Times New Roman(Roman)
	cairo(11):Wingdings(Decorative)
	losAngeles(12):Times New Roman(Roman)
	taliesin(24):Wingdings(Decorative)
	*/

	public static String getFontName(int fontType) {
		if (fontType >= 0 && fontType < FONTS.length && FONTS[fontType] != null) {
			return FONTS[fontType];
		}
		return "Unknown";
	}
}
