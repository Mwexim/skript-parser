package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.DoubleOptional;
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

    Expression<Object> first, second;
    Arithmetic<?, ?> math;
    Class<?> commonSuperClass;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        first = (Expression<Object>) expressions[0];
        second = (Expression<Object>) expressions[1];
        var commonType = TypeManager.getByClass(
                ClassUtils.getCommonSuperclass(first.getReturnType(), second.getReturnType())
        );
        if (commonType.isEmpty()) {
            commonType = TypeManager.getByClassExact(Object.class);
            assert commonType.isPresent();
        }
        var type = commonType.get();
        var arithmetic = type.getArithmetic();
        commonSuperClass = commonType.get().getTypeClass();
        if (arithmetic.isEmpty()) {
            parseContext.getLogger().error("Can't compare these two values", ErrorType.SEMANTIC_ERROR);
            return false;
        }
        math = arithmetic.get();
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return commonSuperClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getValues(TriggerContext ctx) {
        return DoubleOptional.of(first.getSingle(ctx), second.getSingle(ctx))
                .mapToOptional((f, s) -> new Object[] {((Arithmetic<Object, Object>) math).difference(f, s)})
                .orElse(new Object[0]);
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "difference between " + first.toString(ctx, debug) + " and " + second.toString(ctx, debug);
    }
}
