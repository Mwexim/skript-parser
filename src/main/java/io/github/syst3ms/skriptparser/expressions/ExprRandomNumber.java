package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.comparisons.Comparator;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import io.github.syst3ms.skriptparser.util.DoubleOptional;
import io.github.syst3ms.skriptparser.util.math.NumberMath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
/**
 * Generate a random number (double) or integer.
 *
 * @name random number
 * @pattern [a] random integer [strictly] (from|between) %number% (to|and) %number%
 * @pattern [a] random number [strictly] (from|between) %integer% (to|and) %integer%
 * @since ALPHA
 * @author WeeskyBDW
 */
public class ExprRandomNumber implements Expression<Number> {
    static {
        Parser.getMainRegistration().addExpression(
                ExprRandomNumber.class,
                Number.class,
                true,
                "[a] random integer [1:strictly] (from|between) %integer% (to|and) %integer%",
                "[a] random number [1:strictly] (from|between) %number% (to|and) %number%"
        );
    }

    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private Expression<Number> lowerNumber, maxNumber;
    private boolean isInteger, isExclusive;
    private Comparator<? super Number, ? super Number> numComp;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext context) {
        lowerNumber = (Expression<Number>) expressions[0];
        maxNumber = (Expression<Number>) expressions[1];
        isInteger = matchedPattern == 0;
        isExclusive = context.getNumericMark() == 1;
        numComp = Comparators.getComparator(Number.class, Number.class).orElseThrow(AssertionError::new);
        return true;
    }

    @Override
    public Number[] getValues(TriggerContext ctx) {
        return DoubleOptional.ofOptional(lowerNumber.getSingle(ctx), maxNumber.getSingle(ctx))
                .flatMap((l, m) -> DoubleOptional.of(
                        Relation.SMALLER_OR_EQUAL.is(numComp.apply(l, m)) ? l : m,
                        Relation.SMALLER_OR_EQUAL.is(numComp.apply(l, m)) ? m : l
                    )
                )
                .flatMap((f, s) -> DoubleOptional.ofOptional(handleIntegralDecimal(f), handleIntegralDecimal(s)))
                .mapToOptional((l, m) -> new Number[]{NumberMath.random(l, m, !isExclusive, random)})
                .orElse(new Number[0]);
    }

    private Optional<? extends Number> handleIntegralDecimal(Number n) {
        if (isInteger == n instanceof BigInteger) {
            // Either we want and have integers, or we don't want and don't have integers
            return Optional.of(n);
        } else if (isInteger) {
            // We want integers but the types are decimal
            if (n instanceof BigDecimal && ((BigDecimal) n).stripTrailingZeros().scale() >= 0) {
                return Optional.of(((BigDecimal) n).toBigIntegerExact());
            } else {
                return Optional.empty();
            }
        } else {
            // We don't want integers but the types are integral
            return Optional.of(new BigDecimal((BigInteger) n));
        }
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "a random " + (isInteger ? "integer " : "number ") + (isExclusive ? "strictly " : "") + "between " + lowerNumber.toString(ctx, debug) + " and " + maxNumber.toString(ctx, debug);
    }
}
