package com.testpire.testpire.enums;

import org.apache.commons.lang3.StringUtils;

public enum DifficultyLevel {
	EASY, MEDIUM, HARD, ALL;

	public static DifficultyLevel identify(String in) {
		if(StringUtils.isEmpty(in)){
			return ALL;
		}
		return DifficultyLevel.valueOf(in.toUpperCase());
	}

}
