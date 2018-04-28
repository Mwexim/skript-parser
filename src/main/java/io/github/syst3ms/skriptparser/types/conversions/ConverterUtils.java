package io.github.syst3ms.skriptparser.types.conversions;

import java.util.function.Function;

@SuppressWarnings("unchecked")
public class ConverterUtils {

    public static <F, T> Function<?, ? extends T> createInstanceofConverter(Converters.ConverterInfo<F, T> conv) {
        return createInstanceofConverter(conv.getFrom(), (Function<F, T>) conv.getConverter());
    }

    public static <F, T> Function<?, ? extends T> createInstanceofConverter(Class<F> from, Function<F, T> conv) {
        return o -> {
            if (!from.isInstance(o))
                return null;
            return conv.apply((F) o);
        };
    }

    public static <F, T> Function<? super F, ? extends T> createInstanceofConverter(Function<F, ?> conv, Class<T> to) {
        return f -> {
            Object o = conv.apply(f);
            if (to.isInstance(o))
                return (T) o;
            return null;
        };
    }

    public static <F, T> Function<?, ? extends T> createDoubleInstanceofConverter(Converters.ConverterInfo<F, ?> conv, Class<T> to) {
        return createDoubleInstanceofConverter(conv.getFrom(), (Function<F, ?>) conv.getConverter(), to);
    }

    public static <F, T> Function<?, ? extends T> createDoubleInstanceofConverter(Class<F> from, Function<F, ?> conv, Class<T> to) {
        return o -> {
            if (!from.isInstance(o))
                return null;
            Object o2 = conv.apply((F) o);
            if (to.isInstance(o2))
                return (T) o2;
            return null;
        };
    }

}