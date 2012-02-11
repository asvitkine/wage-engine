package com.googlecode.wage_engine;

import java.util.HashMap;
import java.util.Map;

public class RandomHat<T> {
	private HashMap<T, Integer> tokens;

	public RandomHat() {
		tokens = new HashMap<T, Integer>();
	}

	public void addTokens(T type, int count) {
		if (count < 0)
			throw new IllegalArgumentException();
		Integer value = tokens.get(type);
		if (value == null) {
			value = count;
		} else {
			value += count;
		}
		tokens.put(type, count);
	}
	
	private int countTokens() {
		int total = 0;
		for (Integer count : tokens.values()) {
			total += count;
		}
		return total;
	}

	public boolean hasTokens() {
		return countTokens() > 0;
	}
	
	public T drawToken() {
		T drawn = null;
		int total = countTokens();
		if (total > 0) {
			int random = (int) (Math.random() * total);
			int count = 0;
			for (Map.Entry<T, Integer> entry: tokens.entrySet()) {
				int value = entry.getValue();
				if (random >= count && random < count + value) {
					T type = entry.getKey();
					tokens.put(type, value - 1);
					return type;
				}
				count += value;
			}
		}
		return drawn;
	}
}
