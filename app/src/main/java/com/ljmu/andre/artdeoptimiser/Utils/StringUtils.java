package com.ljmu.andre.artdeoptimiser.Utils;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class StringUtils {
	public static String nullSafe(String input) {
		return nullSafe(input, "Null");
	}

	public static String nullSafe(String input, String defaultOutput) {
		return input != null ? input : defaultOutput;
	}
}
