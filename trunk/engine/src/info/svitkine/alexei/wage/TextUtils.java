package info.svitkine.alexei.wage;

public class TextUtils {
	public static final int GENDER_HE = 0;
	public static final int GENDER_SHE = 1;
	public static final int GENDER_IT = 2;

	public static String prependGenderSpecificPronoun(String word, int gender) {
		if (gender == GENDER_HE)
			return "his " + word;
		else if (gender == GENDER_SHE)
			return "her " + word;
		else
			return "its " + word;
	}

	public static String getGenderSpecificPronoun(int gender, boolean capitalize) {
		if (gender == GENDER_HE)
			return capitalize ? "He" : "he";
		else if (gender == GENDER_SHE)
			return capitalize ? "She" : "she";
		else
			return capitalize ? "It" : "it";
	}
	
	public static String prependIndefiniteArticle(String word) {
		switch (word.charAt(0)) {
			case 'a': case 'A':
			case 'e': case 'E':
			case 'i': case 'I':
			case 'o': case 'O':
			case 'u': case 'U':
				return "an " + word;
		}
		return "a " + word;
	}
}
