package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.DoubleOptional;

import java.math.BigInteger;

/**
 * The index of the first or last occurrence of a given string in a string.
 * Note that indices in Skript start at 1.
 *
 * @name Occurrence
 * @type EXPRESSION
 * @pattern [the] (first|last) occurrence of %string% in %string%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprStringOccurrence implements Expression<Number> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprStringOccurrence.class,
				Number.class,
				true,
				"[the] (0:first|1:last) occurrence of %string% in %string%"
		);
	}

	private Expression<String> value, expr;
	private boolean first;

	// TODO let this support nth occurrence
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		first = parseContext.getNumericMark() == 0;
		value = (Expression<String>) expressions[0];
		expr = (Expression<String>) expressions[1];
		return true;
	}

	@Override
	public Number[] getValues(TriggerContext ctx) {
		return DoubleOptional.ofOptional(value.getSingle(ctx), expr.getSingle(ctx))
				.mapToOptional((v, e) -> first ? e.indexOf(v) : e.lastIndexOf(v))
				.filter(i -> i != -1)
				.map(i -> {
					// Return i + 1, since Skript indices start at 1.
					return new Number[]{BigInteger.valueOf(i + 1)};
				})
				.orElse(new Number[0]);
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return (first ? "first" : "last") + " occurrence of " + value.toString(ctx, debug) + " in " + expr.toString(ctx, debug);
	}
}
