package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.Arrays;
import java.util.Optional;

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
        Parser.getMainRegistration().addExpression(
                CondExprContains.class,
                Boolean.class,
                true,
                2,
                "%string% [1:does(n't| not)] contain[s] %string%",
                "%objects% [1:do[es](n't| not)] contain[s] %object%"
        );
    }

    private Expression<?> haystack, needle;
    private boolean onlyString;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        haystack = expressions[0];
        needle = expressions[1];
        onlyString = matchedPattern == 0;
        setNegated(parseContext.getParseMark() == 1);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean check(TriggerContext ctx) {
        if (onlyString) {
            Optional<? extends String> haystackValue = ((Expression<String>) haystack).getSingle(ctx);
            Optional<? extends String> needleValue = ((Expression<String>) needle).getSingle(ctx);
            return isNegated() != haystackValue.filter(o1 -> needleValue.map(o1::contains).isPresent()).isPresent();
        } else {
            Object[] haystackValues = ((Expression<Object>) haystack).getValues(ctx);
            Object[] needleValues = ((Expression<Object>) needle).getValues(ctx);
            return isNegated() != Arrays.asList(haystackValues).containsAll(Arrays.asList(needleValues));
        }
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return haystack.toString(ctx, debug) + (isNegated() ? " does not contain " : " contains ") + needle.toString(ctx, debug);
    }
}
