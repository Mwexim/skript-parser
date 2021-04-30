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
import io.github.syst3ms.skriptparser.util.classes.DoubleOptional;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The difference between two values.
 * Note that only values that can be checked for difference are allowed
 * (this is for example numbers, dates, durations and others).
 *
 * @name Difference
 * @pattern [the] difference (between|of) %object% and %object%
 * @since ALPHA
 * @author Mwexim
 */
public class ExprDifference implements Expression<Object> {
    static {
        Parser.getMainRegistration().addExpression(
                ExprDifference.class,
                Object.class,
                true,
                "[the] difference (between|of) %object% and %object%"
        );
    }

    private Expression<?> first, second;
    private boolean variablePresent;
    @SuppressWarnings("rawtypes")
    @Nullable
    private Arithmetic arithmetic;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        first = expressions[0];
        second = expressions[1];
        Optional<? extends Type<?>> type;

        if (first instanceof Literal<?> && second instanceof Literal<?>) {
            type = TypeManager.getByClass(
                    ClassUtils.getCommonSuperclass(first.getReturnType(), second.getReturnType())
            );
        } else if (first instanceof Variable<?> && second instanceof Variable<?>) {
            variablePresent = true;
            type = TypeManager.getByClassExact(Object.class);
        } else {
            // If the values are Literals, we want to use that to convert them as much as possible.
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

            // If one value is a Variable, we do not know its type at parse-time.
            if (first instanceof Variable<?>) {
                variablePresent = true;
                first = first.convertExpression(second.getReturnType()).orElseThrow();
                type = TypeManager.getByClass(second.getReturnType());
            } else if (second instanceof Variable<?>) {
                variablePresent = true;
                second = second.convertExpression(first.getReturnType()).orElseThrow();
                type = TypeManager.getByClass(first.getReturnType());
            } else {
                type = TypeManager.getByClass(
                        ClassUtils.getCommonSuperclass(first.getReturnType(), second.getReturnType())
                );
            }
        }

        assert type.isPresent();
        if (!variablePresent && type.get().getArithmetic().isEmpty()) {
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
        if (!variablePresent) {
            assert type.get().getArithmetic().isPresent();
            arithmetic = type.get().getArithmetic().get();
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getValues(TriggerContext ctx) {
        return DoubleOptional.ofOptional(first.getSingle(ctx), second.getSingle(ctx))
                .mapToOptional((f, s) -> {
                    // The arithmetic field isn't initialized here.
                    if (variablePresent) {
                        assert f.getClass() == s.getClass();
                        var type = TypeManager.getByClass(f.getClass()).orElseThrow();
                        var variableMath = type.getArithmetic();
                        if (variableMath.isEmpty())
                            return null;
                        arithmetic = variableMath.get();
                    }

                    assert arithmetic != null;
                    return new Object[] {arithmetic.difference(f, s)};
                })
                .orElse(new Object[0]);
    }

    @Override
    public Class<?> getReturnType() {
        return arithmetic != null
                ? arithmetic.getRelativeType()
                : ClassUtils.getCommonSuperclass(false, first.getReturnType(), second.getReturnType());
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "difference between " + first.toString(ctx, debug) + " and " + second.toString(ctx, debug);
    }
}
