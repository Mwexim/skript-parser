package io.github.syst3ms.skriptparser.registration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;

/**
 * Manages all different types.
 */
public class TypeManager {
    public static final String NULL_REPRESENTATION = "<none>";
    public static final String EMPTY_REPRESENTATION = "<empty>";
    private static final TypeManager instance = new TypeManager();
    private Map<String, Type<?>> nameToType = new HashMap<>();
    private Map<Class<?>, Type<?>> classToType = new HashMap<>();

    private TypeManager(){}

    public static TypeManager getInstance() {
        return instance;
    }

    public Map<Class<?>, Type<?>> getClassToTypeMap() {
        return classToType;
    }

    /**
     * Gets a {@link Type} by its exact name (the baseName parameter used in {@link Type#Type(Class, String, String)})
     * @param name the name to get the Type from
     * @return the corresponding Type, or {@literal null} if nothing matched
     */
    public Type<?> getByExactName(String name) {
        return nameToType.get(name);
    }

    /**
     * Gets a {@link Type} using {@link Type#syntaxPattern}, which means this matches any alternate and/or plural form.
     * @param name the name to get a Type from
     * @return the matching Type, or {@literal null} if nothing matched
     */
    public Type<?> getByName(String name) {
        for (Type<?> t : nameToType.values()) {
            Matcher m = t.getSyntaxPattern().matcher(name);
            if (m.matches()) {
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
    @SuppressWarnings("unchecked")
    public <T> Type<T> getByClassExact(Class<T> c) {
        return (Type<T>) classToType.get(c);
    }

    public <T> Type<? super T> getByClass(Class<T> c) {
        Type<? super T> type;
        do {
            Class<? super T> superclass = c.getSuperclass();
            type = getByClassExact(superclass);
        } while (type == null);
        return type;
    }

    public String toString(Object... objects) {
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
    public PatternType<?> getPatternType(String name) {
        if (nameToType.containsKey(name)) { // Might as well avoid the for loop in this case
            return new PatternType<>(nameToType.get(name), false);
        }
        for (Type<?> t : nameToType.values()) {
            Matcher m = t.getSyntaxPattern().matcher(name);
            if (m.matches()) {
                String pluralGroup = m.group("plural");
                return new PatternType<>(t, pluralGroup == null || pluralGroup.isEmpty());
            }
        }
        return null;
    }

    void register(SkriptRegistration reg) {
        for (Type<?> type : reg.getTypes()) {
            nameToType.put(type.getBaseName(), type);
            classToType.put(type.getTypeClass(), type);
        }
    }
}