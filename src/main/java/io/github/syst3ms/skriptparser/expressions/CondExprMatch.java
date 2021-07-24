package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.regex.Pattern;

/**
 * Check if the given strings match a certain regex expression.
 *
 * @name Match
 * @type CONDITION
 * @pattern %strings% [do[es](n't| not)] [part[ial]ly] match[es] %strings%
 * @since ALPHA
 * @author Mwexim
 */
public class CondExprMatch extends ConditionalExpression {
    static {
        Parser.getMainRegistration().addExpression(
                CondExprMatch.class,
                Boolean.class,
                true,
                "%strings% [1:do[es](n't| not)] [2:part[ial]ly] match[es] %strings%"
        );
    }

    private Expression<String> matched;
    private Expression<String> pattern;
    private boolean partly;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        matched = (Expression<String>) expressions[0];
        pattern = (Expression<String>) expressions[1];
        // 1 = negated, 2 = partially, 3 = negated and partially
        setNegated(parseContext.getNumericMark() == 1 || parseContext.getNumericMark() == 3);
        partly = parseContext.getNumericMark() == 2 || parseContext.getNumericMark() == 3;
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        return Expression.check(
                matched.getValues(ctx),
                toMatch -> Expression.check(
                        pattern.getValues(ctx),
                        pattern -> partly ? Pattern.compile(pattern).matcher(toMatch).find() : toMatch.matches(pattern),
                        false,
                        pattern.isAndList()
                ),
                isNegated(),
                matched.isAndList()
        );
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return matched.toString(ctx, debug)
                + (isNegated() ? " does not" : "")
                + (partly ? " partially" : "")
                + (isNegated() ? " match " : " matches ")
                + pattern.toString(ctx, debug);
    }
}
