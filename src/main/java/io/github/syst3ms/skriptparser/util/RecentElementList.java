package io.github.syst3ms.skriptparser.util;

import io.github.syst3ms.skriptparser.registration.SyntaxInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A simple list that is only meant to keep track of which syntaxes are used frequently, in order to preemptively check
 * them against a string that's being parsed.
 *
 * To illustrate the behaviour of this class, imagine you use some syntax A 8 times, then use syntax B once. The very
 * next time the parser does the "recent syntaxes" check, it will check syntax A first, because it was used more than syntax B.
 * @param <T> the type of {@link SyntaxInfo}
 */
public class RecentElementList<T> implements Iterable<T> {
    /**
     * Suppose you use a bunch of different syntaxes in your script: they all get sorted properly in the frequency
     * hierarchy, and the "recent syntaxes" check works fine. But if there are many syntaxes in that list, then if one
     * wants to use a syntax one hasn't used before, it would take a lot of time to actually match the pattern against
     * it, since there's all the previously used syntaxes to check beforehand.
     *
     * Hence, the maximum number of recent elements is capped.
     */
    // Ideally this would be properly configurable. I'll let it be a constant for now.
    public static final int MAX_LIST_SIZE = 10;

    // Sorts entries by their value in decreasing order
    private static final Comparator<Map.Entry<?, Integer>> ENTRY_COMPARATOR = (f, s) -> s.getValue() - f.getValue();

    private final Map<T, Integer> backing = new HashMap<>();
    private final List<T> occurrences = new ArrayList<>();

    public RecentElementList() {}

    /**
     * Updates a given syntax's position inside of the frequency hierarchy. This is used to acknowledge that a given{@link SyntaxInfo}
     * has been successfully parsed, and should as such be part of the "recent syntaxes" check.
     * @param element the element to update
     */
    public void acknowledge(T element) {
        if (!occurrences.contains(element)) {
            if (occurrences.size() >= MAX_LIST_SIZE)
                return;
            occurrences.add(element);
            backing.put(element, 1);
        } else {
            for (var freq : backing.entrySet()) {
                if (freq.getKey().equals(element))
                    backing.put(freq.getKey(), freq.getValue() + 1);
            }
        }
    }

    /**
     * Merges the elements of this list and the elements of the other list into a new set.
     * The elements of this list have priority over the other elements. There will be
     * no duplicate elements in the returned collection.
     * @param other the other list
     * @return a merged set with the elements of both lists
     */
    public List<T> mergeWith(List<T> other) {
        List<T> merged = new ArrayList<>(occurrences);
        other.removeAll(occurrences);
        merged.addAll(other);
        return merged;
    }

    /**
     * Removes the elements of this {@link RecentElementList} from another {@link List}, in-place.
     * @param list the list to remove from
     */
    public void removeFrom(List<T> list) {
        list.removeAll(occurrences);
    }

    /**
     * Custom iterator sorted by frequency of use
     * @return an iterator where syntaxes appear in decreasing order of frequency of use
     */
    @NotNull
    @Override
    public Iterator<T> iterator() {
        var entries = new ArrayList<>(backing.entrySet());
        entries.sort(ENTRY_COMPARATOR);
        /*
         * Anonymous class because usual Iterator implementations check for concurrent modification, which we don't really
         * care about here. This shouldn't cause issues even if parallel parsing is implemented, because any reasonable
         * implementation would not use one RecentElementList across multiple threads. At least I hope so...
         */
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < entries.size();
            }

            @Override
            public T next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return entries.get(index++).getKey();
            }
        };
    }
}
