package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.Time;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Function;

/**
 * Information of a certain time.
 *
 * @name Date Information
 * @type EXPRESSION
 * @pattern [the] (hours|minutes|seconds|milli[second]s) of [time] %time%
 * @pattern [time] %time%'[s] (hours|minutes|seconds|milli[second]s)
 * @since ALPHA
 * @author Mwexim
 */
public class ExprTimeInformation extends PropertyExpression<Number, Time> {

	static {
		Parser.getMainRegistration().addPropertyExpression(
				ExprTimeInformation.class,
				Number.class,
				true,
				"*[time] %time%",
				"(0:hours|1:minutes|2:seconds|3:milli[second]s)"
		);
	}

	private final static String[] CHOICES = {
			"hours", "minutes", "seconds", "milliseconds"
	};

	int parseMark;

	@Override
	public Optional<? extends Function<? super Time[], ? extends Number[]>> getPropertyFunction() {
		return Optional.of(times -> {
			switch (parseMark) {
				case 0:
					return new Number[] {BigInteger.valueOf(times[0].getHour())};
				case 1:
					return new Number[] {BigInteger.valueOf(times[0].getMinute())};
				case 2:
					return new Number[] {BigInteger.valueOf(times[0].getSecond())};
				case 3:
					return new Number[] {BigInteger.valueOf(times[0].getMillis())};
				default:
					throw new IllegalStateException();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		parseMark = parseContext.getParseMark();
		setOwner((Expression<Time>) expressions[0]);
		return true;
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return CHOICES[parseMark] + " of time " + getOwner().toString(ctx, debug);
	}
}
