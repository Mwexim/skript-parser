package io.github.syst3ms.skriptparser.expressions;

import java.util.Arrays;
import java.util.Collections;

import org.jetbrains.annotations.Nullable;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

/**
 * Reverses given list.
 *
 * @name Reversed List
 * @pattern reversed %objects% 
 * @since ALPHA
 * @author Olyno
 */
public class ExprReversedList implements Expression<Object> {

    static {
        Parser.getMainRegistration().addExpression(
            ExprReversedList.class,
            Object.class,
            false,
            "reversed %objects%"
        );
    }

    private Expression<Object> list;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        list = (Expression<Object>) expressions[0];
        return true;
    }

    @Override
    public Object[] getValues(TriggerContext ctx) {
        Object[] values = list.getValues(ctx);
        Collections.reverse(Arrays.asList(values)); 
        return values;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "reversed " + list.toString(ctx, debug);
    }
}
