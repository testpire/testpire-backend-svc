package com.testpire.testpire.enums;

public enum DifficultyLevel {
	EASY, MEDIUM, HARD, ALL;

	public static DifficultyLevel identify(String in) {
		return DifficultyLevel.valueOf(in.toUpperCase());
		
	}

}
