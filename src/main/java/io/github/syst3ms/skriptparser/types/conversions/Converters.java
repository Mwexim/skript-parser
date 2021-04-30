package io.github.syst3ms.skriptparser.types.conversions;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.util.classes.Pair;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Peter Güttinger (Njol)
 */
public abstract class Converters {
    /**
     * A flag declaring a converter may freely be part of a {@link ChainedConverter}
     */
    public static final int ALL_CHAINING = 0;
    /**
     * A flag declaring a converter may only be the first part of a {@link ChainedConverter}
     */
    public static final int NO_LEFT_CHAINING = 1;
    /**
     * A flag declaring a converter may only be the second part of a {@link ChainedConverter}
     */
    public static final int NO_RIGHT_CHAINING = 2;
    /**
     * A flag declaring a converter may not be part of a {@link ChainedConverter}
     */
    public static final int NO_CHAINING = NO_LEFT_CHAINING | NO_RIGHT_CHAINING;

    private static final List<ConverterInfo<?, ?>> converters = new ArrayList<>(50);


    public static List<ConverterInfo<?, ?>> getConverters() {
        return Collections.unmodifiableList(converters);
    }


    @SuppressWarnings("unchecked")
    public static <F, T> void registerConverters(SkriptRegistration registration) {
        for (var info : registration.getConverters()) {
            var inf = (ConverterInfo<F, T>) info;
            registerConverter(
                    inf.getFrom(),
                    inf.getTo(),
                    inf.getConverter(),
                    inf.getFlags()
            );
        }
    }

    /**
     * Registers a converter.
     * @param from the class to convert from
     * @param to the class to convert to
     * @param converter the converter function
     */
    public static <F, T> void registerConverter(Class<F> from, Class<T> to, Function<? super F, Optional<? extends T>> converter) {
        registerConverter(from, to, converter, 0);
    }

	/**
	 * Registers a converter
	 * @param from the class to convert from
	 * @param to the class to convert to
	 * @param converter the converter function
	 * @param options the options
	 */
    public static <F, T> void registerConverter(Class<F> from, Class<T> to, Function<? super F, Optional<? extends T>> converter, int options) {
        if (converterExistsSlow(from, to))
            return;
        var info = new ConverterInfo<>(from, to, converter, options);
        for (var i = 0; i < converters.size(); i++) {
            var info2 = converters.get(i);
            if (info2.getFrom().isAssignableFrom(from) && to.isAssignableFrom(info2.getTo())) {
                converters.add(i, info);
                return;
            }
        }
        converters.add(info);
    }

    /**
     * Adds all possible {@link ChainedConverter}s to the current converters
     */
    public static void createMissingConverters() {
        for (var i = 0; i < converters.size(); i++) {
            var info = converters.get(i);
            for (var j = 0; j < converters.size(); j++) { // not from j = i+1 since new converters get added during the loops
                var info2 = converters.get(j);
                if ((info.getFlags() & NO_RIGHT_CHAINING) == 0 && (info2.getFlags() & NO_LEFT_CHAINING) == 0
                    && info2.getFrom().isAssignableFrom(info.getTo()) && !converterExistsSlow(info.getFrom(), info2.getTo())) {
                    converters.add(createChainedConverter(info, info2));
                } else if ((info.getFlags() & NO_LEFT_CHAINING) == 0 && (info2.getFlags() & NO_RIGHT_CHAINING) == 0
                    && info.getFrom().isAssignableFrom(info2.getTo()) && !converterExistsSlow(info2.getFrom(), info.getTo())) {
                    converters.add(createChainedConverter(info2, info));
                }
            }
        }
    }

