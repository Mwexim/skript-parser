package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.util.SkriptDate;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.function.Function;

/**
 * Names of certain values of a date, for example the name of the month.
 *
 * @name Date Values
 * @type EXPRESSION
 * @pattern [the] (0:era|1:month|2:weekday|2:day [(of|in) week]) [name] of [date] %date%
 * @pattern [date] %date%'[s] (0:era|1:month|2:weekday|2:day [(of|in) week]) [name]
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDateValues extends PropertyExpression<String, SkriptDate> {
	public static final PatternInfos<Function<LocalDateTime, String>> PATTERNS = new PatternInfos<>(new Object[][] {
			{"era", (Function<LocalDateTime, String>) val -> val.toLocalDate().getEra().getDisplayName(TextStyle.SHORT, SkriptDate.DATE_LOCALE)},
			{"month", (Function<LocalDateTime, String>) val -> val.getMonth().getDisplayName(TextStyle.FULL, SkriptDate.DATE_LOCALE)},
			{"(weekday|day [(of|in) week])", (Function<LocalDateTime, String>) val -> val.getDayOfWeek().getDisplayName(TextStyle.FULL, SkriptDate.DATE_LOCALE)},
	});

//	static {
//		Parser.getMainRegistration().addPropertyExpression(
//				ExprDateValues.class,
//				String.class,
//				3,
//				"*[date] %date%",
//				PATTERNS.toChoiceGroup() + " [name]"
//		);
//	}

	private int mark;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		mark = parseContext.getNumericMark();
		return super.init(expressions, matchedPattern, parseContext);
	}

	@Override
	public String getProperty(SkriptDate owner) {
		return PATTERNS.getInfo(mark).apply(owner.toLocalDateTime());
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return toString(ctx, debug, new String[] {"era", "month", "weekday"}[mark] + " name");
	}
}
