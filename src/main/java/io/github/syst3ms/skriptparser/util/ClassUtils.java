package io.github.syst3ms.skriptparser.util;

public class ClassUtils {

    /**
     * @param cs the array of classes
     * @return the nearest common superclass of the provided classes, accounting for interfaces
     */
    public static Class<?> getCommonSuperclass(Class<?>... cs) {
        Class<?> r = cs[0];
        outer:
        for (Class<?> c : cs) {
            if (c.isAssignableFrom(r)) {
                r = c;
                continue;
            }
            if (!r.isAssignableFrom(c)) {
                Class<?> s = c;
                while ((s = s.getSuperclass()) != null) {
                    if (s != Object.class && s.isAssignableFrom(r)) {
                        r = s;
                        continue outer;
                    }
                }
                for (Class<?> i : c.getInterfaces()) {
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

    public static boolean containsSuperclass(Class<?>[] haystack, Class<?> needle) {
        for (Class<?> c : haystack) {
            if (c.isArray())
                c = c.getComponentType();
            if (c.isAssignableFrom(needle))
                return true;
        }
        return false;
    }
}
