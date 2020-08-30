package io.github.syst3ms.skriptparser.util;

import java.util.Arrays;
import java.util.Iterator;
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
}
