package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.DoubleOptional;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Optional;

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
        Parser.getMainRegistration().addExpression(
                ExprDifference.class,
                Object.class,
                true,
                "difference (between|of) %object% and %object%"
        );
    }

    private Expression<?> first, second;
    @SuppressWarnings("rawtypes")
    @Nullable
    private Arithmetic math;
    private Class<?> mathType;
    private boolean bothVariables;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        first = expressions[0];
        second = expressions[1];
        Optional<? extends Type<?>> type;

        if (first instanceof Variable<?> && second instanceof Variable<?>) {
            bothVariables = true;
            type = TypeManager.getByClassExact(Object.class);
        } else if (first instanceof Literal<?> && second instanceof Literal<?>) {
            var firstConverted = first.convertExpression(Object.class);
            var secondConverted = second.convertExpression(Object.class);
            if (firstConverted.isEmpty() || secondConverted.isEmpty())
                return false;
            first = firstConverted.get();
            second = secondConverted.get();
            type = TypeManager.getByClass(
                    ClassUtils.getCommonSuperclass(first.getReturnType(), second.getReturnType())
            );
        } else {
            if (first instanceof Literal<?>) {
                var firstConverted = first.convertExpression(second.getReturnType());
                if (firstConverted.isEmpty())
                    return false;
                first = firstConverted.get();
            } else if (second instanceof Literal<?>) {
                var secondConverted = second.convertExpression(first.getReturnType());
                if (secondConverted.isEmpty())
                    return false;
                second = secondConverted.get();
            }

            if (first instanceof Variable) {
                first = first.convertExpression(second.getReturnType()).orElseThrow();
            } else if (second instanceof Variable) {
                second = second.convertExpression(first.getReturnType()).orElseThrow();
            }
            type = TypeManager.getByClass(
                    ClassUtils.getCommonSuperclass(first.getReturnType(), second.getReturnType())
            );
        }
        assert type.isPresent();
        if (!type.get().getTypeClass().equals(Object.class) && type.get().getArithmetic().isEmpty()) {
            var firstType = TypeManager.getByClass(first.getReturnType());
            var secondType = TypeManager.getByClass(second.getReturnType());
            assert firstType.isPresent() && secondType.isPresent();
            parseContext.getLogger().error(
                    "Cannot compare these two values"
                            + " (types '"
                            + firstType.get().getBaseName()
                            + "' and '"
                            + secondType.get().getBaseName()
                            + "' are inconvertible)",
                    ErrorType.SEMANTIC_ERROR
            );
            return false;
        }
        if (bothVariables) {
            mathType = Object.class; // Initialize less.
        } else {
            assert type.get().getArithmetic().isPresent();
            math = type.get().getArithmetic().get();
            mathType = math.getRelativeType();
        }
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return mathType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getValues(TriggerContext ctx) {
        return DoubleOptional.ofOptional(first.getSingle(ctx), second.getSingle(ctx))
                .mapToOptional((f, s) -> {
                    Object[] diff = (Object[]) Array.newInstance(mathType, 1);

                    // The Arithmetic field isn't initialized here.
                    if (bothVariables) {
                        var type = TypeManager.getByClass(f.getClass()).orElseThrow();
                        var variableMath = type.getArithmetic();
                        if (variableMath.isEmpty())
                            return null;
                        math = variableMath.get();
                    }

                    assert math != null;
                    diff[0] = math.difference(f, s);
                    return diff;
                })
                .orElse((Object[]) Array.newInstance(mathType, 0));
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "difference between " + first.toString(ctx, debug) + " and " + second.toString(ctx, debug);
    }
}
