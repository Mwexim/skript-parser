package io.github.syst3ms.skriptparser.types.ranges;

import java.util.function.BiFunction;

public class RangeInfo<B, T> {
    private final Class<B> bound;
    private final Class<T> to;
    private final BiFunction<? super B, ? super B, T[]> function;

    public RangeInfo(Class<B> bound, Class<T> to, BiFunction<? super B, ? super B, T[]> function) {
        this.bound = bound;
        this.to = to;
        this.function = function;
    }

    public Class<B> getBound() {
        return bound;
    }

    public Class<T> getTo() {
        return to;
    }

    public BiFunction<? super B, ? super B, T[]> getFunction() {
        return function;
    }
}
