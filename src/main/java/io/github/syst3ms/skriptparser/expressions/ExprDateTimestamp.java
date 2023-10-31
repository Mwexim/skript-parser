package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.SkriptDate;

import java.math.BigInteger;

/**
 * The timestamp of a date.
 * The default timestamp returns the amount of <b>milliseconds</b> since the Unix Epoch.
 * The unix timestamp returns the amount of <b>seconds</b> since that same date.
 * The Unix Epoch is defined as January 1st 1970.
 *
 * @name Timestamp
 * @type EXPRESSION
 * @pattern [the] [unix] timestamp of [date] %date%
 * @pattern [date] %date%'[s] [unix] timestamp
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDateTimestamp extends PropertyExpression<SkriptDate, Number> {
	static {
		Parser.getMainRegistration().addPropertyExpression(
				ExprDateTimestamp.class,
				Number.class,
				"[1:unix] timestamp",
				"*[date] %date%"
		);
	}

	private boolean unix;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		unix = parseContext.getNumericMark() == 1;
		return super.init(expressions, matchedPattern, parseContext);
	}

	@Override
	public Number getProperty(SkriptDate owner) {
		return unix ? BigInteger.valueOf(Math.floorDiv(owner.getTimestamp(), 1000))
				: BigInteger.valueOf(owner.getTimestamp());
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return toString(ctx, debug, (unix ? "unix " : "") + "timestamp");
	}
}
