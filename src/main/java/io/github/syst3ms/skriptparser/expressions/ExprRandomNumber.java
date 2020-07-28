package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.ThreadLocalRandom;
import java.lang.Math;
/**
 * Generate a random number (double) or integer.
 *
 * @name random number
 * @pattern [a] random (integer|number) (from|between) %number% (to|and) %number%
 * @since ALPHA
 * @author WeeskyBDW
 */
public class ExprRandomNumber implements Expression<Number> {

    private Expression<Number> lowerNumber, maxNumber;

	private boolean isInteger;
    static {
        Main.getMainRegistration().addExpression(
                ExprRandomNumber.class,
                Number.class,
                true,
                "[a] random (1:integer|2:number) (from|between) %number% (to|and) %number%"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext context) {
        lowerNumber = (Expression<Number>) expressions[0];
        maxNumber = (Expression<Number>) expressions[1];
		isInteger = context.getParseMark() == 1;
        return true;
    }

    @Override
    public Number[] getValues(TriggerContext ctx) {
        Double low = lowerNumber.getSingle(ctx).doubleValue();
        Double max = maxNumber.getSingle(ctx).doubleValue();
		if(isInteger) {
			return new Long[]{ThreadLocalRandom.current().nextLong(Math.round(low), Math.round(max) + 1)};
		} else {
			return new Double[]{ThreadLocalRandom.current().nextDouble(low, max + 1)};
		}
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "Generate random " + (isInteger ? "integer" : "number") + " from " + lowerNumber.toString() + " to " + maxNumber.toString();
    }
}