    private static boolean converterExistsSlow(Class<?> from, Class<?> to) {
        for (var i : converters) {
            if ((i.getFrom().isAssignableFrom(from) || from.isAssignableFrom(i.getFrom())) && (i.getTo().isAssignableFrom(to) || to.isAssignableFrom(i.getTo()))) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"unchecked", "MagicConstant"})
    private static <F, M, T> ConverterInfo<F, T> createChainedConverter(ConverterInfo<?, ?> first, ConverterInfo<?, ?> second) {
        var firstInf = (ConverterInfo<F, M>) first;
        var secondInf = (ConverterInfo<M, T>) second;
        return new ConverterInfo<>(
                firstInf.getFrom(),
                secondInf.getTo(),
                ChainedConverter.newInstance(
                        firstInf.getConverter(),
                        secondInf.getConverter()
                ),
                first.getFlags() | second.getFlags()
        );
    }

    /**
	 * Converts the given value to the desired type. If you want to convert multiple values of the same type you should use {@link #getConverter(Class, Class)} to get a
	 * converter to convert the values.
	 * @param o the value to convert
	 * @param to the class to convert to
	 * @return the converted value or null if no converter exists or the converter returned null for the given value.
	 */
    @SuppressWarnings("unchecked")
    public static <F, T> Optional<? extends T> convert(@Nullable F o, Class<T> to) {
        if (o == null)
            return Optional.empty();
        if (to.isInstance(o))
            return Optional.of((T) o);
        return getConverter((Class<F>) o.getClass(), to)
                .flatMap(c -> c.apply(o));
    }

    /**
	 * Converts an object into one of the given types.
	 * This method does not convert the object if it is already an instance of any of the given classes.
	 * @param o the value to convert
	 * @param to the class to convert to
	 * @return the converted value
	 */
    @SuppressWarnings("unchecked")
    public static <F, T> Optional<? extends T> convert(@Nullable F o, Class<? extends T>[] to) {
        if (o == null)
            return Optional.empty();
        for (var t : to)
            if (t.isInstance(o))
                return Optional.of((T) o);
        var c = (Class<F>) o.getClass();
        for (var t : to) {
            @SuppressWarnings("null")
            var conv = getConverter(c, t).flatMap(co -> co.apply(o));
            if (conv.isPresent())
                return conv;
        }
        return Optional.empty();
    }

    /**
	 * Converts all entries in the given array to the desired type, using {@link #convert(Object, Class)} to convert every single value. If you want to convert an array of values
	 * of a known type, consider using {@link #convert(Object[], Class, Function)} for much better performance.
	 * @param o the values to convert
	 * @param to the class to convert to
	 * @return a T[] array without null elements
	 */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T[]> convertArray(@Nullable Object[] o, Class<T> to) {
        if (o == null)
            return Optional.empty();
        if (to.isAssignableFrom(o.getClass().getComponentType()))
            return Optional.of((T[]) o);
        List<T> l = new ArrayList<>(o.length);
        for (var e : o) {
            convert(e, to).ifPresent(l::add);
        }
        return Optional.of(l.toArray((T[]) Array.newInstance(to, l.size())));
    }

    /**
	 * Converts multiple objects into any of the given classes.
	 * @param o the values to convert
	 * @param to the class to convert to
	 * @param superType The component type of the returned array
	 * @return the converted array
	 */
    @SuppressWarnings("unchecked")
    public static <T> T[] convertArray(@Nullable Object[] o, Class<? extends T> to, Class<T> superType) {
        if (o == null)
            return (T[]) Array.newInstance(superType, 0);
        if (to.isAssignableFrom(o.getClass().getComponentType()))
            return (T[]) o;
        List<T> l = new ArrayList<>(o.length);
        for (var e : o) {
            convert(e, to).ifPresent(l::add);
        }
        return l.toArray((T[]) Array.newInstance(superType, l.size()));
    }

    private final static Map<Pair<Class<?>, Class<?>>, Function<?, ?>> convertersCache = new HashMap<>();

    /**
	 * Tests whether a converter between the given classes exists.
	 * @param from the class to convert from
	 * @param to the class to convert to
	 * @return whether a converter exists
	 */
    public static boolean converterExists(Class<?> from, Class<?> to) {
        return to.isAssignableFrom(from) || from.isAssignableFrom(to) || getConverter(from, to).isPresent();
    }

