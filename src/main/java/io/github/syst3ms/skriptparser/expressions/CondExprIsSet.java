package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

/**
 * Check if a given expression is set (null on the Java side) or not.
 *
 * @name Is Set
 * @type CONDITION
 * @pattern %objects% (is|are)[ not|n't] set
 * @since ALPHA
 * @author Syst3ms
 */
public class CondExprIsSet extends ConditionalExpression {
    private Expression<?> expr;

    static {
        Main.getMainRegistration().addExpression(
                CondExprIsSet.class,
                Boolean.class,
                true,
                2,
                "%~objects% (is|are)[1:( not|n't)] set"
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        expr = expressions[0];
        setNegated(parseContext.getParseMark() == 1);
        return true;
    }

    @Override
    protected boolean check(TriggerContext ctx) {
        return isNegated() != (expr == null || expr.getValues(ctx).length == 0);
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return expr.toString(ctx, debug) + (isNegated() ? " is not " : " is ") + "set";
    }
}
