package io.github.syst3ms.skriptparser.types.conversions;

import org.intellij.lang.annotations.MagicConstant;

import java.util.Optional;
import java.util.function.Function;

public final class ConverterInfo<F, T> {
    private final Class<F> from;
    private final Class<T> to;
    private final Function<? super F, Optional<? extends T>> converter;
    private final int flags;

    public ConverterInfo(Class<F> from, Class<T> to, Function<? super F, Optional<? extends T>> converter) {
        this(from, to, converter, Converters.ALL_CHAINING);
    }

    public ConverterInfo(Class<F> from, Class<T> to, Function<? super F, Optional<? extends T>> converter, @MagicConstant(intValues = {Converters.ALL_CHAINING, Converters.NO_LEFT_CHAINING, Converters.NO_RIGHT_CHAINING, Converters.NO_CHAINING}) int flags) {
        this.from = from;
        this.to = to;
        this.converter = converter;
        this.flags = flags;
    }

    public Class<F> getFrom() {
        return from;
    }

    public Class<T> getTo() {
        return to;
    }

    public Function<? super F, Optional<? extends T>> getConverter() {
        return converter;
    }

    public int getFlags() {
        return flags;
    }
}