	/**
	 * Tests whether a converter between one of the given classes exists
	 * @param from the class to convert from
	 * @param to the class to convert to
	 * @return whether a converter exists
	 */
    public static boolean converterExists(Class<?> from, Class<?>... to) {
        for (var t : to) {
            assert t != null;
            if (converterExists(from, t))
                return true;
        }
        return false;
    }

    /**
	 * Gets a converter
	 * @param from the class to convert from
	 * @param to the class to convert to
	 * @return the converter or null if none exist
	 */
    @SuppressWarnings("unchecked")
    public static <F, T> Optional<? extends Function<? super F, Optional<? extends T>>> getConverter(Class<F> from, Class<T> to) {
        var p = new Pair<Class<?>, Class<?>>(from, to);
        if (convertersCache.containsKey(p)) // can contain null to denote nonexistence of a converter
            return Optional.ofNullable((Function<? super F, Optional<? extends T>>) convertersCache.get(p));
        var c = getConverterInternal(from, to);
        c.ifPresent(con -> convertersCache.put(p, con));
        return c;
    }

    @SuppressWarnings("unchecked")
    private static <F, T> Optional<Function<? super F, Optional<? extends T>>> getConverterInternal(Class<F> from, Class<T> to) {
    	for (var conv : converters) {
            if (conv.getFrom().isAssignableFrom(from) && to.isAssignableFrom(conv.getTo())) {
                var inf = (ConverterInfo<F, T>) conv;
                return Optional.ofNullable(inf.getConverter());
            }
        }
        for (var conv : converters) {
            if (conv.getFrom().isAssignableFrom(from) && conv.getTo().isAssignableFrom(to)) {
                var inf = (ConverterInfo<F, T>) conv;
                return Optional.of(ConverterUtils.createInstanceofConverter(inf.getConverter(), to));
            } else if (from.isAssignableFrom(conv.getFrom()) && to.isAssignableFrom(conv.getTo())) {
                var inf = (ConverterInfo<F, T>) conv;
                return Optional.of((Function<? super F, Optional<? extends T>>) ConverterUtils.createInstanceofConverter(inf));
            }
        }
        for (var conv : converters) {
            if (from.isAssignableFrom(conv.getFrom()) && conv.getTo().isAssignableFrom(to)) {
                return Optional.of((Function<? super F, Optional<? extends T>>) ConverterUtils.createDoubleInstanceofConverter(conv, to));
            }
        }
        if (from.isAssignableFrom(to) || to.isAssignableFrom(from))
            return Optional.of((Function<? super F, Optional<? extends T>>) ConverterUtils.createDoubleInstanceofConverter(from, Optional::ofNullable, to));
        return Optional.empty();
    }

    /**
	 * Converts a given array without checking the type.
	 * @param from the values to convert
	 * @param to the class to convert to
	 * @param converter the converter function
	 * @return the converted values
	 * @throws ArrayStoreException if the given class is not a superclass of all objects returned by the converter
	 */
    @SuppressWarnings("unchecked")
    public static <F, T> T[] convertUnsafe(F[] from, Class<?> to, Function<? super F, Optional<? extends T>> converter) {
        return convert(from, (Class<T>) to, converter);
    }

	/**
	 * Converts a given array.
	 * @param from the values to convert
	 * @param to the class to convert to
	 * @param converter the converter function
	 * @return the converted values
	 */
    public static <F, T> T[] convert(F[] from, Class<T> to, Function<? super F, Optional<? extends T>> converter) {
        @SuppressWarnings("unchecked")
        var ts = (T[]) Array.newInstance(to, from.length);
        var j = 0;
        for (var f : from) {
            var t = Optional.ofNullable(f).flatMap(converter::apply);
            if (t.isPresent())
                ts[j++] = t.get();
        }
        if (j != ts.length)
            ts = Arrays.copyOf(ts, j);
        return ts;
    }
}