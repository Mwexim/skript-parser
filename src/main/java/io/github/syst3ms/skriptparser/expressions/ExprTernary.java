package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

/**
 * Returns a value depending of a boolean.
 *
 * @name Ternary
 * @pattern %objects% if %=boolean%[,] (otherwise|else) %objects%
 * @pattern %=boolean% ? %objects% : %objects%
 * @since ALPHA
 * @author Olyno
 */
public class ExprTernary implements Expression<Object> {

    static {
        Main.getMainRegistration().addExpression(
            ExprTernary.class,
            Object.class,
            false,
            2,
            "%objects% if %=boolean%[,] (otherwise|else) %objects%",
            "%=boolean% ? %objects% : %objects%"
        );
    }

    private Expression<Boolean> valueToCheck;
    private Expression<Object> firstValue, secondValue;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        valueToCheck = (Expression<Boolean>) expressions[matchedPattern == 0 ? 1 : 0];
        firstValue = (Expression<Object>) expressions[matchedPattern == 0 ? 0 : 1];
        secondValue = (Expression<Object>) expressions[2];
        return true;
    }

    @Override
    public Object[] getValues(TriggerContext ctx) {
        Boolean check = valueToCheck.getSingle(ctx);
        Object[] first = firstValue.getValues(ctx);
        Object[] second = secondValue.getValues(ctx);
        if (check == null)
            return new Object[0];
        Object[] result = check ? first : second;
        if (result == null)
            return new Object[0];
        return result;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return firstValue.toString(ctx, debug) + " if " + valueToCheck.toString(ctx, debug) + " else " + secondValue.toString(ctx, debug);
    }
}
