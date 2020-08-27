package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.SkriptDate;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Optional;
import java.util.function.Function;

/**
 * Names of certain values of a date, for example the name of the month.
 *
 * @name Date Values
 * @type EXPRESSION
 * @pattern [the] (era|(name of month|month name)|(name of day [(in|of) week]|week[ ]day name)) of [date] %date%
 * @pattern [date] %date%'[s] (era|(name of month|month name)|(name of day [(in|of) week]|week[ ]day name))
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDateValues extends PropertyExpression<String, SkriptDate> {

	static {
		Main.getMainRegistration().addPropertyExpression(
				ExprDateValues.class,
				String.class,
				true,
				"*[date] %date%",
				"(0:era|1:(name of month|month name)|2:(name of day [(in|of) week]|week[ ]day name))");
	}

	int parseMark;

	@Override
	public Optional<? extends Function<? super SkriptDate[], ? extends String[]>> getPropertyFunction() {
		return Optional.of(dates -> {
			LocalDateTime lcd = dates[0].toLocalDateTime();
			switch (parseMark) {
				case 0:
					return new String[] {lcd.toLocalDate().getEra().getDisplayName(
							TextStyle.FULL, SkriptDate.DATE_LOCALE
					)};
				case 1:
					return new String[] {lcd.getMonth().getDisplayName(
							TextStyle.FULL, SkriptDate.DATE_LOCALE
					)};
				case 2:
					return new String[] {lcd.getDayOfWeek().getDisplayName(
							TextStyle.FULL, SkriptDate.DATE_LOCALE
					)};
				default:
					return new String[0];
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
		switch (parseMark)  {
			case 0:
				return "era of " + getOwner().toString(ctx, debug);
			case 1:
				return "month name of " + getOwner().toString(ctx, debug);
			case 2:
				return "weekday name of " + getOwner().toString(ctx, debug);
			default:
				return "date value of " + getOwner().toString(ctx, debug);
		}
	}
}
