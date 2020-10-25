package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.DoubleOptional;
import org.jetbrains.annotations.Nullable;

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

	private Expression<Integer> position;
	private Expression<String> value;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		position = (Expression<Integer>) expressions[0];
		value = (Expression<String>) expressions[1];
		return true;
	}

	@Override
	public String[] getValues(TriggerContext ctx) {
		return DoubleOptional.ofOptional(value.getSingle(ctx), position.getSingle(ctx))
			.filter((val, pos) -> pos > 0 && pos <= val.length())
			.mapToOptional((val, pos) -> new String[] {String.valueOf(val.charAt(pos - 1))})
			.orElse(new String[0]);
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return "character at index " + position.toString(ctx, debug) + " in " + value.toString(ctx, debug);
	}
}
