package io.github.syst3ms.skriptparser.util;

import java.math.BigInteger;

/**
 * Utility functions for numbers that don't have to do with math.
 */
public class NumberUtils {

	/**
	 * Checks if a given integer is between two values, both inclusive.
	 * @param i the value to check
	 * @param a the lower bound
	 * @param b the upper bound
	 * @return whether or not the value lays in between or is equal to a and b
	 */
	public static boolean between(int i, int a, int b) {
		return a <= i && i <= b;
	}

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

	/**
	 * Parse a string as a BigInteger.
	 * Note that the parsed string is expected to be a parsable BigInteger.
	 * Therefore it will only take care of overflow situations.
	 * @param str the string to parse
	 * @return the parsed integer
	 */
	public static BigInteger parseBigInteger(String str) {
		return BigInteger.valueOf(parseLong(str));
	}
}
