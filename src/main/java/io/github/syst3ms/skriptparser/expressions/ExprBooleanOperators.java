package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

/**
 * Basic boolean operators. It is possible to use conditions inside the operators.
 *
 * @name Boolean Operators
 * @pattern not %=boolean%
 * @pattern %=boolean% or %=boolean%
 * @pattern %=boolean% and %=boolean%
 * @since ALPHA
 * @author Syst3ms
 */
public class ExprBooleanOperators implements Expression<Boolean> {
    private int pattern;
    private Expression<Boolean> first;
    @Nullable
    private Expression<Boolean> second;

    static {
        Main.getMainRegistration().addExpression(
                ExprBooleanOperators.class,
                Boolean.class,
                true,
                3,
                "not %=boolean%",
                "%=boolean% or %=boolean%",
                "%=boolean% and %=boolean%"
        );
    }

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
        Optional<Boolean> f = first.getSingle(ctx);
        if (!f.isPresent())
            return new Boolean[0];
        if (pattern == 0) {
            return new Boolean[]{!f.get()};
        } else {
            Optional<Boolean> s = second.getSingle(ctx);
            if (!s.isPresent())
                return new Boolean[0];
            if (pattern == 1) {
                return new Boolean[]{f.get() || s.get()};
            } else {
                return new Boolean[]{f.get() && s.get()};
            }
        }
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
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
