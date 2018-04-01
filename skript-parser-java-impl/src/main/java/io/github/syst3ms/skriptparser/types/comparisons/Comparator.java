package io.github.syst3ms.skriptparser.types.comparisons;

import java.util.function.BiFunction;

/**
 * An interface for comparing two values given types
 * @param <T1> the type of the first value
 * @param <T2> the type of the second value
 * @see Comparators
 */
public interface Comparator<T1, T2> extends BiFunction<T1, T2, Relation> {
    /**
     * @param t1 the first value
     * @param t2 the second value
     * @return the {@linkplain Relation relation} between the two values
     */
    @Override
    Relation apply(T1 t1, T2 t2);

    /**
     * @return whether this Comparator can be used to order values
     */
    boolean supportsOrdering();

}
