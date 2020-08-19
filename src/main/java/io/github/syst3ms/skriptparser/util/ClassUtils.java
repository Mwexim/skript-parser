package io.github.syst3ms.skriptparser.util;

/**
 * Utility functions for Class objects
 */
public class ClassUtils {

    /**
     * @param cs the array of classes
     * @return the nearest common superclass of the provided classes, accounting for interfaces
     */
    public static Class<?> getCommonSuperclass(Class<?>... cs) {
        var r = cs[0];
        outer:
        for (var c : cs) {
            if (c.isAssignableFrom(r)) {
                r = c;
                continue;
            }
            if (!r.isAssignableFrom(c)) {
                var s = c;
                while ((s = s.getSuperclass()) != null) {
                    if (s != Object.class && s.isAssignableFrom(r)) {
                        r = s;
                        continue outer;
                    }
                }
                for (var i : c.getInterfaces()) {
                    s = getCommonSuperclass(i, r);
                    if (s != Object.class) {
                        r = s;
                        continue outer;
                    }
                }
                return Object.class;
            }
        }
        return r;
    }

    /**
     * Checks whether an array of classes contains a superclass of the second class argument
     * @param haystack the array of classes to check
     * @param needle the class which may have a superclass inside of the array
     * @return whether the first argument contains a superclass of the second argument
     */
    public static boolean containsSuperclass(Class<?>[] haystack, Class<?> needle) {
        for (var c : haystack) {
            if (c.isArray())
                c = c.getComponentType();
            if (c.isAssignableFrom(needle))
                return true;
        }
        return false;
    }
}
