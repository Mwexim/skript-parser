package io.github.syst3ms.skriptparser.types.ranges;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Ranges {
    private static Map<Class<?>, RangeInfo<?, ?>> rangeMap = new HashMap<>();

    public static <B, T> void registerRange(Class<B> bound, Class<T> to, BiFunction<? super B, ? super B, T[]> function) {
        rangeMap.put(
                bound,
                new RangeInfo<>(bound, to, function)
        );
    }

    @SuppressWarnings("unchecked")
    public static <B, T> RangeInfo<B, T> getRange(Class<B> bound) {
        for (Class<?> c : rangeMap.keySet()) {
            if (c == bound || c.isAssignableFrom(bound)) {
                return (RangeInfo<B, T>) rangeMap.get(c);
            }
        }
        return null;
    }
}
