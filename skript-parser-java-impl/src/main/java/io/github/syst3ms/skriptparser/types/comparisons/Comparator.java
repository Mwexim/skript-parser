package io.github.syst3ms.skriptparser.types.comparisons;

import java.util.function.BiFunction;

public interface Comparator<T1, T2> extends BiFunction<T1, T2, Relation> {
    @Override
    Relation apply(T1 t1, T2 t2);

    boolean supportsOrdering();

}
