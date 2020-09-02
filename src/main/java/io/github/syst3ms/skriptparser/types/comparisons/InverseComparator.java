package io.github.syst3ms.skriptparser.types.comparisons;

/**
 * A {@linkplain Comparator} that is the opposite of another one
 * @param <T1> the first type
 * @param <T2> the second type
 */
public class InverseComparator<T1, T2> extends Comparator<T1, T2> {
    private final Comparator<? super T2, ? super T1> comparator;

    public InverseComparator(Comparator<? super T2, ? super T1> comparator) {
        super(comparator.supportsOrdering());
        this.comparator = comparator;
    }

    @Override
    public Relation apply(T1 t1, T2 t2) {
        return comparator.apply(t2, t1).getSwitched();
    }
}
