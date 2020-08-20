package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

/**
 * The index of the first or last occurrence of a given string in a string.
 *
 * @name Occurrence
 * @type EXPRESSION
 * @pattern [the] (first|last) occurrence of %string% in %string%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprStringOccurrence implements Expression<Number> {

	static {
		Main.getMainRegistration().addExpression(
				ExprStringOccurrence.class,
				Number.class,
				true,
				"[the] (0:first|1:last) occurrence of %string% in %string%");
	}

	private Expression<String> value, expr;
	private boolean first;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		first = parseContext.getParseMark() == 0;
		value = (Expression<String>) expressions[0];
		expr = (Expression<String>) expressions[1];
		return true;
	}

	@Override
	public Number[] getValues(TriggerContext ctx) {
		String v = value.getSingle(ctx), e = expr.getSingle(ctx);
		if (v == null || e == null)
			return new Number[0];

		int i = first ? e.indexOf(v) : e.lastIndexOf(v);
		// Return i + 1, since Skript indices start at 1.
		return i == -1 ? new Number[0] : new Number[] {i + 1};
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return (first ? "first" : "last") + " occurrence of " + value.toString(ctx, debug) + " in " + expr.toString(ctx, debug);
	}
}
