package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.SkriptDate;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * Information of a certain date.
 *
 * @name Date Information
 * @type EXPRESSION
 * @pattern [the] (year[s]|month[s]|day[s] (of|in) year|day[s] (of|in) month|day[s] (of|in) week|hour[s]|minute[s]|second[s]|milli[second][s]) of [date] %date%
 * @pattern [date] %date%'[s] (year[s]|month[s]|day[s] (of|in) year|day[s] (of|in) month|day[s] (of|in) week|hour[s]|minute[s]|second[s]|milli[second][s])
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDateInformation extends PropertyExpression<Number, SkriptDate> {
	static {
		Parser.getMainRegistration().addPropertyExpression(
				ExprDateInformation.class,
				Number.class,
				true,
				4,
				"*[date] %date%",
				"(0:year[s]|1:month[s]|2:day[s] (of|in) year|3:day[s] (of|in) month|4:day[s] (of|in) week|5:hour[s]|6:minute[s]|7:second[s]|8:milli[second][s])"
		);
	}

	private final static String[] CHOICES = {
			"year", "month", "day of year", "day of month", "day of week", "hours", "minutes", "seconds", "milliseconds"
	};

	private int parseMark;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		parseMark = parseContext.getNumericMark();
		setOwner((Expression<SkriptDate>) expressions[0]);
		return true;
	}

	@Override
	public Number[] getProperty(SkriptDate[] owners) {
		LocalDateTime lcd = owners[0].toLocalDateTime();
		switch (parseMark) {
			case 0:
				return new Number[] {BigInteger.valueOf(lcd.getYear())};
			case 1:
				return new Number[] {BigInteger.valueOf(lcd.getMonthValue())};
			case 2:
				return new Number[] {BigInteger.valueOf(lcd.getDayOfYear())};
			case 3:
				return new Number[] {BigInteger.valueOf(lcd.getDayOfMonth())};
			case 4:
				return new Number[] {BigInteger.valueOf(lcd.getDayOfWeek().getValue())};
			case 5:
				return new Number[] {BigInteger.valueOf(lcd.getHour())};
			case 6:
				return new Number[] {BigInteger.valueOf(lcd.getMinute())};
			case 7:
				return new Number[] {BigInteger.valueOf(lcd.getSecond())};
			case 8:
				return new Number[] {BigInteger.valueOf(lcd.getNano() / 1_000_000)};
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return CHOICES[parseMark] + " of date " + getOwner().toString(ctx, debug);
	}
}
