package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;

import java.math.BigInteger;

/**
 * Length of a string.
 *
 * @name Length
 * @pattern [the] length of %string%
 * @pattern %string%'s length
 * @since ALPHA
 * @author Romitou
 */
public class ExprLength extends PropertyExpression<Number, String> {
    static {
        Parser.getMainRegistration().addPropertyExpression(
                ExprLength.class,
                Number.class,
                true,
                "string",
                "length"
        );
    }

    @Override
    public Number[] getProperty(String[] owners) {
        return new Number[] {BigInteger.valueOf(owners[0].length())};
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "length of " + getOwner().toString(ctx, debug);
    }
}
