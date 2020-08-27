package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.SkriptDate;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

/**
 * Information of a certain date.
 *
 * @name Date Information
 * @type EXPRESSION
 * @pattern [the] (year|month|day of year|day of month|day of week|hours|minutes|seconds) of [date] %date%
 * @pattern [date] %date%'[s] (year|month|day of year|day of month|day of week|hours|minutes|seconds)
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDateInformation extends PropertyExpression<Number, SkriptDate> {

	static {
		Main.getMainRegistration().addPropertyExpression(
				ExprDateInformation.class,
				Number.class,
				true,
				"*[date] %date%",
				"(0:year|1:month|2:day of year|3:day of month|4:day of week|5:hours|6:minutes|7:seconds)");
	}

	private final static String[] CHOICES = {
			"year", "month", "day of year", "day of month", "day of week", "hours", "minutes", "seconds"
	};

	int parseMark;

	@Override
	public Optional<? extends Function<? super SkriptDate[], ? extends Number[]>> getPropertyFunction() {
		return Optional.of(dates -> {
			LocalDateTime lcd = dates[0].toLocalDateTime();
			switch (parseMark) {
				case 0:
					return new Number[] {lcd.getYear()};
				case 1:
					return new Number[] {lcd.getMonthValue()};
				case 2:
					return new Number[] {lcd.getDayOfYear()};
				case 3:
					return new Number[] {lcd.getDayOfMonth()};
				case 4:
					return new Number[] {lcd.getDayOfWeek().getValue()};
				case 5:
					return new Number[] {lcd.getHour()};
				case 6:
					return new Number[] {lcd.getMinute()};
				case 7:
					return new Number[] {lcd.getSecond()};
				default:
					return new Number[0];
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		parseMark = parseContext.getParseMark();
		setOwner((Expression<SkriptDate>) expressions[0]);
		return true;
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return CHOICES[parseMark] + " of date " + getOwner().toString(ctx, debug);
	}
}
