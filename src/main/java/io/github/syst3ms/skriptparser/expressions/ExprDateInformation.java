package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.util.SkriptDate;
import io.github.syst3ms.skriptparser.util.Time;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.function.Function;

/**
 * Information of a certain date.
 *
 * @name Date Information
 * @type EXPRESSION
 * @pattern [the] (year[s]|month[s]|day[s] (of|in) year|day[s] (of|in) month|day[s] (of|in) week|hour[s]|minute[s]|second[s]|milli[second][s]) (of|in) %date/time%
 * @pattern [the] (era|month|weekday|day [(of|in) week]) [name] (of|in) [date] %date%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDateInformation implements Expression<Object> {
	private static final PatternInfos<Function<LocalDateTime, Integer>> NUMBER_VALUES = new PatternInfos<>(new Object[][] {
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

	private static final PatternInfos<Function<LocalDateTime, String>> STRING_VALUES = new PatternInfos<>(new Object[][] {
			{"era", (Function<LocalDateTime, String>) val -> val.toLocalDate().getEra().getDisplayName(TextStyle.SHORT, SkriptDate.DATE_LOCALE)},
			{"month", (Function<LocalDateTime, String>) val -> val.getMonth().getDisplayName(TextStyle.FULL, SkriptDate.DATE_LOCALE)},
			{"(weekday|day [(of|in) week])", (Function<LocalDateTime, String>) val -> val.getDayOfWeek().getDisplayName(TextStyle.FULL, SkriptDate.DATE_LOCALE)},
	});

	static {
		Parser.getMainRegistration().addExpression(
				ExprDateInformation.class,
				Object.class,
				true,
				"[the] " + NUMBER_VALUES.toChoiceGroup() + " (of|in) %date/time%",
				"[the] " + STRING_VALUES.toChoiceGroup() + " [name] (of|in) [date] %date%"
		);
	}

	private Expression<?> value;
	private boolean returnsNumber;
	private int mark;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		returnsNumber = matchedPattern == 0;
		mark = parseContext.getNumericMark();

		value = expressions[0];
		return value.getReturnType() != Time.class || 5 <= mark && mark <= 8;
	}

	@Override
	public Object[] getValues(TriggerContext ctx) {
		return value.getSingle(ctx)
				.map(val -> {
					if (returnsNumber) {
						if (val instanceof SkriptDate) {
							return BigInteger.valueOf(NUMBER_VALUES.getInfo(mark).apply(((SkriptDate) val).toLocalDateTime()));
						} else {
							assert val instanceof Time;
							var todayAt = SkriptDate.today().plus(Duration.ofMillis(((Time) val).toMillis()));
							return BigInteger.valueOf(NUMBER_VALUES.getInfo(mark).apply(todayAt.toLocalDateTime()));
						}
					} else {
						assert val instanceof SkriptDate;
						return STRING_VALUES.getInfo(mark).apply(((SkriptDate) val).toLocalDateTime());
					}
				})
				.map(val -> new Object[] {val})
				.orElse(new Object[0]);
	}

	@Override
	public Class<?> getReturnType() {
		return returnsNumber ? Number.class : String.class;
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		if (returnsNumber) {
			return new String[] {"year", "month", "day of year", "day of month", "day of week", "hours", "minutes", "seconds", "milliseconds"}[mark]
					+ " of "
					+ value.toString(ctx, debug);
		} else {
			return new String[] {"era", "month", "weekday"}[mark] + " of " + value.toString(ctx, debug);
		}
	}
}
