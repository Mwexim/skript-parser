package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.math.BigInteger;

/**
 * The character at a given position in a string. Note that indices in Skript start at 1.
 *
 * @name Character At
 * @pattern [the] char[acter][s] at [(ind(ex[es]|ices)|pos[ition][s])] %integers% (of|in) %string%
 * @since ALPHA
 * @author Olyno
 */
public class ExprStringCharAt implements Expression<String> {
	static {
		Parser.getMainRegistration().addExpression(
			ExprStringCharAt.class,
			String.class,
			false,
			"[the] char[acter][s] at [(ind(ex[es]|ices)|pos[ition][s])] %integers% (of|in) %string%"
		);
	}

	private Expression<BigInteger> position;
	private Expression<String> value;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		position = (Expression<BigInteger>) expressions[0];
		value = (Expression<String>) expressions[1];
		return true;
	}

	@Override
	public String[] getValues(TriggerContext ctx) {
		return value.getSingle(ctx)
				.map(val -> position.stream(ctx)
						.filter(pos -> pos.signum() > 0 && pos.compareTo(BigInteger.valueOf(val.length())) <= 0)
						.map(pos -> String.valueOf(val.charAt(pos.intValue() - 1)))
						.toArray(String[]::new))
				.orElse(new String[0]);
	}

	@Override
	public boolean isSingle() {
		return position.isSingle();
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return "character at index " + position.toString(ctx, debug) + " in " + value.toString(ctx, debug);
	}
}
