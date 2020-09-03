package io.github.syst3ms.skriptparser.expressions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

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
			"char[acter] at [(index|pos[ition])] %number% (of|in) %string%"
		);
	}

	private Expression<Number> position;
	private Expression<String> value;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?> @NotNull [] expressions, final int matchedPattern, @NotNull final ParseContext parseContext) {
		position = (Expression<Number>) expressions[0];
		value = (Expression<String>) expressions[1];
		return true;
	}

	@Override
	public String[] getValues(final TriggerContext ctx) {
		return new String[] {String.valueOf(
			value.getSingle(ctx).get().charAt(position.getSingle(ctx).get().intValue())
		)};
	}

	@Override
	public String toString(@Nullable final TriggerContext ctx, final boolean debug) {
		return "character at " + position.getSingle(ctx).get() + " in " + value.toString(ctx, debug);
	}
}
