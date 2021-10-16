package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.Arrays;
import java.util.Collections;

/**
 * Reverse, shuffle or order a given list.
 *
 * @name Mutable List
 * @pattern reverse[d] %objects%
 * @pattern shuffle[d] %objects%
 * @pattern sort[ed] %objects%
 * @since ALPHA
 * @author Olyno
 */
public class ExprMutableList implements Expression<Object> {
    static {
        Parser.getMainRegistration().addExpression(
            ExprMutableList.class,
            Object.class,
            false,
            "reverse[d] %objects%",
            "shuffle[d] %objects%",
            "sort[ed] %objects%"
        );
    }

    private Expression<Object> list;
    private int type;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        list = (Expression<Object>) expressions[0];
        type = matchedPattern;
        return true;
    }

    @Override
    public Object[] getValues(TriggerContext ctx) {
        Object[] values = list.getValues(ctx);
        switch (type) {
            case 0:
                Collections.reverse(Arrays.asList(values));
                break;
            case 1:
                Collections.shuffle(Arrays.asList(values));
                break;
            case 2:
                Arrays.sort(values);
                break;
            default:
                throw new IllegalStateException();
        }
        return values;
    }

    @Override
    public Class<?> getReturnType() {
        return list.getReturnType();
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return new String[] {"reversed ", "shuffled ", "sorted "}[type] + list.toString(ctx, debug);
    }
}
