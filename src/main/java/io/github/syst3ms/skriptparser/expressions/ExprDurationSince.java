package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.SkriptDate;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * The duration that has passed since a given date. This will basically compare
 * the current date and the given date.
 * Note that when the given date is in the feature, the duration will represent 0.
 * The precision can differ several milliseconds.
 *
 * @name Duration Since
 * @type EXPRESSION
 * @pattern [the] (duration|time) [passed] since [date] %date%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDurationSince implements Expression<Duration> {

	static {
		Parser.getMainRegistration().addExpression(
				ExprDurationSince.class,
				Duration.class,
				true,
				"[the] (duration|time) [passed] since [date] %date%"
		);
	}

	Expression<SkriptDate> date;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		date = (Expression<SkriptDate>) expressions[0];
		return true;
	}

	@Override
	public Duration[] getValues(TriggerContext ctx) {
		return date.getSingle(ctx)
				.filter(da -> da.compareTo(SkriptDate.now()) < 1)
				.map(da -> new Duration[] {da.difference(SkriptDate.now())})
				.orElse(new Duration[0]);
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return "duration since " + date.toString(ctx, debug);
	}
}
