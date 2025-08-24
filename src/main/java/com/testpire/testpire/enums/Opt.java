package com.testpire.testpire.enums;

public enum Opt {
	OPT1, OPT2, OPT3, OPT4, DEF;

	public static Opt identify(String in) {
		if (in.contains("1"))
			return OPT1;
		else if (in.contains("2"))
			return OPT2;
		else if (in.contains("3"))
			return OPT3;
		else if (in.contains("4"))
			return OPT4;
		else
			return DEF;
	}

}
