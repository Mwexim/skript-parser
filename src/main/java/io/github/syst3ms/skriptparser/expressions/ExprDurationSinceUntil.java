package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.classes.SkriptDate;

import java.time.Duration;

/**
 * Compares the given date with the current date.
 * Note that the first pattern is to compare with the past
 * and the second one to compare with the future.
 * Note that the accuracy can be off some milliseconds.
 *
 * @name Duration Since/Until
 * @type EXPRESSION
 * @pattern [the] (duration|time) [passed] since [date] %date%
 * @pattern [the] (duration|time) (until|till) [date] %date%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDurationSinceUntil implements Expression<Duration> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprDurationSinceUntil.class,
				Duration.class,
				true,
				"[the] (duration|time) [passed] since [date] %date%",
				"[the] (duration|time) (until|till) [date] %date%"
		);
	}

	private Expression<SkriptDate> date;
	private boolean past;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		date = (Expression<SkriptDate>) expressions[0];
		past = matchedPattern == 0;
		return true;
	}

	@Override
	public Duration[] getValues(TriggerContext ctx) {
		return date.getSingle(ctx)
				.filter(da -> da.compareTo(SkriptDate.now()) < 0 || !past)
				.map(da -> new Duration[] {da.difference(SkriptDate.now())})
				.orElse(new Duration[0]);
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return "duration " + (past ? "since " : "until ") + date.toString(ctx, debug);
	}
}
