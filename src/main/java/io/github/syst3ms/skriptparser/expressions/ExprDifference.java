package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import org.jetbrains.annotations.Nullable;

/**
 * The difference between two values.
 * Note that only values that can be checked for difference are allowed
 * (this is for example numbers, dates, durations and others).
 *
 * @name Difference
 * @pattern difference (between|of) %object% and %object%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDifference implements Expression<Object> {

    static {
        Main.getMainRegistration().addExpression(
                ExprDifference.class,
                Object.class,
                true,
                "difference (between|of) %object% and %object%"
        );
    }

    Expression<?> first, second;
    Arithmetic<Object, Object> math;
    Class<?> commonSuperClass;
    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        first = expressions[0];
        second = expressions[1];
        Type<?> type = TypeManager.getByClass(
                ClassUtils.getCommonSuperclass(true, first.getReturnType(), second.getReturnType())
        );
        if (type == null) {
            // Should never happen!
            return false;
        }
        math = (Arithmetic<Object, Object>) type.getArithmetic();
        commonSuperClass = type.getTypeClass();
        if (math == null) {
            parseContext.getLogger().recurse();
            parseContext.getLogger().error("Can't compare these two values.", ErrorType.SEMANTIC_ERROR);
            parseContext.getLogger().callback();
            return false;
        }
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return commonSuperClass;
    }

    @Override
    public Object[] getValues(TriggerContext ctx) {
        Object f = first.getSingle(ctx);
        Object s = second.getSingle(ctx);
        if (f == null || s == null)
            return new Object[0];

        return new Object[] {math.difference(f, s)};
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "difference between " + first.toString(ctx, debug) + " and " + second.toString(ctx, debug);
    }
}
