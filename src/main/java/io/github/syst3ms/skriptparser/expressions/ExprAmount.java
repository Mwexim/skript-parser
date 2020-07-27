package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

/**
 * Amount of a list of values.
 *
 * @name Amount
 * @pattern (amount|number|size) of %objects%
 * @since ALPHA
 * @author Olyno
 */
public class ExprAmount implements Expression<Number> {

    private Expression<Object> valuesList;

    static {
        Main.getMainRegistration().addExpression(
            ExprAmount.class,
            Number.class,
            true,
            "(amount|number|size) of %objects%"
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        valuesList = (Expression<Object>) expressions[0];
        return true;
    }

    @Override
    public Number[] getValues(TriggerContext ctx) {
        return new Number[]{valuesList.getValues(ctx).length};
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "amount of " + valuesList.toString(ctx, debug);
    }
}
