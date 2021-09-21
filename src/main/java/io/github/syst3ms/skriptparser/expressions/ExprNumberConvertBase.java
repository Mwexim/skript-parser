package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.CollectionUtils;

import java.math.BigInteger;
import java.util.Base64;
import java.util.Objects;

/**
 * Convert a number to a different base (like binary, octal or hexadecimal) or convert a string to its decimal form.
 * You can specify a custom base, but it can only range from 2 to 36. Values outside of this range, or invalid string
 * representations will result in an empty list.
 * @name Number Convert Base
 * @type EXPRESSION
 * @pattern %integers% [converted] to (binary|octal|hex[adecimal]|base[ ]64|base %integer%)
 * @pattern (binary|octal|hex[adecimal]|base[ ]64) %strings% [converted] to (binary|octal|hex[adecimal]|base[ ]64|base %integer%|decimal)
 * @since ALPHA
 * @author Mwexim, WeeskyBDW
 */
public class ExprNumberConvertBase implements Expression<String> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprNumberConvertBase.class,
				String.class,
				false,
				"%integers% [converted] to (2:binary|8:octal|16:hex[adecimal]|64:base[ ]64|custom:base %integer%)",
				"(2:binary|8:octal|10:decimal|16:hex[adecimal]|64:base[ ]64) %strings% [converted] to (2:binary|8:octal|10:decimal|16:hex[adecimal]|64:base[ ]64|custom:base %integer%)"
		);
	}

	private Expression<?> expression;
	private Expression<BigInteger> base;
	private int pattern;
	private String baseFrom;
	private String baseTo;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		pattern = matchedPattern;
		if (pattern == 0) {
			baseFrom = "10";
			baseTo = parseContext.getMarks().get(0);
		} else {
			baseFrom = parseContext.getMarks().get(0);
			baseTo = parseContext.getMarks().get(1);
		}

		expression = expressions[0];
		if (baseTo.equals("custom")) {
			base = (Expression<BigInteger>) expressions[1];
		}
		return true;
	}

	@Override
	public String[] getValues(TriggerContext ctx) {
		int radixFrom = Integer.parseInt(baseFrom);
		int radixTo = baseTo.equals("custom")
				? base.getSingle(ctx).map(BigInteger::intValue).orElse(-1)
				: Integer.parseInt(baseTo);
		if ((radixTo < Character.MIN_RADIX || radixTo > Character.MAX_RADIX) && radixTo != 64) {
			return new String[0];
		} else if (radixFrom == radixTo) {
			return expression.stream(ctx).map(Object::toString).toArray(String[]::new);
		}

		String[] convertedValues = expression.stream(ctx)
				.map(val -> {
					if (pattern == 0) {
						// From integer (always decimal)
						assert val instanceof BigInteger;
						return radixTo == 64
								? Base64.getEncoder().encodeToString(((BigInteger) val).toByteArray())
								: ((BigInteger) val).toString(radixTo);
					} else {
						// From string
						assert val instanceof String;
						try {
							var converted = BigInteger.valueOf(radixFrom == 64
									? bytesToLong(Base64.getDecoder().decode((String) val))
									: Long.parseLong((String) val, radixFrom)
							);
							return radixTo == 64
									? Base64.getEncoder().encodeToString(converted.toByteArray())
									: converted.toString(radixTo);
						} catch (IllegalArgumentException ignored) {
							return null;
						}
					}
				})
				.toArray(String[]::new);

		if (CollectionUtils.contains(convertedValues, (String) null)) {
			return new String[0];
		}
		return convertedValues;
	}

	@Override
	public boolean isSingle() {
		return expression.isSingle();
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return baseFrom + " converted to "
				+ (baseTo.equals("custom") ? Objects.requireNonNull(base).toString(ctx, debug) : baseTo);
	}

	private static long bytesToLong(byte[] bytes) {
		long result = 0;
		for (byte b : bytes) {
			result <<= 8;
			result |= (b & 0xFF);
		}
		return result;
	}
}
