package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.SkriptDate;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

/**
 * The timestamp of a date.
 * The default timestamp returns the amount of <b>milliseconds</b> since the Unix Epoch.
 * The unix timestamp returns the amount of <b>seconds</b> since that same date.
 * The Unix Epoch is defined as January 1st 1970.
 *
 * @name Timestamp
 * @type EXPRESSION
 * @pattern [the] [unix] timestamp of [date] %date%
 * @pattern [date] %date%'[s] [unix] timestamp
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDateTimestamp extends PropertyExpression<Number, SkriptDate> {

	static {
		Main.getMainRegistration().addPropertyExpression(
				ExprDateTimestamp.class,
				Number.class,
				true,
				"*[date] %date%",
				"[1:unix] timestamp");
	}

	boolean unix;

	@Override
	public Optional<? extends Function<? super SkriptDate[], ? extends Number[]>> getPropertyFunction() {
		return Optional.of(
				dates -> new Number[] {
					unix
						? Math.floorDiv(dates[0].getTimestamp(), 1000)
						: dates[0].getTimestamp()
				}
		);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		unix = parseContext.getParseMark() == 1;
		setOwner((Expression<SkriptDate>) expressions[0]);
		return true;
	}

	@Override
	public String toString(@Nullable TriggerContext ctx, boolean debug) {
		return (unix ? "unix " : "") + "timestamp of " + getOwner().toString(ctx, debug);
	}
}
