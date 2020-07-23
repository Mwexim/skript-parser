package io.github.syst3ms.skriptparser.types.conversions;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Used to chain converters to build a single converter. This is automatically created when a new converter is added.
 *
 * @author Peter Güttinger
 * @param <F> same as Converter's <F> (from)
 * @param <M> the middle type, i.e. the type the first converter converts to and the second converter comverts from.
 * @param <T> same as Converter's <T> (to)
 * @see Converters#registerConverter(Class, Class, Function)
 */
public final class ChainedConverter<F, M, T> implements Function<F, T> {
    private final Function<? super F, ? extends M> first;
    private final Function<? super M, ? extends T> second;

    public ChainedConverter(Function<? super F, ? extends M> first, Function<? super M, ? extends T> second) {
        this.first = first;
        this.second = second;
    }

    public static <F, M, T> ChainedConverter<F, M, T> newInstance(Function<? super F, ? extends M> first, Function<? super M, ? extends T> second) {
        return new ChainedConverter<>(first, second);
    }

    @Override
    @Nullable
    public T apply(@Nullable F f) {
        M m = first.apply(f);
        if (m == null)
            return null;
        return second.apply(m);
    }

}