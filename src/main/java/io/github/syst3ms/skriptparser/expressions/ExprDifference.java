package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.attributes.Arithmetic;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.DoubleOptional;
import org.jetbrains.annotations.Nullable;

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
    @Nullable
    private Arithmetic arithmetic;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        first = expressions[0];
        second = expressions[1];
        if (first instanceof Variable<?> || second instanceof Variable<?>)
            return true;

        arithmetic = TypeManager.getByClass(ClassUtils.getCommonSuperclass(first.getReturnType(), second.getReturnType())).flatMap(Type::getArithmetic).orElse(null);
        if (arithmetic == null) {
            SkriptLogger logger = parseContext.getLogger();
            parseContext.getLogger().error(
                    "Cannot get the difference between "
                            + first.toString(TriggerContext.DUMMY, logger.isDebug())
                            + " and "
                            + second.toString(TriggerContext.DUMMY, logger.isDebug()),
                    ErrorType.SEMANTIC_ERROR
            );
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getValues(TriggerContext ctx) {
        return DoubleOptional.ofOptional(first.getSingle(ctx), second.getSingle(ctx))
                .mapToOptional((f, s) -> {
                    // If variables are used, the arithmetic field is not initialised.
                    if (arithmetic == null) {
                        arithmetic = TypeManager.getByClass(ClassUtils.getCommonSuperclass(f.getClass(), s.getClass()))
                                .flatMap(Type::getArithmetic)
                                .orElse(null);
                        // If it's still null, then no difference can be found sadly...
                        if (arithmetic == null)
                            return null;
                    }
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
