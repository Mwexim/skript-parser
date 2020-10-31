package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.DoubleOptional;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;

/**
 * The character at a given position in a string. Note that indices in Skript start at 1.
 *
 * @name Character At
 * @pattern char[acter] at [(index|pos[ition])] %number% (of|in) %string%
 * @since ALPHA
 * @author Olyno
 */
public class ExprStringCharAt implements Expression<String> {
	static {
		Parser.getMainRegistration().addExpression(
			ExprStringCharAt.class,
			String.class,
			true,
			"char[acter] at [(index|pos[ition])] %integer% (of|in) %string%"
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
		return DoubleOptional.ofOptional(value.getSingle(ctx), position.getSingle(ctx))
			.filter((val, pos) -> pos.signum() > 0
					&& pos.compareTo(BigInteger.valueOf(val.length())) <= 0)
			.mapToOptional((val, pos) -> new String[] {String.valueOf(val.charAt(pos.intValue() - 1))})
			.orElse(new String[0]);
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return "character at index " + position.toString(ctx, debug) + " in " + value.toString(ctx, debug);
	}
}
