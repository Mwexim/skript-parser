package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.comparisons.Comparator;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import io.github.syst3ms.skriptparser.util.math.NumberMath;
import org.jetbrains.annotations.Nullable;

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
        Main.getMainRegistration().addExpression(
                ExprRandomNumber.class,
                Number.class,
                true,
                "[a] random integer [1:strictly] (from|between) %integer% (to|and) %integer%",
                "[a] random number [1:strictly] (from|between) %number% (to|and) %number%"
        );
    }

    private Expression<Number> lowerNumber, maxNumber;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private boolean isInteger, isExclusive;
    private Comparator<? super Number, ? super Number> numComp;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext context) {
        lowerNumber = (Expression<Number>) expressions[0];
        maxNumber = (Expression<Number>) expressions[1];
        isInteger = matchedPattern == 0;
        isExclusive = context.getParseMark() == 1;
        numComp = Comparators.getComparator(Number.class, Number.class);
        assert numComp != null;
        return true;
    }

    @Override
    public Number[] getValues(TriggerContext ctx) {
        Number low = lowerNumber.getSingle(ctx);
        Number max = maxNumber.getSingle(ctx);
        if (low == null || max == null)
            return new Number[0];
        //Check to find out which number is the greater of the 2, while keeping the type
        Number realLow = Relation.SMALLER_OR_EQUAL.is(numComp.apply(low, max)) ? low : max;
        Number realMax = Relation.SMALLER_OR_EQUAL.is(numComp.apply(low, max)) ? max : low;
        return new Number[]{NumberMath.random(realLow, realMax, !isExclusive, random)};
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "random " + (isInteger ? "integer " : "number ") + (isExclusive ? "strictly " : "") + "between " + lowerNumber.toString(ctx, debug) + " and " + maxNumber.toString(ctx, debug);
    }
}
