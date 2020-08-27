package io.github.syst3ms.skriptparser.types.comparisons;

/**
 * A class containing information about a {@link Comparator}
 * @param <T1> the first type
 * @param <T2> the second type
 */
public class ComparatorInfo<T1, T2> {
    private final Class<T1> firstClass;
    private final Class<T2> secondClass;
    private final Comparator<T1, T2> comparator;

    public ComparatorInfo(Class<T1> firstClass, Class<T2> secondClass, Comparator<T1, T2> comparator) {
        this.firstClass = firstClass;
        this.secondClass = secondClass;
        this.comparator = comparator;
    }

    public Class<?> getType(boolean first) {
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
