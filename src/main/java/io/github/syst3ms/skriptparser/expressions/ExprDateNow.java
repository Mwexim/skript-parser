package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.SkriptDate;

/**
 * The current date, the one from yesterday or the one from tomorrow.
 *
 * @name Now
 * @type EXPRESSION
 * @pattern (now|yesterday|tomorrow)
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDateNow implements Expression<SkriptDate> {
	static {
		Parser.getMainRegistration().addExpression(
				ExprDateNow.class,
				SkriptDate.class,
				true,
				"(0:now|1:yesterday|2:tomorrow)"
		);
	}

	private int parseMark;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		parseMark = parseContext.getNumericMark();
		return true;
	}

	@Override
	public SkriptDate[] getValues(TriggerContext ctx) {
		switch (parseMark) {
			case 0:
				return new SkriptDate[] {SkriptDate.now()};
			case 1:
				return new SkriptDate[] {SkriptDate.of(SkriptDate.now().getTimestamp() - SkriptDate.MILLIS_PER_DAY)};
			case 2:
				return new SkriptDate[] {SkriptDate.of(SkriptDate.now().getTimestamp() + SkriptDate.MILLIS_PER_DAY)};
		}
		return new SkriptDate[0];
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		switch (parseMark) {
			case 1:
				return "yesterday";
			case 2:
				return "tomorrow";
			case 0:
			default:
				return "now";
		}
	}
}
