package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.types.ranges.RangeInfo;
import io.github.syst3ms.skriptparser.types.ranges.Ranges;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class ExprRange implements Expression<Object> {
    private Expression<?> from, to;
    private RangeInfo<?, ?> range;

    static {
        Main.getMainRegistration().addExpression(
                ExprRange.class,
                Object.class,
                false,
                "range from %object% to %object%"
        );
        Ranges.registerRange(
                Long.class,
                Long.class,
                (l, r) -> {
                    if (l.compareTo(r) >= 0) {
                        return new Long[0];
                    } else {
                        return LongStream.range(l, r + 1)
                                         .boxed()
                                         .toArray(Long[]::new);
                    }
                }
        );
        Ranges.registerRange(
                BigInteger.class,
                BigInteger.class,
                (l, r) -> {
                    if (l.compareTo(r) >= 0) {
                        return new BigInteger[0];
                    } else {
                        List<BigInteger> elements = new ArrayList<>();
                        BigInteger current = l;
                        do {
                            elements.add(current);
                            current = current.add(BigInteger.ONE);
                        } while (current.compareTo(r) <= 0);
                        return elements.toArray(new BigInteger[elements.size()]);
                    }
                }
        );
        // It's actually a character range
        Ranges.registerRange(
                String.class,
                String.class,
                (l, r) -> {
                    if (l.length() != 1 || r.length() != 1)
                        return new String[0];
                    char leftChar = l.charAt(0), rightChar = r.charAt(0);
                    return IntStream.range(leftChar, rightChar + 1)
                                    .mapToObj(i -> Character.toString((char) i))
                                    .toArray(String[]::new);
                }
        );
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        from = expressions[0];
        to = expressions[1];
        range = Ranges.getRange(ClassUtils.getCommonSuperclass(from.getReturnType(), to.getReturnType()));
        if (range == null) {
            // REMIND error
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getValues(Event e) {
        Object f = from.getSingle(e);
        Object t = to.getSingle(e);
        if (f == null || t == null) {
            return new Object[0];
        }
        // This is safe... right ?
        return (Object[]) ((BiFunction) range.getFunction()).apply(f, t);
    }

    @Override
    public Class<?> getReturnType() {
        return range.getTo();
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "range from " + from.toString(e, debug) + " to " + to.toString(e, debug);
    }
}
