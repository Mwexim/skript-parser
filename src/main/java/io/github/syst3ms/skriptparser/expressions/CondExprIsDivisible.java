package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.ConditionalType;
import io.github.syst3ms.skriptparser.lang.properties.PropertyConditional;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Check if a given number is divisible by another number.
 * Note that when the number is a decimal number,
 * the check will automatically fail.
 *
 * @name Is Divisible
 * @type CONDITION
 * @pattern %numbers% (is|are)[ not|n't] divisible by %integer%
 * @since ALPHA
 * @author Mwexim
 */
public class CondExprIsDivisible extends PropertyConditional<Number> {
    static {
        Parser.getMainRegistration().addPropertyConditional(
                CondExprIsDivisible.class,
                "numbers",
                ConditionalType.BE,
                "divisible by %integer%"
        );
    }

    private Expression<BigInteger> divider;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        divider = (Expression<BigInteger>) expressions[1];
        return super.init(expressions, matchedPattern, parseContext);
    }

    @Override
    public boolean check(TriggerContext ctx, Number[] performers) {
        return isNegated() != Arrays.stream(performers)
                .allMatch(val -> divider.getSingle(ctx)
                        .filter(__ -> BigDecimalMath.isIntValue(BigDecimalMath.getBigDecimal(val)))
                        .filter(d -> BigDecimalMath.getBigInteger(val).mod(d).equals(BigInteger.ZERO))
                        .isPresent()
                );
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return toString(ctx, debug, ConditionalType.BE, "divisible by " + divider.toString(ctx, debug));
    }
}
