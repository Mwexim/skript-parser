package io.github.syst3ms.skriptparser.types;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages all different types.
 */
@SuppressWarnings("unchecked")
public class TypeManager {
    public static final String NULL_REPRESENTATION = "<none>";
    public static final String EMPTY_REPRESENTATION = "<empty>";
    private static final TypeManager instance = new TypeManager();
    private static Map<String, Type<?>> nameToType = new HashMap<>();
    private static Map<Class<?>, Type<?>> classToType = new LinkedHashMap<>(); // Ordering is important for stuff like number types

    private TypeManager(){}

    public static Map<Class<?>, Type<?>> getClassToTypeMap() {
        return classToType;
    }

    /**
     * Gets a {@link Type} by its exact name (the baseName parameter used in {@link Type#Type(Class, String, String)})
     * @param name the name to get the Type from
     * @return the corresponding Type, or {@literal null} if nothing matched
     */
    public static Type<?> getByExactName(String name) {
        return nameToType.get(name);
    }

    /**
     * Gets a {@link Type} using {@link Type#pluralForms}, which means this matches any alternate and/or plural form.
     * @param name the name to get a Type from
     * @return the matching Type, or {@literal null} if nothing matched
     */
    public static Type<?> getByName(String name) {
        for (Type<?> t : nameToType.values()) {
            String[] forms = t.getPluralForms();
            if (name.equalsIgnoreCase(forms[0]) || name.equalsIgnoreCase(forms[1])) {
                return t;
            }
        }
        return null;
    }

    /**
     * Gets a {@link Type} from its associated {@link Class}.
     * @param c the Class to get the Type from
     * @param <T> the underlying type of the Class and the returned Type
     * @return the associated Type, or {@literal null}
     */
    public static <T> Type<T> getByClassExact(Class<T> c) {
        return (Type<T>) classToType.get(c);
    }

    public static <T> Type<? super T> getByClass(Class<T> c) {
        Type<? super T> type = getByClassExact(c);
        Class<? super T> superclass = c;
        while (superclass != null && type == null) {
            superclass = superclass.getSuperclass();
            type = getByClassExact(superclass);
        }
        return type;
    }

    public static String toString(Object... objects) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < objects.length; i++) {
            if (i > 0) {
                sb.append(i == objects.length - 1 ? " and " : ", ");
            }
            Object o = objects[i];
            if (o == null) {
                sb.append(NULL_REPRESENTATION);
                continue;
            }
            Type<?> type = getByClass(o.getClass());
            if (type == null) {
                sb.append(Objects.toString(o));
            } else {
                sb.append(type.getToStringFunction().apply(o));
            }
        }
        return sb.length() == 0 ? EMPTY_REPRESENTATION : sb.toString();
    }

    /**
     * Gets a {@link PatternType} from a name. This determines the number (single/plural) from the input.
     * If the input happens to be the base name of a type, then a single PatternType (as in "not plural") of the corresponding type is returned.
     * @param name the name input
     * @return a corresponding PatternType, or {@literal null} if nothing matched
     */
    public static PatternType<?> getPatternType(String name) {
        for (Type<?> t : nameToType.values()) {
            String[] forms = t.getPluralForms();
            if (name.equalsIgnoreCase(forms[0])) {
                return new PatternType<>(t, true);
            } else if (name.equalsIgnoreCase(forms[1])) {
                return new PatternType<>(t, false);
            }
        }
        return null;
    }

    public static void register(SkriptRegistration reg) {
        for (Type<?> type : reg.getTypes()) {
            nameToType.put(type.getBaseName(), type);
            classToType.put(type.getTypeClass(), type);
        }
    }
}