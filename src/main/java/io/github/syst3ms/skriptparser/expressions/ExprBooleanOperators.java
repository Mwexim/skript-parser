package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Basic boolean operators. It is possible to use conditions inside the operators.
 *
 * @name Boolean Operators
 * @pattern not %=boolean%
 * @pattern %=boolean% (or|\|\|) %=boolean%
 * @pattern %=boolean% (and|&&) %=boolean%
 * @since ALPHA
 * @author Syst3ms
 */
public class ExprBooleanOperators implements Expression<Boolean> {
    static {
        Parser.getMainRegistration().addExpression(
                ExprBooleanOperators.class,
                Boolean.class,
                true,
                "not %=boolean%",
                "%=boolean% (or|\\|\\|) %=boolean%",
                "%=boolean% (and|&&) %=boolean%"
        );
    }

    private int pattern;
    private Expression<Boolean> first;
    @Nullable
    private Expression<Boolean> second;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        pattern = matchedPattern;
        first = (Expression<Boolean>) expressions[0];
        if (expressions.length > 1) {
            second = (Expression<Boolean>) expressions[1];
        }
        return true;
    }

    @Override
    public Boolean[] getValues(TriggerContext ctx) {
        assert second != null || pattern == 0;
        return first.getSingle(ctx)
                .flatMap(f -> pattern == 0
                        ? Optional.of(!f)
                        : second.getSingle(ctx).map(s -> pattern == 1 ? f || s : f && s)
                )
                .map(val -> new Boolean[] {val})
                .orElse(new Boolean[0]);
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        if (pattern == 0) {
            return "not " + first.toString(ctx, debug);
        } else {
            assert second != null;
            if (pattern == 1) {
                return first.toString(ctx, debug) + " or " + second.toString(ctx, debug);
            } else {
                return first.toString(ctx, debug) + " and " + second.toString(ctx, debug);
            }
        }
    }
}
