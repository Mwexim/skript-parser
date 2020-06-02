/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package io.github.syst3ms.skriptparser.types.conversions;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
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
        for (ConverterInfo<?, ?> info : registration.getConverters()) {
            // Well, this is... fun
            registerConverter((Class<F>) info.getFrom(), (Class<T>) info.getTo(), (Function<F, T>) info.getConverter(), info.getFlags());
        }
    }

    /**
	 * Registers a converter.
	 *
	 * @param from
	 * @param to
	 * @param converter
	 */
    public static <F, T> void registerConverter(Class<F> from, Class<T> to, Function<F, T> converter) {
        registerConverter(from, to, converter, 0);
    }

    public static <F, T> void registerConverter(Class<F> from, Class<T> to, Function<F, T> converter, int options) {
        if (converterExistsSlow(from, to))
            return;
        ConverterInfo<F, T> info = new ConverterInfo<>(from, to, converter, options);
        for (int i = 0; i < converters.size(); i++) {
            ConverterInfo<?, ?> info2 = converters.get(i);
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
        for (int i = 0; i < converters.size(); i++) {
            ConverterInfo<?, ?> info = converters.get(i);
            for (int j = 0; j < converters.size(); j++) { // not from j = i+1 since new converters get added during the loops
                ConverterInfo<?, ?> info2 = converters.get(j);
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
        for (ConverterInfo<?, ?> i : converters) {
            if ((i.getFrom().isAssignableFrom(from) || from.isAssignableFrom(i.getFrom())) && (i.getTo().isAssignableFrom(to) || to.isAssignableFrom(i.getTo()))) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"unchecked", "MagicConstant"})
    private static <F, M, T> ConverterInfo<F, T> createChainedConverter(ConverterInfo<?, ?> first, ConverterInfo<?, ?> second) {
        return new ConverterInfo<>((Class<F>) first.getFrom(), (Class<T>) second.getTo(), ChainedConverter.newInstance((Function<F, M>) first.getConverter(), (Function<M, T>) second.getConverter()), first.getFlags() | second.getFlags());
    }

    /**
	 * Converts the given value to the desired type. If you want to convert multiple values of the same type you should use {@link #getConverter(Class, Class)} to get a
	 * converter to convert the values.
	 *
	 * @param o
	 * @param to
	 * @return The converted value or null if no converter exists or the converter returned null for the given value.
	 */
    @Nullable
    @SuppressWarnings("unchecked")
    @Contract("null, _, -> null")
    public static <F, T> T convert(@Nullable F o, Class<T> to) {
        if (o == null)
            return null;
        if (to.isInstance(o))
            return (T) o;
        Function<? super F, ? extends T> conv = getConverter((Class<F>) o.getClass(), to);
        if (conv == null)
            return null;
        return conv.apply(o);
    }

    /**
	 * Converts an object into one of the given types.
	 * <p>
	 * This method does not convert the object if it is already an instance of any of the given classes.
	 *
	 * @param o
	 * @param to
	 * @return The converted object
	 */
    @Nullable
    @SuppressWarnings("unchecked")
    @Contract("null, _, -> null")
    public static <F, T> T convert(@Nullable F o, Class<? extends T>[] to) {
        if (o == null)
            return null;
        for (Class<? extends T> t : to)
            if (t.isInstance(o))
                return (T) o;
        Class<F> c = (Class<F>) o.getClass();
        for (Class<? extends T> t : to) {
            @SuppressWarnings("null")
            Function<? super F, ? extends T> conv = getConverter(c, t);
            if (conv != null)
                return conv.apply(o);
        }
        return null;
    }

    /**
	 * Converts all entries in the given array to the desired type, using {@link #convert(Object, Class)} to convert every single value. If you want to convert an array of values
	 * of a known type, consider using {@link #convert(Object[], Class, Function)} for much better performance.
	 *
	 * @param o
	 * @param to
	 * @return A T[] array without null elements
	 */
    @Nullable
    @SuppressWarnings("unchecked")
    @Contract("null, _, -> null")
    public static <T> T[] convertArray(@Nullable Object[] o, Class<T> to) {
        if (o == null)
            return null;
        if (to.isAssignableFrom(o.getClass().getComponentType()))
            return (T[]) o;
        List<T> l = new ArrayList<>(o.length);
        for (Object e : o) {
            T c = convert(e, to);
            if (c != null)
                l.add(c);
        }
        return l.toArray((T[]) Array.newInstance(to, l.size()));
    }

    /**
	 * Converts multiple objects into any of the given classes.
	 *
	 * @param o
	 * @param to
	 * @param superType The component type of the returned array
	 * @return The converted array
	 */
    @SuppressWarnings("unchecked")
    public static <T> T[] convertArray(@Nullable Object[] o, Class<? extends T> to, Class<T> superType) {
        if (o == null) {
            return (T[]) Array.newInstance(superType, 0);
        }
        if (to.isAssignableFrom(o.getClass().getComponentType()))
            return (T[]) o;
        List<T> l = new ArrayList<>(o.length);
        for (Object e : o) {
            T c = convert(e, to);
            if (c != null)
                l.add(c);
        }
        return l.toArray((T[]) Array.newInstance(superType, l.size()));
    }

    private final static Map<Pair<Class<?>, Class<?>>, Function<?, ?>> convertersCache = new HashMap<>();

    /**
	 * Tests whether a converter between the given classes exists.
	 *
	 * @param from
	 * @param to
	 * @return Whether a converter exists
	 */
    public static boolean converterExists(Class<?> from, Class<?> to) {
        return to.isAssignableFrom(from) || from.isAssignableFrom(to) || getConverter(from, to) != null;
    }

    public static boolean converterExists(Class<?> from, Class<?>... to) {
        for (Class<?> t : to) {
            assert t != null;
            if (converterExists(from, t))
                return true;
        }
        return false;
    }

    /**
	 * Gets a converter
	 *
	 * @param from
	 * @param to
	 * @return the converter or null if none exist
	 */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <F, T> Function<? super F, ? extends T> getConverter(Class<F> from, Class<T> to) {
        Pair<Class<?>, Class<?>> p = new Pair<>(from, to);
        if (convertersCache.containsKey(p)) // can contain null to denote nonexistence of a converter
            return (Function<? super F, ? extends T>) convertersCache.get(p);
        Function<? super F, ? extends T> c = getConverter_i(from, to);
        convertersCache.put(p, c);
        return c;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <F, T> Function<? super F, ? extends T> getConverter_i(Class<F> from, Class<T> to) {
        for (ConverterInfo<?, ?> conv : converters) {
            if (conv.getFrom().isAssignableFrom(from) && to.isAssignableFrom(conv.getTo()))
                return (Function<? super F, ? extends T>) conv.getConverter();
        }
        for (ConverterInfo<?, ?> conv : converters) {
            if (conv.getFrom().isAssignableFrom(from) && conv.getTo().isAssignableFrom(to)) {
                return (Function<? super F, ? extends T>) ConverterUtils.createInstanceofConverter(conv.getConverter(), to);
            } else if (from.isAssignableFrom(conv.getFrom()) && to.isAssignableFrom(conv.getTo())) {
                return (Function<? super F, ? extends T>) ConverterUtils.createInstanceofConverter(conv);
            }
        }
        for (ConverterInfo<?, ?> conv : converters) {
            if (from.isAssignableFrom(conv.getFrom()) && conv.getTo().isAssignableFrom(to)) {
                return (Function<? super F, ? extends T>) ConverterUtils.createDoubleInstanceofConverter(conv, to);
            }
        }
        return null;
    }

    /**
	 * @param from
	 * @param to
	 * @param conv
	 * @return The converted array
	 * @throws ArrayStoreException if the given class is not a superclass of all objects returned by the converter
	 */
    @SuppressWarnings("unchecked")
    public static <F, T> T[] convertUnsafe(F[] from, Class<?> to, Function<? super F, ? extends T> conv) {
        return convert(from, (Class<T>) to, conv);
    }

    public static <F, T> T[] convert(F[] from, Class<T> to, Function<? super F, ? extends T> conv) {
        @SuppressWarnings("unchecked")
        T[] ts = (T[]) Array.newInstance(to, from.length);
        int j = 0;
        for (F f : from) {
            T t = f == null ? null : conv.apply(f);
            if (t != null) {
                ts[j++] = t;
            }
        }
        if (j != ts.length)
            ts = Arrays.copyOf(ts, j);
        return ts;
    }

}