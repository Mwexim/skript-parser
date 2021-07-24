package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.CollectionUtils;

import java.util.Optional;

/**
 * See if a given list of objects contain a given element.
 * You can also check if a string contains another string.
 *
 * @name Contain
 * @type CONDITION
 * @pattern %string% [does(n't| not)] contain[s] %string%
 * @pattern %objects% [do[es](n't| not)] contain[s] %object%
 * @since ALPHA
 * @author Mwexim
 */
public class CondExprContains extends ConditionalExpression {
    static {
        Parser.getMainRegistration().addExpression(
                CondExprContains.class,
                Boolean.class,
                true,
                2,
                "%string% [1:does(n't| not)] contain[s] %string%",
                "%objects% [1:do[es](n't| not)] contain[s] %object%"
        );
    }

    private Expression<?> first, second;
    private boolean onlyString;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        first = expressions[0];
        second = expressions[1];
        onlyString = matchedPattern == 0;
        setNegated(parseContext.getNumericMark() == 1);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean check(TriggerContext ctx) {
        if (onlyString) {
            Optional<? extends String> f = ((Expression<String>) first).getSingle(ctx);
            Optional<? extends String> s = ((Expression<String>) second).getSingle(ctx);
            return isNegated() != f.filter(o1 -> s.map(o1::contains).isPresent()).isPresent();
        } else {
            Object[] f = ((Expression<Object>) first).getValues(ctx);
            Object[] s = ((Expression<Object>) second).getValues(ctx);
            return isNegated() != CollectionUtils.contains(f, s);
        }
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return first.toString(ctx, debug) + (isNegated() ? " does not contain " : " contains ") + second.toString(ctx, debug);
    }
}
