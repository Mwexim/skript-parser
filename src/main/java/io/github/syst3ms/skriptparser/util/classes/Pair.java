package io.github.syst3ms.skriptparser.util.classes;

/**
 * A simple pair of two values.
 * @param <T> type of the first value
 * @param <U> type of the second value
 */
public class Pair<T, U> {
    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Retrieves the first element
     * @return the first element
     */
    public T getFirst() {
        return first;
    }

    /**
     * Retrieves the second element
     * @return the second element
     */
    public U getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var pair = (Pair<?, ?>) o;
        return first.equals(pair.first) &&
                second.equals(pair.second);
    }
}
