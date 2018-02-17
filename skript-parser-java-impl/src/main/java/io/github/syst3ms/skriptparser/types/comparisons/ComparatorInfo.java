package io.github.syst3ms.skriptparser.types.comparisons;

public class ComparatorInfo<T1, T2> {
    private Class<T1> firstClass;
    private Class<T2> secondClass;
    private Comparator<T1, T2> comparator;

    public ComparatorInfo(final Class<T1> firstClass, final Class<T2> secondClass, final Comparator<T1, T2> comparator) {
        this.firstClass = firstClass;
        this.secondClass = secondClass;
        this.comparator = comparator;
    }

    public Class<?> getType(final boolean first) {
        return first ? getFirstClass() : getSecondClass();
    }

    public Class<T1> getFirstClass() {
        return firstClass;
    }

    public Class<T2> getSecondClass() {
        return secondClass;
    }

    public Comparator<T1, T2> getComparator() {
        return comparator;
    }
}
