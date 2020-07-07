package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
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

    private Expression<?> valueToCheck, firstValue, secondValue;

    static {
        Main.getMainRegistration().addExpression(
            ExprTernary.class,
            Object.class,
            false,
            "%objects% if %=boolean%[,] (otherwise|else) %objects%",
            "%=boolean% ? %objects% : %objects%"
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        valueToCheck = expressions[matchedPattern == 0 ? 1 : 0];
        firstValue = expressions[matchedPattern == 0 ? 0 : 1];
        secondValue = expressions[2];
        return true;
    }

    @Override
    public Object[] getValues(TriggerContext ctx) {
        Boolean check = (Boolean) valueToCheck.getSingle(ctx);
        Object first = firstValue.getSingle(ctx);
        Object second = secondValue.getSingle(ctx);
        return new Object[]{check ? first : second};
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return firstValue.toString(ctx, debug) + " if " + valueToCheck.toString(ctx, debug) + " else " + secondValue.toString(ctx, debug);
    }
}
