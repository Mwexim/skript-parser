package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.expressions.arithmetic.Arithmetics;
import io.github.syst3ms.skriptparser.expressions.arithmetic.DifferenceInfo;
import io.github.syst3ms.skriptparser.expressions.arithmetic.ExprArithmetic;
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
    @SuppressWarnings("rawtypes")
    private DifferenceInfo differenceInfo;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, ParseContext parseContext) {
        Expression<?> first = exprs[0];
        Expression<?> second = exprs[1];

        Class<?> firstReturnType = first.getReturnType();
        Class<?> secondReturnType = second.getReturnType();
        Class<?> superType = ExprArithmetic.getSuperClass(firstReturnType, secondReturnType);

        boolean fail = false;

        if (superType == Object.class && (firstReturnType != Object.class || secondReturnType != Object.class)) {
            // We may not have a way to obtain the difference between these two values. Further checks needed.

            // These two types are unrelated, meaning conversion is needed
            if (firstReturnType != Object.class && secondReturnType != Object.class) {

                // We will work our way out of failure
                fail = true;

                // Attempt to use first type's math
                differenceInfo = Arithmetics.getDifferenceInfo(firstReturnType);
                if (differenceInfo != null) { // Try to convert second to first
                    Expression<?> secondConverted = second.convertExpression(firstReturnType).orElse(null);
                    if (secondConverted != null) {
                        second = secondConverted;
                        fail = false;
                    }
                }

                if (fail) { // First type won't work, try second type
                    differenceInfo = Arithmetics.getDifferenceInfo(secondReturnType);
                    if (differenceInfo != null) { // Try to convert first to second
                        Expression<?> firstConverted = first.convertExpression(secondReturnType).orElse(null);
                        if (firstConverted != null) {
                            first = firstConverted;
                            fail = false;
                        }
                    }
                }

            } else { // It may just be the case that the type of one of our values cannot be known at parse time
                Expression<?> converted;
                if (firstReturnType == Object.class) {
                    converted = first.convertExpression(secondReturnType).orElse(null);
                    if (converted != null) { // This may fail if both types are Object
                        first = converted;
                    }
                } else { // This is an else statement to avoid X->Object conversions
                    converted = second.convertExpression(firstReturnType).orElse(null);
                    if (converted != null) {
                        second = converted;
                    }
                }

                if (converted == null) { // It's unlikely that these two can be compared
                    fail = true;
                } else { // Attempt to resolve a better class info
                    superType = ExprArithmetic.getSuperClass(first.getReturnType(), second.getReturnType());
                }
            }

        }

        if (superType != Object.class && (differenceInfo = Arithmetics.getDifferenceInfo(superType)) == null) {
            fail = true;
        }

        if (fail) {
            parseContext.getLogger().error("Can't get the difference of " + properName(first) + " and " + properName(second), ErrorType.SEMANTIC_ERROR);
            return false;
        }

        this.first = first;
        this.second = second;

        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object[] getValues(TriggerContext ctx) {
        Object first = this.first.getSingle(ctx).orElse(null);
        Object second = this.second.getSingle(ctx).orElse(null);
        if (first == null || second == null) {
            return new Object[0];
        }

        DifferenceInfo differenceInfo = this.differenceInfo;
        if (differenceInfo == null) { // Try to determine now that actual types are known
            Class<?> superType = ExprArithmetic.getSuperClass(first.getClass(), second.getClass());
            differenceInfo = Arithmetics.getDifferenceInfo(superType);
            if (differenceInfo == null) { // User did something stupid, just return <none> for them
                return new Object[0];
            }
        }

        Object[] one = (Object[]) Array.newInstance(differenceInfo.getReturnType(), 1);

        one[0] = differenceInfo.getOperation().calculate(first, second);

        return one;
    }

    @Override
    public Class<?> getReturnType() {
        return differenceInfo == null ? Object.class : differenceInfo.getReturnType();
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "difference between " + first.toString(ctx, debug) + " and " + second.toString(ctx, debug);
    }

    public static String properName(final Expression<?> e) {
        if (e.getReturnType() == Object.class)
            return e.toString(null, false);
        return TypeManager.getByClass(e.getReturnType()).get().withIndefiniteArticle(false);
    }

}
