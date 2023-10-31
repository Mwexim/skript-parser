package io.github.syst3ms.skriptparser.util;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Utility functions for Collection objects
 */
public class CollectionUtils {
    private static final Random rnd = new Random();

    @SafeVarargs
    public static <T> T[] arrayOf(T... values) {
        return values;
    }

    public static <T> T getRandom(T[] array) {
        return array[rnd.nextInt(array.length)];
    }

    public static <T> T[] reverseArray(T[] array) {
        for (var i = 0; i < array.length / 2; i++) {
            var temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
        return array;
    }

    @SafeVarargs
    public static <T> boolean contains(T[] array, T... contained) {
        return Arrays.asList(array).containsAll(Arrays.asList(contained));
    }

    /**
     * Find the index of an item, where a given predicate applies,
     * from a given class in an array, skipping over all these items
     * as long as the amount of items skipped is smaller than a given amount.
     * @param array the array
     * @param n the ordinal you want to get the index from
     * @param condition the condition
     * @return the index, {@code -1} if no index was found
     */
    public static <T> int ordinalConditionalIndexOf(T[] array, int n, Predicate<T> condition) {
        int index = 0;
        int findTimes = 0;
        if (n == 0)
            return -1;
        if (array.length == 0)
            return -1;
        for (T o : array) {
            if (condition.test(o))
                findTimes++;
            if (findTimes >= n)
                return index;
            index++;
        }
        return -1;
    }
}
