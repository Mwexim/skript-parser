package io.github.syst3ms.skriptparser.types.conversions;

import java.util.Optional;
import java.util.function.Function;

/**
 * Utility functions regarding converters
 */
@SuppressWarnings("unchecked")
public class ConverterUtils {

    public static <F, T> Function<?, Optional<? extends T>> createInstanceofConverter(ConverterInfo<F, T> conv) {
        return createInstanceofConverter(conv.getFrom(), conv.getConverter());
    }

    public static <F, T> Function<?, Optional<? extends T>> createInstanceofConverter(Class<F> from, Function<? super F, Optional<? extends T>> conv) {
        return p -> Optional.ofNullable(p)
                .filter(from::isInstance)
                .flatMap(f -> conv.apply((F) f));
    }

    public static <F, T> Function<? super F, Optional<? extends T>> createInstanceofConverter(Function<? super F, Optional<? extends T>> conv, Class<T> to) {
        return p -> conv.apply(p)
                .filter(to::isInstance)
                .map(t -> (T) t);
    }

    public static <F, T> Function<?, Optional<? extends T>> createDoubleInstanceofConverter(ConverterInfo<F, ?> conv, Class<T> to) {
        return createDoubleInstanceofConverter(conv.getFrom(), (Function<F, Optional<?>>) conv.getConverter(), to);
    }

    public static <F, T> Function<?, Optional<? extends T>> createDoubleInstanceofConverter(Class<F> from, Function<? super F, Optional<?>> conv, Class<T> to) {
        return p -> Optional.ofNullable(p)
                .filter(from::isInstance)
                .flatMap(f -> conv.apply((F) f))
                .filter(to::isInstance)
                .map(t -> (T) t);
    }

}