package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.util.SkriptDate;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.function.Function;

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
	public static final PatternInfos<Function<LocalDateTime, Integer>> PATTERNS = new PatternInfos<>(new Object[][] {
			{"year[s]", (Function<LocalDateTime, Number>) LocalDateTime::getYear},
			{"month[s]", (Function<LocalDateTime, Number>) LocalDateTime::getMonthValue},
			{"day[s] (of|in) year", (Function<LocalDateTime, Number>) LocalDateTime::getDayOfYear},
			{"day[s] (of|in) month", (Function<LocalDateTime, Number>) LocalDateTime::getDayOfMonth},
			{"day[s] (of|in) week", (Function<LocalDateTime, Number>) val -> val.getDayOfWeek().getValue()},
			{"hour[s]", (Function<LocalDateTime, Number>) LocalDateTime::getHour},
			{"minute[s]", (Function<LocalDateTime, Number>) LocalDateTime::getMinute},
			{"second[s]", (Function<LocalDateTime, Number>) LocalDateTime::getSecond},
			{"milli[second][s]", (Function<LocalDateTime, Number>) val -> val.getNano() / 1_000_000}
	});

	static {
		Parser.getMainRegistration().addPropertyExpression(
				ExprDateInformation.class,
				Number.class,
				4,
				"*[date] %date%",
				PATTERNS.toChoiceGroup()
		);
	}

	private int mark;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		mark = parseContext.getNumericMark();
		return super.init(expressions, matchedPattern, parseContext);
	}

	@Override
	public Number getProperty(SkriptDate owner) {
		return BigInteger.valueOf(PATTERNS.getInfo(mark).apply(owner.toLocalDateTime()));
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return toString(ctx, debug, new String[] {"year", "month", "day of year", "day of month", "day of week", "hours", "minutes", "seconds", "milliseconds"}[mark]);
	}
}
