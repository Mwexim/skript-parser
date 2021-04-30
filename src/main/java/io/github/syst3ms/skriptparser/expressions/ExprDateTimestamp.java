package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.classes.SkriptDate;

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
public class ExprDateTimestamp extends PropertyExpression<Number, SkriptDate> {
	static {
		Parser.getMainRegistration().addPropertyExpression(
				ExprDateTimestamp.class,
				Number.class,
				true,
				"*[date] %date%",
				"[1:unix] timestamp"
		);
	}

	private boolean unix;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		unix = parseContext.getParseMark() == 1;
		setOwner((Expression<SkriptDate>) expressions[0]);
		return true;
	}

	@Override
	public Number[] getProperty(SkriptDate[] owners) {
		return new Number[] {
				unix ? BigInteger.valueOf(Math.floorDiv(owners[0].getTimestamp(), 1000))
						: BigInteger.valueOf(owners[0].getTimestamp())
		};
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return (unix ? "unix " : "") + "timestamp of " + getOwner().toString(ctx, debug);
	}
}
