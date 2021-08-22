package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.DoubleOptional;
import io.github.syst3ms.skriptparser.util.SkriptDate;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;

/**
 * The date that was a certain duration ago or is a certain duration in the future.
 *
 * @name Ago/Later
 * @type EXPRESSION
 * @pattern %duration% (ago|in the past|before [the] [date] %date%)
 * @pattern %duration% (later|in the future|(from|after) [the] [date] %date%)
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDateAgoLater implements Expression<SkriptDate> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprDateAgoLater.class,
				SkriptDate.class,
				true,
				"%duration% (ago|in the past|before [the] [date] %date%)",
				"%duration% (later|in the future|(from|after) [the] [date] %date%)"
		);
	}

	private Expression<Duration> duration;
	@Nullable
	private Expression<SkriptDate> date;
	private boolean past;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		past = matchedPattern == 0;
		duration = (Expression<Duration>) expressions[0];
		if (expressions.length == 2)
			date = (Expression<SkriptDate>) expressions[1];
		return true;
	}

	@Override
	public SkriptDate[] getValues(TriggerContext ctx) {
		var actualDate = date != null ? date.getSingle(ctx) : Optional.of(SkriptDate.now());
		return DoubleOptional.ofOptional(actualDate, duration.getSingle(ctx))
				.mapToOptional((da, du) -> new SkriptDate[] {past ? da.minus(du) : da.plus(du)})
				.orElse(new SkriptDate[0]);
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return date != null
				? duration.toString(ctx, debug) + (past ? " before date " : " after date ") + date.toString(ctx, debug)
				: duration.toString(ctx, debug) + (past ? " in the past" : " in the future");
	}
}
