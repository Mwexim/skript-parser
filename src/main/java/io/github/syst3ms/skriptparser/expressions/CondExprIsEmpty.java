package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

/**
 * Check if a given string or list is empty.
 * Note that multiple strings will all be checked for blankness.
 * If you want a string list to be checked as a list instead of all separate strings,
 * you should use the 'list' modifier at the end.
 *
 * @name Is Empty
 * @pattern %strings% (is)[1:( not|n't)] empty
 * @pattern %objects% (is|are)[1:( not|n't)] empty
 * @since ALPHA
 * @author Olyno
 */
public class CondExprIsEmpty extends ConditionalExpression {
    static {
        Parser.getMainRegistration().addExpression(CondExprIsEmpty.class,
                Boolean.class,
                true,
                5,
                "%objects% is[1:( not|n't)] empty",
                "%strings% (is|are)[1:( not|n't)] [an] empty string[s]"
        );
    }

    private Expression<?> expr;
    private boolean isList;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        expr = expressions[0];
        isList = matchedPattern == 0;
        setNegated(parseContext.getParseMark() == 1);
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        var values = expr.getValues(ctx);
        return isNegated() != (isList && values.length != 1
                ? values.length == 0
                : values instanceof String[] && ((String) values[0]).isBlank());
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return expr.toString(ctx, debug) + (isNegated() ? " is not " : " is ") + "empty";
    }
} 
