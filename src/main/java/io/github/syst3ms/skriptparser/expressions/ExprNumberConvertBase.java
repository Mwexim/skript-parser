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
import java.util.Objects;
import java.util.Optional;

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
				"%integers% [converted] to :(binary|octal|hex[adecimal]|base64:base[ ]64|custom:base %integer%)",
				":(binary|octal|hex[adecimal]|base64:base[ ]64) %strings% [converted] to :(binary|octal|hex[adecimal]|base64:base[ ]64|custom:base %integer%|decimal)"
		);
	}

	private Expression<?> expression;
	@Nullable
	private Expression<BigInteger> baseTo;
	private int pattern;
	private String typeFrom;
	private String typeTo;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		pattern = matchedPattern;
		if (pattern == 0) {
			typeTo = parseContext.getSingleMark().orElseThrow();
		} else {
			typeFrom = parseContext.getMarks().get(0);
			typeTo = parseContext.getMarks().get(1);
		}

		expression = expressions[0];
		if (typeTo.equals("custom")) {
			baseTo = (Expression<BigInteger>) expressions[1];
		}
		return true;
	}

	@Override
	public String[] getValues(TriggerContext ctx) {
		int radixTo;
		switch (typeTo) {
			case "binary":
				radixTo = 2;
				break;
			case "octal":
				radixTo = 8;
				break;
			case "hex":
				radixTo = 16;
				break;
			case "base64":
				radixTo = 64;
				break;
			case "custom":
				assert baseTo != null;
				radixTo = baseTo.getSingle(ctx).map(BigInteger::intValue).orElse(-1);
				break;
			case "decimal":
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
						switch (typeFrom) {
							case "binary":
								radixFrom = 2;
								break;
							case "octal":
								radixFrom = 8;
								break;
							case "hex":
								radixFrom = 16;
								break;
							case "base64":
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
		if (to.isAssignableFrom(BigInteger.class) && typeTo.equals("decimal")) {
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
		return typeFrom + " converted to "
				+ (typeTo.equals("custom") ? Objects.requireNonNull(baseTo).toString(ctx, debug) : typeTo);
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
