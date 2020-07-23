package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper that turns a condition into a boolean expression than can be used anywhere.
 *
 * @name Whether
 * @pattern whether %=boolean%
 * @since ALPHA
 * @author Syst3ms
 */
public class ExprWhether implements Expression<Boolean> {
    private Expression<Boolean> condition;

    static {
        Main.getMainRegistration().addExpression(
                ExprWhether.class,
                Boolean.class,
                true,
                "whether %~=boolean%"
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        condition = (Expression<Boolean>) expressions[0];
        return true;
    }

    @Override
    public Boolean[] getValues(TriggerContext ctx) {
        return condition.getValues(ctx);
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "whether " + condition.toString(ctx, debug);
    }
}
