package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.Arrays;

/**
 * Check if a given string starts or ends with a certain string.
 *
 * @name Starts/Ends With
 * @type CONDITION
 * @pattern %strings% (start|end)[s] with %string%
 * @pattern %strings% do[es](n't| not) (start|end) with %string%
 * @since ALPHA
 * @author Mwexim
 */
public class CondExprStartsEnds extends ConditionalExpression {
    static {
        Parser.getMainRegistration().addExpression(
                CondExprStartsEnds.class,
                Boolean.class,
                true,
                "%strings% (0:start|1:end)[s] with %string%",
                "%strings% do[es](n't| not) (0:start|1:end) with %string%"
        );
    }

    private Expression<String> expr;
    private Expression<String> value;
    private boolean start;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        expr = (Expression<String>) expressions[0];
        value = (Expression<String>) expressions[1];
        start = parseContext.getNumericMark() == 0;
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        String[] values = expr.getValues(ctx);
        return isNegated() != value.getSingle(ctx)
                .filter(val -> Arrays.stream(values)
                        .allMatch(val2 -> start ? val2.startsWith(val) : val2.endsWith(val)))
                .isPresent();
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return expr.toString(ctx, debug) + (isNegated() ? " does not" : "") + (start ? " start with " : " end with ") + value.toString(ctx, debug);
    }
}
