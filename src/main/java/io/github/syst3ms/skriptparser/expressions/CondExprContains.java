package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * See if a given list of objects contain a given element.
 * You can also check if a string contains another string.
 *
 * @name Contain
 * @type CONDITION
 * @pattern %string% [does(n't| not)] contains %string%
 * @pattern %objects% [do[es](n't| not)] contain[s] %object%
 * @since ALPHA
 * @author Mwexim
 */
public class CondExprContains extends ConditionalExpression {

    static {
        Main.getMainRegistration().addExpression(
                CondExprContains.class,
                Boolean.class,
                true,
                "%string% [1:does(n't| not)] contain[s] %string%",
                "%objects% [1:do[es](n't| not)] contain[s] %object%"
        );
    }

    private Expression<?> first;
    private Expression<?> second;

    private boolean onlyString;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        first = expressions[0];
        second = expressions[1];
        onlyString = matchedPattern == 0;
        setNegated(parseContext.getParseMark() == 1);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean check(TriggerContext ctx) {
        if (onlyString) {
            String f = ((Expression<String>) first).getSingle(ctx);
            String s = ((Expression<String>) second).getSingle(ctx);
            if (f == null || s == null) {
                return false;
            }
            return isNegated() != f.contains(s);
        } else {
            Object[] f = ((Expression<Object>) first).getValues(ctx);
            Object[] s = ((Expression<Object>) second).getValues(ctx);
            return isNegated() != Arrays.asList(f).containsAll(Arrays.asList(s));
        }
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return first.toString(ctx, debug) + (isNegated() ? " does not contain " : " contains ") + second.toString(ctx, debug);
    }
}
