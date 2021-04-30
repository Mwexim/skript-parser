package io.github.syst3ms.skriptparser.types.comparisons;

import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.classes.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A class handling registration and usage of {@link Comparator}s
 */
public class Comparators {
    public static final Comparator<Object, Object> EQUALS_COMPARATOR = new Comparator<>(false) {
        @Override
        public Relation apply(@Nullable Object o, @Nullable Object o2) {
            return Relation.get(Objects.equals(o, o2));
        }
    };

    private static final Collection<ComparatorInfo<?, ?>> comparators = new ArrayList<>();

    /**
     * Registers a {@link Comparator}.
     * @param t1 class of first type
     * @param t2 class of second type
     * @param c the comparator
     * @throws IllegalArgumentException if any given class is equal to <code>Object.class</code>
     */
    public static <T1, T2> void registerComparator(Class<T1> t1, Class<T2> t2, Comparator<T1, T2> c) {
        if (t1 == Object.class || t2 == Object.class)
            throw new IllegalArgumentException("You must not add a comparator for Objects");
        comparators.add(new ComparatorInfo<>(t1, t2, c));
    }

    @SuppressWarnings({"unchecked"})
    public static <F, S> Relation compare(@Nullable F o1, @Nullable S o2) {
        if (o1 == null || o2 == null)
            return Relation.NOT_EQUAL;
        var c = getComparator((Class<F>) o1.getClass(), (Class<S>) o2.getClass());
        return c.map(comp -> comp.apply(o1, o2)).orElse(Relation.NOT_EQUAL);
    }

    private final static java.util.Comparator<Object> javaComparator = (o1, o2) -> compare(o1, o2).getComparison();

    public static java.util.Comparator<Object> getJavaComparator() {
        return javaComparator;
    }

    private final static Map<Pair<Class<?>, Class<?>>, Comparator<?, ?>> comparatorsQuickAccess = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <F, S> Optional<? extends Comparator<? super F, ? super S>> getComparator(Class<F> f, Class<S> s) {
        var p = new Pair<Class<?>, Class<?>>(f, s);
        if (comparatorsQuickAccess.containsKey(p))
            return Optional.ofNullable((Comparator<? super F, ? super S>) comparatorsQuickAccess.get(p));
        var comp = getComparatorInternal(f, s);
        comp.ifPresent(c -> comparatorsQuickAccess.put(p, c));
        return comp;
    }

    @SuppressWarnings("unchecked")
    private static <F, S> Optional<? extends Comparator<? super F, ? super S>> getComparatorInternal(Class<F> f, Class<S> s) {
        // Perfect match
        for (var info : comparators) {
            if (info.getFirstClass().isAssignableFrom(f) && info.getSecondClass().isAssignableFrom(s)) {
                return Optional.ofNullable((Comparator<? super F, ? super S>) info.getComparator());
            } else if (info.getFirstClass().isAssignableFrom(s) && info.getSecondClass().isAssignableFrom(f)) {
                return Optional.of(new InverseComparator<>((Comparator<? super S, ? super F>) info.getComparator()));
            }
        }

        // Same class but no comparator
        if (f == s) {
            return Optional.of(EQUALS_COMPARATOR);
        }

        boolean[] trueFalse = {true, false};
        Optional<? extends Function<? super F, Optional<?>>> c1;
        Optional<? extends Function<? super S, Optional<?>>> c2;

        // Single conversion
        for (var info : comparators) {
            for (var first : trueFalse) {
                if (info.getType(first).isAssignableFrom(f)) {
                    c2 = (Optional<? extends Function<? super S, Optional<?>>>) Converters.getConverter(s, info.getType(!first));
                    if (c2.isPresent()) {
                        return Optional.of(first
                                ? new ConvertedComparator<>(info.getComparator(), c2.get())
                                : new InverseComparator<>(
                                        new ConvertedComparator<>(c2.get(), info.getComparator())
                                )
                        );
                    }
                }
                if (info.getType(first).isAssignableFrom(s)) {
                    c1 = (Optional<? extends Function<? super F, Optional<?>>>) Converters.getConverter(f, info.getType(!first));
                    if (c1.isPresent()) {
                        return Optional.of(!first
                                ? new ConvertedComparator<>(
                                        (Comparator<? super F, ?>) c1.get(),
                                        (Function<? super S, Optional<?>>) info.getComparator()
                                )
                                : new InverseComparator<>(
                                        new ConvertedComparator<>(info.getComparator(), c1.get())
                                )
                        );
                    }
                }
            }
        }

        // Double conversion
        for (var info : comparators) {
            for (var first : trueFalse) {
                c1 = (Optional<? extends Function<? super F, Optional<?>>>) Converters.getConverter(f, info.getType(first));
                c2 = (Optional<? extends Function<? super S, Optional<?>>>) Converters.getConverter(s, info.getType(!first));
                if (c1.isPresent() && c2.isPresent()) {
                    return Optional.of(first
                            ? new ConvertedComparator<>(c1.get(), info.getComparator(), c2.get())
                            : new InverseComparator<>(new ConvertedComparator<>(c2.get(), info.getComparator(), c1.get()))
                    );
                }
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("rawtypes")
    private final static class ConvertedComparator<T1, T2> extends Comparator<T1, T2> {
        private final Comparator c;
        @Nullable
        private final Function c1, c2;

        public ConvertedComparator(Function<? super T1, ?> c1, Comparator<?, ?> c) {
            super(c.supportsOrdering());
            this.c1 = c1;
            this.c = c;
            this.c2 = null;
        }

        public ConvertedComparator(Comparator<?, ?> c, Function<? super T2, ?> c2) {
            super(c.supportsOrdering());
            this.c1 = null;
            this.c = c;
            this.c2 = c2;
        }

        public ConvertedComparator(Function<? super T1, ?> c1, Comparator<?, ?> c, Function<? super T2, ?> c2) {
            super(c.supportsOrdering());
            this.c1 = c1;
            this.c = c;
            this.c2 = c2;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Relation apply(@Nullable T1 o1, @Nullable T2 o2) {
            var t1 = c1 == null ? Optional.ofNullable(o1) : (Optional<?>) c1.apply(o1);
            var t2 = c2 == null ? Optional.ofNullable(o2) : (Optional<?>) c2.apply(o2);
            if (t1.isEmpty() || t2.isEmpty())
                return Relation.NOT_EQUAL;
            return c.apply(t1.get(), t2.get());
        }

        @Override
        public String toString() {
            return "ConvertedComparator(" + c1 + "," + c + "," + c2 + ")";
        }

    }
}
