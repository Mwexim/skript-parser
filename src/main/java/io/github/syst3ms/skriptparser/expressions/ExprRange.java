package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.comparisons.Comparator;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import io.github.syst3ms.skriptparser.types.ranges.RangeInfo;
import io.github.syst3ms.skriptparser.types.ranges.Ranges;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Returns a range of values between two endpoints. Types supported by default are integers and characters (length 1 strings).
 *
 * @name Range
 * @pattern range from %object% to %object%
 * @since ALPHA
 * @author Syst3ms
 */
public class ExprRange implements Expression<Object> {
    private Expression<?> from, to;
    private RangeInfo<?, ?> range;
    @Nullable
    private Comparator<?, ?> comparator;

    static {
        Main.getMainRegistration().addExpression(
                ExprRange.class,
                Object.class,
                false,
                "range from %object% to %object%"
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        from = expressions[0];
        to = expressions[1];
        range = Ranges.getRange(ClassUtils.getCommonSuperclass(from.getReturnType(), to.getReturnType()));
        comparator = Comparators.getComparator(from.getReturnType(), to.getReturnType());
        if (range == null) {
            SkriptLogger logger = parseContext.getLogger();
            logger.error("Cannot get a range between " + from.toString(null, logger.isDebug()) + " and " + from.toString(null, logger.isDebug()), ErrorType.SEMANTIC_ERROR);
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getValues(TriggerContext ctx) {
        Object f = from.getSingle(ctx);
        Object t = to.getSingle(ctx);
        if (f == null || t == null) {
            return new Object[0];
        }
        // This is safe... right ?
        if (comparator != null && ((Comparator<Object, Object>) comparator).apply(f, t).is(Relation.GREATER)) {
            return CollectionUtils.reverseArray((Object[]) ((BiFunction<? super Object, ? super Object, ?>) this.range.getFunction()).apply(t, f));
        } else {
            return (Object[]) ((BiFunction<? super Object, ? super Object, ?>) this.range.getFunction()).apply(f, t);
        }
    }

    @Override
    public Class<?> getReturnType() {
        return range.getTo();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "range from " + from.toString(ctx, debug) + " to " + to.toString(ctx, debug);
    }
}
