package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.SkriptDate;
import io.github.syst3ms.skriptparser.util.Time;

import java.time.Duration;

/**
 * Today at a given time. When no time is set,
 * it will take today's date as it would be midnight.
 *
 * @name Today At
 * @type EXPRESSION
 * @pattern today at %time%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDateTodayAt implements Expression<SkriptDate> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprDateTodayAt.class,
				SkriptDate.class,
				true,
				"today [1:at %time%]"
		);
	}

	private Expression<Time> time;
	private boolean simple;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		simple = parseContext.getNumericMark() == 1;
		if (simple)
			time = (Expression<Time>) expressions[0];
 		return true;
	}

	@Override
	public SkriptDate[] getValues(TriggerContext ctx) {
		return simple
				? time.getSingle(ctx)
				.map(ti -> new SkriptDate[] {
						SkriptDate.today().plus(Duration.ofMillis(ti.toMillis()))
				})
				.orElse(new SkriptDate[0])
				: new SkriptDate[] {SkriptDate.today()};
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return "today" + (simple ? " at " + time.toString(ctx, debug) : "");
	}
}
