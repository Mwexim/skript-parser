package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.classes.Time;

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
				true,
				5,
				"*[time] %time%",
				"(0:hour[s]|1:minute[s]|2:second[s]|3:milli[second][s])"
		);
	}

	private final static String[] CHOICES = {
			"hours", "minutes", "seconds", "milliseconds"
	};

	private int parseMark;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		parseMark = parseContext.getParseMark();
		setOwner((Expression<Time>) expressions[0]);
		return true;
	}

	@Override
	public Number[] getProperty(Time[] owners) {
		switch (parseMark) {
			case 0:
				return new Number[] {BigInteger.valueOf(owners[0].getHour())};
			case 1:
				return new Number[] {BigInteger.valueOf(owners[0].getMinute())};
			case 2:
				return new Number[] {BigInteger.valueOf(owners[0].getSecond())};
			case 3:
				return new Number[] {BigInteger.valueOf(owners[0].getMillis())};
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return CHOICES[parseMark] + " of time " + getOwner().toString(ctx, debug);
	}
}
