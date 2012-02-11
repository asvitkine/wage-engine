package com.googlecode.wage_engine;

public interface Weapon {
	public String getName();
	public String getOperativeVerb();
	public String getFailureMessage();
	public int getType();
	public int getAccuracy();
	public int getDamage();
	public String getSound();
}
