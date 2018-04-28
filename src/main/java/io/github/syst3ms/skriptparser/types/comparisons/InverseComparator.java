package io.github.syst3ms.skriptparser.types.comparisons;

public class InverseComparator<T1, T2> extends Comparator<T1, T2> {
    private Comparator<? super T2, ? super T1> comparator;

    public InverseComparator(Comparator<? super T2, ? super T1> comparator) {
        super(comparator.supportsOrdering());
        this.comparator = comparator;
    }

    @Override
    public Relation apply(T1 t1, T2 t2) {
        return comparator.apply(t2, t1).getSwitched();
    }
}
