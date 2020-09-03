package io.github.syst3ms.skriptparser.expressions;

import java.util.Arrays;

import org.jetbrains.annotations.Nullable;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

/**
 * Check if a given string or list is empty.
 *
 * @name Empty
 * @pattern %strings/objects% (is|are)[1:( not|n't)] empty
 * @since ALPHA
 * @author Olyno
 */
public class CondExprEmpty extends ConditionalExpression {

    static {
        Parser.getMainRegistration().addExpression(CondExprEmpty.class,
            Boolean.class,
            true,
            2,
            "%strings/objects% (is|are)[1:( not|n't)] empty"
        );
    }

    private Expression<?> expr;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        expr = (Expression<?>) expressions[0];
        setNegated(parseContext.getParseMark() == 1);
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        Object[] values = expr.getValues(ctx);
        if (values.length == 0)
            return !isNegated();
        if (values.length == 1)
            return !(values[0] instanceof String) != isNegated();
        if (values instanceof String[]) {
            return (Arrays.stream(values)
                .filter(value -> !((String) value).isBlank())
                .count() == 0) != isNegated();
        }
        return isNegated();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return expr.toString(ctx, debug) + (isNegated() ? " is not " : " is ") + "empty";
    }
} 
