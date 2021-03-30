package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConvertedExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 * Convert a number to a different base (like binary, octal or hexadecimal) or convert a string to its decimal form.
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
				"%integers% [converted] to (0:binary|1:octal|2:hex[adecimal]|3:base[ ]64|4:base %integer%)",
				"binary %strings% [converted] to (1:octal|2:hex[adecimal]|3:base[ ]64|4:base %integer%|5:decimal)",
				"octal %strings% [converted] to (0:binary|2:hex[adecimal]|3:base[ ]64|4:base %integer%|5:decimal)",
				"hex[adecimal] %strings% [converted] to (0:binary|1:octal|3:base[ ]64|4:base %integer%|5:decimal)",
				"base[ ]64 %strings% [converted] to (0:binary|1:octal|2:hex[adecimal]|4:base %integer%|5:decimal)"
		);
	}

	private Expression<?> expression;
	@Nullable
	private Expression<BigInteger> baseTo;
	private int pattern;
	private int parseMark;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		pattern = matchedPattern;
		parseMark = parseContext.getParseMark();

		expression = expressions[0];
		if (parseMark == 4) {
			baseTo = (Expression<BigInteger>) expressions[1];
		}
		return true;
	}

	@Override
	public String[] getValues(TriggerContext ctx) {
		int radixTo;
		switch (parseMark) {
			case 0:
				radixTo = 2;
				break;
			case 1:
				radixTo = 8;
				break;
			case 2:
				radixTo = 16;
				break;
			case 3:
				radixTo = 64;
				break;
			case 4:
				assert baseTo != null;
				radixTo = baseTo.getSingle(ctx).map(BigInteger::intValue).orElse(-1);
				break;
			case 5:
				radixTo = 10;
				break;
			default:
				throw new IllegalStateException();
		}
		if ((radixTo < Character.MIN_RADIX || radixTo > Character.MAX_RADIX)
			&& radixTo != 64)
			return new String[0];

		String[] convertedValues = Arrays.stream(expression.getValues(ctx))
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

						int radixFrom;
						switch (pattern) {
							case 1:
								radixFrom = 2;
								break;
							case 2:
								radixFrom = 8;
								break;
							case 3:
								radixFrom = 16;
								break;
							case 4:
								radixFrom = 64;
								break;
							default:
								throw new IllegalStateException();
						}
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

	@SuppressWarnings("unchecked")
	@Override
	public <C> Optional<? extends Expression<C>> convertExpression(Class<C> to) {
		if (to.isAssignableFrom(BigInteger.class) && parseMark == 5) {
			// We will only convert to the integer if it is actually a decimal,
			// otherwise things will be messy.
			return Optional.of((Expression<C>) ConvertedExpression.newInstance(
					this,
					BigInteger.class,
					val -> Optional.of(new BigInteger((String) val))
			));
		}
		return Optional.empty();
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
				assert baseTo != null;
				return expression.toString(ctx, debug) + " converted to base " + baseTo.toString(ctx, debug);
			case 5:
				return expression.toString(ctx, debug) + " converted to decimal";
			default:
				throw new IllegalStateException();
		}
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
