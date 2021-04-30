package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.classes.DoubleOptional;
import io.github.syst3ms.skriptparser.util.classes.SkriptDate;

import java.time.Duration;

/**
 * Check if a given date is a certain duration before or after the current date.
 *
 * @name Compare Date
 * @type CONDITION
 * @pattern %date% (was|were)( more|(n't| not) less) than %duration% [ago]
 * @pattern %date% (was|were)((n't| not) more| less) than %duration% [ago]
 * @since ALPHA
 * @author Mwexim
 */
public class CondExprDateCompare extends ConditionalExpression {
    static {
        Parser.getMainRegistration().addExpression(
                CondExprDateCompare.class,
                Boolean.class,
                true,
                "%date% (was|were)( more|(n't| not) less) than %duration% [ago]",
                "%date% (was|were)((n't| not) more| less) than %duration% [ago]"
        );
    }

    private Expression<SkriptDate> date;
    private Expression<Duration> duration;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        date = (Expression<SkriptDate>) expressions[0];
        duration = (Expression<Duration>) expressions[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        return DoubleOptional.ofOptional(date.getSingle(ctx), duration.getSingle(ctx))
                .filter(
                        (dat, dur) -> isNegated() != (dat.getTimestamp() < SkriptDate.now().getTimestamp() - dur.toMillis())
                )
                .isPresent();
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return date.toString(ctx, debug) + (isNegated() ? " was more than " : " was less than ") + duration.toString(ctx, debug) + " ago";
    }
}
