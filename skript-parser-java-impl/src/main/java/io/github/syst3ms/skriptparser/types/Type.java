package io.github.syst3ms.skriptparser.types;

import io.github.syst3ms.skriptparser.util.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

/**
 * A basic definition of a type. This doesn't handle number (single/plural), see {@link PatternType} for that.
 *
 */
public class Type<T> {
    private Class<T> typeClass;
    private String baseName;
    private String[] pluralForms;
    private Function<String, ? extends T> literalParser;
    private Function<Object, String> toStringFunction;

    /**
     * Constructs a new Type.
     *
     * @param typeClass the class this type represents
     * @param baseName the basic name to represent this type with. It should be more or less a lowercase version of the Java class.
     * @param pattern the pattern for plural forms. It's written in Skript aliases plural format. Examples :
     *                <ul>
     *                  <li>{@code fish} -> {@literal fish} (invariant)</li>
     *                  <li>{@code dog&brvbar;s} -> {@literal dog} and {@literal dogs}</li>
     *                  <li>{@code part&brvbar;y&brvbar;ies} -> {@literal party} and {@literal parties} (irregular plural)</li>
     *                </ul>
     */
    public Type(Class<T> typeClass, String baseName, String pattern) {
        this(typeClass, baseName, pattern, null);
    }

    /**
     * Constructs a new Type.
     *
     * @param typeClass the class this type represents
     * @param baseName the basic name to represent this type with. It should be more or less a lowercase version of the Java class.
     * @param pattern the pattern for plural forms. It's written in Skript aliases plural format. Examples :
     *                <ul>
     *                  <li>{@code fish} -> {@literal fish} (invariant)</li>
     *                  <li>{@code dog&brvbar;s} -> {@literal dog} and {@literal dogs}</li>
     *                  <li>{@code part&brvbar;y&brvbar;ies} -> {@literal party} and {@literal parties} (irregular plural)</li>
     *                </ul>
     * @param literalParser the function that would parse literals for the given type. If the parser throws an exception on parsing, it will be
     *                      caught and the type will be ignored.
     */
    public Type(Class<T> typeClass, String baseName, String pattern, Function<String, ? extends T> literalParser) {
        this(typeClass, baseName, pattern, literalParser, Objects::toString);
    }

    /**
     * Constructs a new Type.
     *
     * @param typeClass the class this type represents
     * @param baseName the basic name to represent this type with. It should be more or less a lowercase version of the Java class.
     * @param pattern the pattern for plural forms. It's written in Skript aliases plural format. Examples :
     *                <ul>
     *                  <li>{@code fish} -> {@literal fish} (invariant)</li>
     *                  <li>{@code dog&brvbar;s} -> {@literal dog} and {@literal dogs}</li>
     *                  <li>{@code part&brvbar;y&brvbar;ies} -> {@literal party} and {@literal parties} (irregular plural)</li>
     *                </ul>
     * @param literalParser the function that would parse literals for the given type. If the parser throws an exception on parsing, it will be
     *                      caught and the type will be ignored.
     * @param toStringFunction the functions that converts an object of the type {@link T} to a {@link String}.Defaults to {@link Objects#toString} for
     *                         other constructors.
     */
    @SuppressWarnings("unchecked")
    public Type(Class<T> typeClass, String baseName, String pattern, Function<String, ? extends T> literalParser, Function<? super T, String> toStringFunction) {
        this.typeClass = typeClass;
        this.baseName = baseName;
        this.literalParser = literalParser;
        this.toStringFunction = (Function<Object, String>) toStringFunction;
        this.pluralForms = StringUtils.getForms(pattern.trim());
    }

    public Function<String, ? extends T> getLiteralParser() {
        return literalParser;
    }

    public String[] getPluralForms() {
        return pluralForms;
    }

    public Class<T> getTypeClass() {
        return typeClass;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Type)) {
            return false;
        } else {
            Type<?> o = (Type<?>) obj;
            return typeClass.equals(o.typeClass) && baseName.equals(o.baseName) &&
                   Arrays.equals(pluralForms, o.pluralForms);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pluralForms);
    }

    public String getBaseName() {
        return baseName;
    }

    public Function<Object, String> getToStringFunction() {
        return toStringFunction;
    }
}