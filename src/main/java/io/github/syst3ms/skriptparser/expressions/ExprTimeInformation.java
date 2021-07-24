package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.Time;

import java.math.BigInteger;

/**
 * Information of a certain time.
 *
 * @name Date Information
 * @type EXPRESSION
 * @pattern [the] (hour[s]|minute[s]|second[s]|milli[second][s]) of [time] %time%
 * @pattern [time] %time%'[s] (hour[s]|minute[s]|second[s]|milli[second][s])
 * @since ALPHA
 * @author Mwexim
 */
public class ExprTimeInformation extends PropertyExpression<Number, Time> {
	static {
		Parser.getMainRegistration().addPropertyExpression(
				ExprTimeInformation.class,
				Number.class,
				5, // Leave this here
				"*[time] %time%",
				"(0:hour[s]|1:minute[s]|2:second[s]|3:milli[second][s])"
		);
	}

	private final static String[] CHOICES = {
			"hours", "minutes", "seconds", "milliseconds"
	};

	private int mark;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		mark = parseContext.getNumericMark();
		return super.init(expressions, matchedPattern, parseContext);
	}

	@Override
	public Number getProperty(Time owner) {
		switch (mark) {
			case 0:
				return BigInteger.valueOf(owner.getHour());
			case 1:
				return BigInteger.valueOf(owner.getMinute());
			case 2:
				return BigInteger.valueOf(owner.getSecond());
			case 3:
				return BigInteger.valueOf(owner.getMillis());
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return CHOICES[mark] + " of time " + getOwner().toString(ctx, debug);
	}
}
