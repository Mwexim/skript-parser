package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.util.Time;

import java.math.BigInteger;
import java.util.function.Function;

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
	public static final PatternInfos<Function<Time, Integer>> PATTERNS = new PatternInfos<>(new Object[][] {
			{"hour[s]", (Function<Time, Integer>) Time::getHour},
			{"minute[s]", (Function<Time, Integer>) Time::getMinute},
			{"second[s]", (Function<Time, Integer>) Time::getSecond},
			{"milli[second][s]", (Function<Time, Integer>) Time::getMillis}
	});

	static {
		Parser.getMainRegistration().addPropertyExpression(
				ExprTimeInformation.class,
				Number.class,
				5, // Leave this here
				"*[time] %time%",
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
	public Number getProperty(Time owner) {
		return BigInteger.valueOf(PATTERNS.getInfo(mark).apply(owner));
	}

	@Override
	public String toString(TriggerContext ctx, boolean debug) {
		return toString(ctx, debug, new String[] {"hours", "minutes", "seconds", "milliseconds"}[mark]);
	}
}
