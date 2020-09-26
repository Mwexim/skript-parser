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
 * @pattern %strings% (is|are)[1:( not|n't)] empty
 * @pattern %objects% is[1:( not|n't)] [a[n]] empty list
 * @since ALPHA
 * @author Olyno
 */
public class CondExprEmpty extends ConditionalExpression {

    static {
        Parser.getMainRegistration().addExpression(CondExprEmpty.class,
            Boolean.class,
            true,
            2,
            "%strings% (is|are)[1:( not|n't)] empty",
            "%objects% is[1:( not|n't)] [a[n]] empty list"
        );
    }

    private Expression<?> expr;
    private boolean comparingList;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        expr = (Expression<?>) expressions[0];
        comparingList = matchedPattern == 1;
        setNegated(parseContext.getParseMark() == 1);
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        Object[] values = expr.getValues(ctx);
        boolean isEmpty = comparingList ? values.length == 0 : Arrays.stream(values)
            .allMatch(value -> value instanceof String && ((String) value).isBlank());
        return isEmpty != isNegated();

    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return expr.toString(ctx, debug) + (isNegated() ? " is not " : " is ") + "empty" + (comparingList ? " list" : "");
    }
} 
