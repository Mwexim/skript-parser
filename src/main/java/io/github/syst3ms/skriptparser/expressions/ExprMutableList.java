package io.github.syst3ms.skriptparser.expressions;

import java.util.Arrays;
import java.util.Collections;

import org.jetbrains.annotations.Nullable;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

/**
 * Reverses, shuffle or order given list.
 *
 * @name Mutable List
 * @pattern reverse[d] %objects%
 * @pattern shuffle[d] %objects%
 * @pattern sort[ed] %objects%
 * @since ALPHA
 * @author Olyno
 */
public class ExprMutableList implements Expression<Object> {

    private enum MutableType {
        REVERSE, SHUFFLE, SORT
    }

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
    private MutableType type;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        list = (Expression<Object>) expressions[0];
        type = MutableType.values()[matchedPattern];
        return true;
    }

    @Override
    public Object[] getValues(TriggerContext ctx) {
        Object[] values = list.getValues(ctx);
        switch (type) {
            case REVERSE:
                Collections.reverse(Arrays.asList(values));
                break;
            case SHUFFLE:
                Collections.shuffle(Arrays.asList(values));
                break;
            case SORT:
                Arrays.sort(values);
                break;

        }
        return values;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return type.name().replaceAll("_", "").toLowerCase() + " " + list.toString(ctx, debug);
    }
}
