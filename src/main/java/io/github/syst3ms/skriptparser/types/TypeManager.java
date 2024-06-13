package io.github.syst3ms.skriptparser.types;

import io.github.syst3ms.skriptparser.expressions.ExprDifference;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.conversions.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages the registration and usage of {@link Type}
 */
@SuppressWarnings("unchecked")
public class TypeManager {
    /**
     * The string equivalent of null
     */
    public static final String NULL_REPRESENTATION = "<none>";
    /**
     * The string equivalent of an empty array
     */
    public static final String EMPTY_REPRESENTATION = "<empty>";
    private static final List<Type<?>> types = new ArrayList<>();
    private static final Map<String, Type<?>> nameToType = new HashMap<>();
    private static final Map<Class<?>, Type<?>> classToType = new LinkedHashMap<>(); // Ordering is important for stuff like number types

    public static List<Type<?>> getTypeList() {
        return types;
    }

    public static Map<Class<?>, Type<?>> getClassToTypeMap() {
        return classToType;
    }

    /**
     * Gets a {@link Type} by its exact name (the baseName parameter used in {@link Type(Class, String, String)})
     * @param name the name to get the Type from
     * @return the corresponding Type, or {@literal null} if nothing matched
     */
    public static Optional<? extends Type<?>> getByExactName(String name) {
        return Optional.ofNullable(nameToType.get(name));
    }

    /**
     * Gets a {@link Type} using its plural forms, which means this matches any alternate and/or plural form.
     * @param name the name to get a Type from
     * @return the matching Type, or {@literal null} if nothing matched
     */
    public static Optional<? extends Type<?>> getByName(String name) {
        for (var t : nameToType.values()) {
            var forms = t.getPluralForms();
            if (name.equalsIgnoreCase(forms[0]) || name.equalsIgnoreCase(forms[1])) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets a {@link Type} from its associated {@link Class}.
     * @param c the Class to get the Type from
     * @param <T> the underlying type of the Class and the returned Type
     * @return the associated Type, or {@literal null}
     */
    public static <T> Optional<? extends Type<T>> getByClassExact(Class<T> c) {
        if (c.isArray())
            c = (Class<T>) c.getComponentType();
        return Optional.ofNullable((Type<T>) classToType.get(c));
    }

    public static <T> Optional<? extends Type<? super T>> getByClass(Class<T> c) {
        Optional<? extends Type<? super T>> type = getByClassExact(c);
        var superclass = c.getSuperclass();
        while (superclass != null && type.isEmpty()) {
            type = getByClass(superclass);
            superclass = superclass.getSuperclass();
        }
        var interf = (Class<? super T>[]) c.getInterfaces();
        var i = 0;
        while ((type.isEmpty() || type.filter(t -> t.getTypeClass() == Object.class).isPresent()) && i < interf.length) {
            type = getByClass(interf[i]);
            i++;
        }
        return type;
    }

    public static String toString(Object[] objects) {
        var sb = new StringBuilder();
        for (var i = 0; i < objects.length; i++) {
            if (i > 0) {
                sb.append(i == objects.length - 1 ? " and " : ", ");
            }
            var o = objects[i];
            if (o == null) {
                sb.append(NULL_REPRESENTATION);
                continue;
            }
            var type = getByClass(o.getClass());
            sb.append(type.map(t -> (Object) t.getToStringFunction().apply(o)).orElse(o));
        }
        return sb.length() == 0 ? EMPTY_REPRESENTATION : sb.toString();
    }

    /**
     * Gets a {@link PatternType} from a name. This determines the number (single/plural) from the input.
     * If the input happens to be the base name of a type, then a single PatternType (as in "not plural") of the corresponding type is returned.
     * @param name the name input
     * @return a corresponding PatternType, or {@literal null} if nothing matched
     */
    public static Optional<PatternType<?>> getPatternType(String name) {
        for (var t : nameToType.values()) {
            var forms = t.getPluralForms();
            if (name.equalsIgnoreCase(forms[0])) {
                return Optional.of(new PatternType<>(t, true));
            } else if (name.equalsIgnoreCase(forms[1])) {
                return Optional.of(new PatternType<>(t, false));
            }
        }
        return Optional.empty();
    }

    public static void register(SkriptRegistration reg) {
        for (var type : reg.getTypes()) {
            types.add(type);
            nameToType.put(type.getBaseName(), type);
            classToType.put(type.getTypeClass(), type);
        }
    }

    /**
     * If a certain set of types can all be converted to a single type, then that type is an intersection
     * type for that set of types. Given an array of classes, this method determines that intersection type,
     * or returns an empty optional if no such type was found.
     * @param classes the return types
     * @return an {@link IntersectionType}
     */
    public static Optional<IntersectionType> getByIntersection(Class<?>... classes) {
        for (var type : types) {
            var converters = Arrays.stream(classes)
                    .map(cls -> (Optional<?>) Converters.getConverter(cls, type.getTypeClass()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            // Check if we didn't lose some converters by the filter
            if (converters.size() == classes.length)
                return Optional.of(new IntersectionType(type, (List<Function<?, Optional<?>>>) converters));
        }
        return Optional.empty();
    }

    /**
     * If a certain set of types can all be converted to a single type, then that type is an intersection
     * type for that set of types. Given an array of classes, this method determines that intersection type,
     * or returns an empty optional if no such type was found.
     * In addition to this, this method will only consider types that have a certain attribute defined.
     * @param attributeClass the attribute class
     * @param classes the return types
     * @return an {@link IntersectionType}
     * @see ExprDifference#init(Expression[], int, ParseContext)
     */
    public static Optional<IntersectionType> getByIntersection(Class<? extends Type.Attribute> attributeClass,
                                                               Class<?>... classes) {
        for (var type : types) {
            var provided = type.getAttribute(attributeClass);
            if (provided.isPresent()) {
                var converters = Arrays.stream(classes)
                        .map(cls -> (Optional<?>) Converters.getConverter(cls, type.getTypeClass()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                // Check if we didn't lose some converters by the filter
                if (converters.size() == classes.length)
                    return Optional.of(new IntersectionType(type, (List<Function<?, Optional<?>>>) converters));
            }
        }
        return Optional.empty();
    }

    // TODO better generics?
    public static class IntersectionType {
        private final Type<?> type;
        private final List<Function<?, Optional<?>>> converters;
        private int currentIndex = 0;

        public IntersectionType(Type<?> type, List<Function<?, Optional<?>>> converters) {
            this.type = type;
            this.converters = converters;
        }

        public Type<?> getType() {
            return type;
        }

        public Function<Object, Optional<?>> getConverter(int index) {
            return (Function<Object, Optional<?>>) converters.get(index);
        }

        /**
         * Converts the next item, in the order specified in the
         * {@link #getByIntersection(Class[]) intersection} method.
         * @param toConvert the object to convert
         * @return the converted object
         */
        public Optional<?> convert(Object toConvert) {
            return ((Function<Object, Optional<?>>) converters.get(currentIndex++)).apply(toConvert);
        }
    }
}