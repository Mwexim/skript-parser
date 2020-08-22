package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

/**
 * Check if a given string starts or ends with a certain string.
 *
 * @name Starts/Ends With
 * @type CONDITION
 * @pattern %strings% (start|end)[s] with %string%
 * @pattern %strings% do[es](n't| not) (start|end) with %string%
 * @since ALPHA
 * @author Mwexim
 */
public class CondExprStartsEnds extends ConditionalExpression {

    static {
        Main.getMainRegistration()
                .addExpression(CondExprStartsEnds.class,
                        Boolean.class,
                        true,
                        "%strings% (0:start|1:end)[s] with %string%",
                        "%strings% do[es](n't| not) (0:start|1:end) with %string%");
    }

    private Expression<String> expr;
    private Expression<String> value;
    private boolean start;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        expr = (Expression<String>) expressions[0];
        value = (Expression<String>) expressions[1];
        start = parseContext.getParseMark() == 0;
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    protected boolean check(TriggerContext ctx) {
        String[] strs = expr.getValues(ctx);
        return value.getSingle(ctx).filter(v -> {
            for (String s : strs) {
                if (isNegated() != (start ? s.startsWith(v) : s.endsWith(v))) {
                    return false;
                }
            }
            return true;
        }).isPresent();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return expr.toString(ctx, debug) + (isNegated() ? " does not" : "") + (start ? " start with " : " end with ") + value.toString(ctx, debug);
    }
}
