package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.DoubleOptional;

import java.math.BigInteger;

/**
 * Extracts a part of the string.
 * Note that indices in Skript start at 1.
 *
 * @name Substring
 * @pattern [the] (part|sub[ ](text|string)) of %string% (between|from) [(ind(ex[es]|ices)|char[acter][s])] %integer% (and|to) [(index|char[acter])] %integer%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprSubstring implements Expression<String> {
	static {
		Parser.getMainRegistration().addExpression(
			ExprSubstring.class,
			String.class,
			true,
			"[the] (part|sub[ ](text|string)) of %string% (between|from) [(ind(ex[es]|ices)|char[acter][s])] %integer% (and|to) [(index|char[acter])] %integer%",
				"[the] (0:first|1:last) [%integer%] char[acter][s] (of|in) %string%",
				"[the] %integer% (0:first|1:last) char[acter]s (of|in) %string%"
		);
	}

	private Expression<String> value;
	private Expression<BigInteger> lower;
	private Expression<BigInteger> upper;
	private int pattern;
	private boolean first;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		pattern = matchedPattern;
		switch (pattern) {
			case 0:
				value = (Expression<String>) expressions[0];
				lower = (Expression<BigInteger>) expressions[1];
				upper = (Expression<BigInteger>) expressions[2];
				break;
			case 1:
				if (expressions.length == 2) {
					lower = (Expression<BigInteger>) expressions[0];
					value = (Expression<String>) expressions[1];
				} else {
					value = (Expression<String>) expressions[0];
				}
				first = parseContext.getNumericMark() == 0;
				break;
			case 2:
				lower = (Expression<BigInteger>) expressions[0];
				value = (Expression<String>) expressions[1];
				first = parseContext.getNumericMark() == 0;
				break;
			default:
				throw new IllegalStateException();
		}
		return true;
	}

	@Override
	public String[] getValues(TriggerContext ctx) {
		if (pattern == 0) {
			return value.getSingle(ctx)
					.flatMap(val -> DoubleOptional.ofOptional(lower.getSingle(ctx), upper.getSingle(ctx))
							.filter((low, up) -> low.compareTo(up) < 0
									&& low.compareTo(BigInteger.ZERO) > 0
									&& up.compareTo(BigInteger.valueOf(val.length())) <= 0
							)
							.mapToOptional((low, up) -> val.substring(low.intValue() - 1, up.intValue())))
							.map(val -> new String[] {val})
					.orElse(new String[0]);
		} else if (lower != null) {
			return DoubleOptional.ofOptional(value.getSingle(ctx), lower.getSingle(ctx))
					.filter((val, n) -> n.compareTo(BigInteger.ZERO) > 0 && n.compareTo(BigInteger.valueOf(val.length())) <= 0)
					.mapToOptional((val, n) -> first ? val.substring(0, n.intValue()) : val.substring(val.length() - n.intValue()))
					.map(val -> new String[] {val})
					.orElse(new String[0]);
		} else {
			return value.getSingle(ctx)
					.map(val -> new String[] {String.valueOf(val.charAt(first ? 0 : val.length() - 1))})
					.orElse(new String[0]);
		}
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		if (pattern == 0) {
			return "substring of " + value.toString(ctx, debug)
					+ " from " + lower.toString(ctx, debug) + " to " + upper.toString(ctx, debug);
		} else {
			return (first ? "first " : "last ") + (lower != null ? lower.toString(ctx, debug) : "")
					+ " characters of " + value.toString(ctx, debug);
		}
	}
}
