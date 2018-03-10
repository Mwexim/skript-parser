package io.github.syst3ms.skriptparser.util;

import java.util.*;

public class CollectionUtils {
    private static final Random rnd = new Random();

    @SafeVarargs
    public static <T> Iterator<T> iterator(T... elements) {
        return Arrays.asList(elements).iterator();
    }

    public static <T> T getRandom(T[] array) {
        return array[rnd.nextInt(array.length)];
    }
}
