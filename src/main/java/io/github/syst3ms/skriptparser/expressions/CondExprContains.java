package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import io.github.syst3ms.skriptparser.util.DoubleOptional;

/**
 * See if a given list of objects contain a given element.
 * You can also check if a string contains another string.
 *
 * @name Contain
 * @type CONDITION
 * @pattern %string% [does(n't| not)] contain[s] %string%
 * @pattern %objects% [do[es](n't| not)] contain[s] %objects%
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
                "%objects% [1:do[es](n't| not)] contain[s] %objects%"
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
        if (!onlyString && !first.isAndList()) {
            parseContext.getLogger().error(
                    "An or-list cannot contain any values",
                    ErrorType.SEMANTIC_ERROR,
                    "If you want to check if an or-list 'contains' a value, you should do an equality check instead."
            );
            return false;
        }
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        if (onlyString) {
            return isNegated() != DoubleOptional.ofOptional(first.getSingle(ctx), second.getSingle(ctx))
                    .map(toCheck -> (String) toCheck, toMatch -> (String) toMatch)
                    .mapToOptional(String::contains)
                    .orElse(false);
        } else {
            return second.check(
                    ctx,
                    toMatch -> Expression.check(
                            first.getValues(ctx),
                            toCheck -> Comparators.compare(toCheck, toMatch).is(Relation.EQUAL),
                            false,
                            !first.isAndList()
                    ),
                    isNegated()
            );
        }
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return first.toString(ctx, debug) + (isNegated() ? " does not contain " : " contains ") + second.toString(ctx, debug);
    }
}
