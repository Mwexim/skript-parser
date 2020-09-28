package io.github.syst3ms.skriptparser.util;

/**
 * Utility functions for numbers that don't have to do with math.
 */
public class NumberUtils {

	/**
	 * Parse a string as an integer.
	 * Note that the parsed string is expected to be a parsable integer.
	 * Therefore it will only take care of overflow situations.
	 * @param str the string to parse
	 * @return the parsed integer
	 */
	public static int parseInt(String str) {
		assert str.matches("-?\\d+");
		try {
			return Integer.parseInt(str);
		} catch (final NumberFormatException e) {
			return str.startsWith("-") ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		}
	}

	/**
	 * Parse a string as a long.
	 * Note that the parsed string is expected to be a parsable long.
	 * Therefore it will only take care of overflow situations.
	 * @param str the string to parse
	 * @return the parsed integer
	 */
	public static long parseLong(String str) {
		assert str.matches("-?\\d+");
		try {
			return Long.parseLong(str);
		} catch (final NumberFormatException e) {
			return str.startsWith("-") ? Long.MIN_VALUE : Long.MAX_VALUE;
		}
	}
}
