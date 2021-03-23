package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * Convert a number to a different base (like binary, octal or hexadecimal) or convert a string to its decimal form.
 * @name Number Convert Base
 * @type EXPRESSION
 * @pattern %integers% converted [in]to (binary|octal|hex[adecimal]|base64|base %integer%)
 * @pattern %strings% converted [in]to (binary|octal|hex[adecimal]|base64|base %integer%|5:decimal)
 * @since ALPHA
 * @author Mwexim, WeeskyBDW
 */
public class ExprNumberConvertBase implements Expression<String> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprNumberConvertBase.class,
				String.class,
				true,
				"%integers% converted [in]to (0:binary|1:octal|2:hex[adecimal]|3:base[ ]64|4:base %integer%)",
				"%strings% converted [in]to (0:binary|1:octal|2:hex[adecimal]|3:base[ ]64|4:base %integer%|5:decimal)"
		);
	}

	private Expression<?> expression;
	@Nullable
	private Expression<BigInteger> base;
	private int parseMark;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		parseMark = parseContext.getParseMark();

		expression = expressions[0];
		if (parseMark == 4) {
			base = (Expression<BigInteger>) expressions[1];
		}
		return true;
	}

	@Override
	public String[] getValues(TriggerContext ctx) {
		int radix = 10; // Let's default it to 10 for convenience
		switch (parseMark) {
			case 0:
				radix = 2;
				break;
			case 1:
				radix = 8;
				break;
			case 2:
				radix = 16;
				break;
			case 4:
				assert base != null;
				radix = base.getSingle(ctx).map(BigInteger::intValue).orElse(10);
				break;
			case 3:
			case 5:
				break; // Handled separately
			default:
				throw new IllegalStateException();
		}
		if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
			return new String[0];

		int finalRadix = radix;
		return Arrays.stream(expression.getValues(ctx))
				.map(val -> {
					if (val instanceof BigInteger) {
						return parseMark == 3
								? Base64.getEncoder().encodeToString(((BigInteger) val).toByteArray())
								: ((BigInteger) val).toString(finalRadix);
					} else {
						assert val instanceof String;

						var originalRadix = radixFromString((String) val);
						if (originalRadix.getFirst() == -1)
							return null;

						var converted = BigInteger.valueOf(
								parseMark == 3
								? bytesToLong(Base64.getDecoder().decode((String) val))
								: Long.parseLong(
										((String) val).substring(originalRadix.getSecond() + 1),
										originalRadix.getFirst()
								)
						);
						return converted.toString(finalRadix);
					}
				})
				.filter(Objects::nonNull)
				.toArray(String[]::new);
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		switch (parseMark) {
			case 0:
				return expression.toString(ctx, debug) + " converted to binary";
			case 1:
				return expression.toString(ctx, debug) + " converted to octal";
			case 2:
				return expression.toString(ctx, debug) + " converted to hexadecimal";
			case 3:
				return expression.toString(ctx, debug) + " converted to base 64";
			case 4:
				assert base != null;
				return expression.toString(ctx, debug) + " converted to base " + base.toString(ctx, debug);
			case 5:
				return expression.toString(ctx, debug) + " converted to decimal";
			default:
				throw new IllegalStateException();
		}
	}

	/**
	 * Get the radix of a given string that represents a number. Prefixes are accounted for.
	 * If no prefixes were found, base 10 (decimal) is chosen as primary base, unless this fails too.
	 * @param str the string
	 * @return the radix, -1 if the string does not represent a number and the last index of the string that should not be parsed
	 */
	private static Pair<Integer, Integer> radixFromString(String str) {
		str = str.toLowerCase();

		// First check prefixes
		if (str.startsWith("0b") || str.startsWith("b")) {
			return new Pair<>(2, str.indexOf('b'));
		} else if (str.startsWith("0o") || str.startsWith("o")) {
			return new Pair<>(8, str.indexOf('o'));
		} else if (str.startsWith("0x") || str.startsWith("x")) {
			return new Pair<>(16, str.indexOf('x'));
		}

		// Then check for base 10
		try {
			Integer.parseInt(str, 10);
			return new Pair<>(10, -1);
		} catch (NumberFormatException ignored) { /* Nothing */ }

		// Then check radix from lowest to highest.
		for (int i = Character.MIN_RADIX; i <= Character.MAX_RADIX; i++) {
			if (i == 10)
				continue;

			try {
				Integer.parseInt(str, i);
				return new Pair<>(i, -1);
			} catch (NumberFormatException ignored) { /* Nothing */ }
		}
		return new Pair<>(-1, -1);
	}

	private static long bytesToLong(byte[] bytes) {
		long result = 0;
		for (int i = 0; i < 8; i++) {
			result <<= 8;
			result |= (bytes[i] & 0xFF);
		}
		return result;
	}
}
