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
import io.github.syst3ms.skriptparser.types.attributes.Range;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import io.github.syst3ms.skriptparser.util.DoubleOptional;

/**
 * Returns a range of values between two endpoints. Types supported by default are integers and characters (length 1 strings).
 *
 * @name Range
 * @pattern [the] range from %object% to %object%
 * @since ALPHA
 * @author Syst3ms
 */
public class ExprRange implements Expression<Object> {
    static {
        Parser.getMainRegistration().addExpression(
                ExprRange.class,
                Object.class,
                false,
                "[the] range from %object% to %object%"
        );
    }

    private Expression<?> from, to;
    private Range<?, ?> range;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        from = expressions[0];
        to = expressions[1];
        if (from instanceof Variable<?> || to instanceof Variable<?>)
            return true;

        range = TypeManager.getByClass(ClassUtils.getCommonSuperclass(from.getReturnType(), to.getReturnType())).flatMap(Type::getRange).orElse(null);
        if (range == null) {
            SkriptLogger logger = parseContext.getLogger();
            logger.error(
                    "Cannot get a range between "
                            + from.toString(TriggerContext.DUMMY, logger.isDebug())
                            + " and "
                            + to.toString(TriggerContext.DUMMY, logger.isDebug()),
                    ErrorType.SEMANTIC_ERROR);
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getValues(TriggerContext ctx) {
        return DoubleOptional.ofOptional(from.getSingle(ctx), to.getSingle(ctx))
                .mapToOptional((f, t) -> {
                    if (range == null) {
                        range = TypeManager.getByClass(ClassUtils.getCommonSuperclass(f.getClass(), t.getClass()))
                                .flatMap(Type::getRange)
                                .orElse(null);
                        // If it's still null, then no range can be found sadly...
                        if (range == null)
                            return null;
                    }

                    // This is safe... right?
                    if (Comparators.compare(f, t) == Relation.GREATER) {
                        return CollectionUtils.reverseArray(
                                ((Range<? super Object, ?>) this.range).apply(t, f)
                        );
                    } else {
                        return ((Range<? super Object, ?>) this.range).apply(f, t);
                    }
                })
                .orElse(new Object[0]);
    }

    @Override
    public Class<?> getReturnType() {
        return range != null
                ? range.getRelativeType()
                : ClassUtils.getCommonSuperclass(from.getReturnType(), to.getReturnType());
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return "range from " + from.toString(ctx, debug) + " to " + to.toString(ctx, debug);
    }
}
