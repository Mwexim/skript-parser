package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.Arrays;

/**
 * Check if a given string or list is empty.
 * Note that multiple strings will all be checked for blankness.
 * If you want a string list to be checked as a list instead of all separate strings,
 * you should use the 'list' modifier at the end.
 *
 * @name Is Empty
 * @pattern %objects% (is|are)[( not|n't)] empty
 * @pattern %strings% (is|are)[( not|n't)] [an] empty string[s]
 * @since ALPHA
 * @author Olyno
 */
public class CondExprIsEmpty extends ConditionalExpression {
    static {
        Parser.getMainRegistration().addExpression(CondExprIsEmpty.class,
                Boolean.class,
                true,
                5,
                "%objects% (is|are)[1:( not|n't)] empty",
                "%strings% (is|are)[1:( not|n't)] [an] empty string[s]"
        );
    }

    private Expression<?> expr;
    private boolean isList;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        expr = expressions[0];
        isList = matchedPattern == 0;
        setNegated(parseContext.getNumericMark() == 1);
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        var values = expr.getValues(ctx);
        if (isList) {
            if (values.length != 1) {
                return isNegated() != (values.length == 0);
            } else {
                return isNegated() != (values[0] instanceof String && ((String) values[0]).isBlank());
            }
        } else {
            return isNegated() != Arrays.stream((String[]) values)
                    .allMatch(String::isBlank);
        }
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return expr.toString(ctx, debug) + (isNegated() ? " is not " : " is ") + "empty";
    }
} 
