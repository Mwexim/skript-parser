package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.attributes.Arithmetic;
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
    private TypeManager.IntersectionType intersectionType;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        first = expressions[0];
        second = expressions[1];
        if (first instanceof Variable<?> || second instanceof Variable<?>)
            return true;

        var info = TypeManager.getByIntersection(Arithmetic.class, first.getReturnType(), second.getReturnType());
        if (info.isEmpty()) {
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
        intersectionType = info.get();
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getValues(TriggerContext ctx) {
        return DoubleOptional.ofOptional(first.getSingle(ctx), second.getSingle(ctx))
                .mapToOptional((f, s) -> {
                    // If variables are used, the arithmetic field is not initialised.
                    if (intersectionType == null) {
                        var info = TypeManager.getByIntersection(Arithmetic.class, f.getClass(), s.getClass());
                        // If it's still null, then no difference can be found sadly...
                        if (info.isEmpty())
                            return null;
                        intersectionType = info.get();
                    }

                    // Convert the expressions to the intersection type
                    var firstConverted = intersectionType.convert(f);
                    var secondConverted = intersectionType.convert(s);
                    if (firstConverted.isEmpty() || secondConverted.isEmpty())
                        return null;
                    Arithmetic arithmetic = intersectionType.getType().getArithmetic().orElseThrow();
                    return new Object[] {arithmetic.difference(firstConverted.get(), secondConverted.get())};
                })
                .orElse(new Object[0]);
    }

    @Override
    public Class<?> getReturnType() {
        return intersectionType != null
                ? intersectionType.getType().getArithmetic().orElseThrow().getRelativeType()
                : Object.class;
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "difference between " + first.toString(ctx, debug) + " and " + second.toString(ctx, debug);
    }
}
