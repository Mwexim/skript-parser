package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.classes.DoubleOptional;
import io.github.syst3ms.skriptparser.util.classes.SkriptDate;

/**
 * Formats a date as a string using the given format.
 * Learn <a href="https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html">here</a>
 * how you can use different formats.
 *
 * @name Formatted Date
 * @type EXPRESSION
 * @pattern [date] %date% formatted as %string%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDateFormatted implements Expression<String> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprDateFormatted.class,
				String.class,
				true,
				"[date] %date% formatted as %string%"
		);
	}

	private Expression<SkriptDate> date;
	private Expression<String> format;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		date = (Expression<SkriptDate>) expressions[0];
		format = (Expression<String>) expressions[1];
		return true;
	}

	@Override
	public String[] getValues(TriggerContext ctx) {
		return DoubleOptional.ofOptional(date.getSingle(ctx), format.getSingle(ctx))
				.mapToOptional((da, f) -> new String[] {da.toString(f)})
				.orElse(new String[0]);
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return date.toString(ctx, debug) + " formatted as " + format.toString(ctx, debug);
	}
}
