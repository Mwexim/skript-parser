package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.classes.DoubleOptional;
import io.github.syst3ms.skriptparser.util.classes.SkriptDate;

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
				3,
				"%duration% (ago|in the past|1:before [the] [date] %date%)",
						"%duration% (later|in the future|1:(from|after) [the] [date] %date%)"
		);
	}

	private Expression<Duration> duration;
	private Expression<SkriptDate> date;
	private boolean past;
	private boolean relative;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		past = matchedPattern == 0;
		relative = parseContext.getParseMark() == 1;
		duration = (Expression<Duration>) expressions[0];
		if (relative)
			date = (Expression<SkriptDate>) expressions[1];
		return true;
	}

	@Override
	public SkriptDate[] getValues(TriggerContext ctx) {
		Optional<? extends Duration> dur = duration.getSingle(ctx);
		Optional<? extends SkriptDate> dat = relative ? date.getSingle(ctx) : Optional.of(SkriptDate.now());
		DoubleOptional<? extends SkriptDate, ? extends Duration> opt = DoubleOptional.ofOptional(dat, dur);
		return opt.mapToOptional((da, du) -> new SkriptDate[]{past ? da.minus(du) : da.plus(du)})
				.orElse(new SkriptDate[0]);
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		if (relative) {
			return duration.toString(ctx, debug) + (past ? " before date " : " after date ") + date.toString(ctx, debug);
		} else {
			return duration.toString(ctx, debug) + (past ? " in the past" : " in the future");
		}
	}
}
