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
        return o -> Optional.ofNullable(o)
                .filter(from::isInstance)
                .flatMap(p -> conv.apply((F) p));
    }

    public static <F, T> Function<? super F, Optional<? extends T>> createInstanceofConverter(Function<? super F, Optional<?>> conv, Class<T> to) {
        return f -> conv.apply(f)
                .filter(to::isInstance)
                .map(p -> (T) p);
    }

    public static <F, T> Function<?, Optional<? extends T>> createDoubleInstanceofConverter(ConverterInfo<F, ?> conv, Class<T> to) {
        return createDoubleInstanceofConverter(conv.getFrom(), (Function<? super F, Optional<?>>) conv.getConverter(), to);
    }

    public static <F, T> Function<?, Optional<? extends T>> createDoubleInstanceofConverter(Class<F> from, Function<? super F, Optional<?>> conv, Class<T> to) {
        return o -> Optional.ofNullable(o)
                .filter(from::isInstance)
                .flatMap(p -> conv.apply((F) p))
                .filter(to::isInstance)
                .map(p -> (T) p);
    }

}