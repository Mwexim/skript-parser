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
        return getCommonSuperclass(true, cs);
    }

    /**
     * @param cs the array of classes
     * @param interfaces whether or not to account for interfaces
     * @return the nearest common superclass of the provided classes
     */
    public static Class<?> getCommonSuperclass(boolean interfaces, Class<?>... cs) {
        if (cs.length == 1)
            return cs[0];

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
                if (interfaces) {
                    for (var i : c.getInterfaces()) {
                        s = getCommonSuperclass(i, r);
                        if (s != Object.class) {
                            r = s;
                            continue outer;
                        }
                    }
                }
                return Object.class;
            }
        }
        return r;
    }
}
