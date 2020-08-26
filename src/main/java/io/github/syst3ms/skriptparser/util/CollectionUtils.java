package io.github.syst3ms.skriptparser.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Utility functions for Collection objects
 */
public class CollectionUtils {
    private static final Random rnd = new Random();

    @SafeVarargs
    public static <T> Iterator<T> iterator(T... elements) {
        return Arrays.asList(elements).iterator();
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

    /**
     * Find the Nth index of an item in a list, skipping over all the items,
     * as long as the amount of items skipped is smaller than N.
     * @param list the list
     * @param obj the object
     * @param n the Nth item you want the index from
     * @return the index, {@code -1} if no index was found
     */
    public static <T> int indexOfNth(List<T> list, T obj, int n) {
        int index = 0;
        int findTimes = 0;
        if (n == 0)
            return -1;
        if (list.isEmpty())
            return -1;
        for (T o : list) {
            if (o.equals(obj))
                findTimes++;
            if (findTimes >= n)
                return index;
            index++;
        }
        return -1;
    }
}
