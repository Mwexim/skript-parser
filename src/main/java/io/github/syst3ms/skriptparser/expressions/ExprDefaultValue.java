package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import org.jetbrains.annotations.Nullable;

/**
 * A shorthand expression for giving things a default value. If the first thing isn't set, the second thing will be returned.
 *
 * @name Default Value
 * @pattern %objects% (otherwise|?) %objects%
 * @since ALPHA
 * @author Olyno
 */
public class ExprDefaultValue implements Expression<Object> {

    private Expression<Object> firstValue, secondValue;

    static {
        Main.getMainRegistration().addExpression(
            ExprDefaultValue.class,
            Object.class,
            false,
            3,
            "%objects% (otherwise|?) %objects%"
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        firstValue = (Expression<Object>) expressions[0];
        secondValue = (Expression<Object>) expressions[1];
        return true;
    }

    @Override
    public Object[] getValues(TriggerContext ctx) {
        Object[] first = firstValue.getValues(ctx);
        Object[] second = secondValue.getValues(ctx);
        if (first.length != 0) {
            return first;
        } else if (second.length != 0) {
            return second;
        } else {
            return new Object[0];
        }
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return firstValue.toString(ctx, debug) + " otherwise " + secondValue.toString(ctx, debug);
    }

}
