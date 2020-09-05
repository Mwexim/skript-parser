package io.github.syst3ms.skriptparser.expressions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.DoubleOptional;

/**
 * The character at a position of a string. The first character is at the position 0.
 *
 * @name Character at
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
	public boolean init(final Expression<?> @NotNull [] expressions, final int matchedPattern, @NotNull final ParseContext parseContext) {
		position = (Expression<Integer>) expressions[0];
		value = (Expression<String>) expressions[1];
		return true;
	}

	@Override
	public String[] getValues(final TriggerContext ctx) {
		DoubleOptional<? extends String, ? extends Integer> opt = DoubleOptional.ofOptional(value.getSingle(ctx), position.getSingle(ctx));
		return opt.mapToOptional((val, pos) -> new String[]{ String.valueOf(val.charAt(pos)) } )
			.orElse(new String[0]);
	}

	@Override
	public String toString(@Nullable final TriggerContext ctx, final boolean debug) {
		return "character at " + position.toString(ctx, debug) + " in " + value.toString(ctx, debug);
	}
}
