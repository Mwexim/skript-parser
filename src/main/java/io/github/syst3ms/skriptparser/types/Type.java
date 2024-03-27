package io.github.syst3ms.skriptparser.types;

import io.github.syst3ms.skriptparser.types.changers.Arithmetic;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import io.github.syst3ms.skriptparser.types.changers.TypeSerializer;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A basic definition of a type. This doesn't handle number (single/plural), unlike {@link PatternType}
 * @see PatternType
 */
public class Type<T> {

    @Nullable
    private final Function<String, ? extends T> literalParser;

    @Nullable
    private final Changer<? super T> defaultChanger;

    @Nullable
    private final TypeSerializer<T> serializer;

    @Nullable
    private final Arithmetic<T, ?> arithmetic;

    private final Function<Object, String> toStringFunction;
    private final String[] pluralForms;
    private final Class<T> typeClass;
    private final String baseName;

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
    public Type(Class<T> typeClass,
                String baseName,
                String pattern,
                @Nullable Function<String, ? extends T> literalParser) {
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
    public Type(Class<T> typeClass,
                String baseName,
                String pattern,
                @Nullable Function<String, ? extends T> literalParser,
                Function<? super T, String> toStringFunction) {
        this(typeClass, baseName, pattern, literalParser, toStringFunction, null);
    }

    public Type(Class<T> typeClass,
                String baseName,
                String pattern,
                @Nullable Function<String, ? extends T> literalParser,
                Function<? super T, String> toStringFunction,
                @Nullable Changer<? super T> defaultChanger) {
        this(typeClass, baseName, pattern, literalParser, toStringFunction, defaultChanger, null);
    }

    public Type(Class<T> typeClass,
            String baseName,
            String pattern,
            @Nullable Function<String, ? extends T> literalParser,
            Function<? super T, String> toStringFunction,
            @Nullable Changer<? super T> defaultChanger,
            @Nullable Arithmetic<T, ?> arithmetic) {
        this(typeClass, baseName, pattern, literalParser, toStringFunction, defaultChanger, arithmetic, null);
    }

    @SuppressWarnings("unchecked")
    public Type(Class<T> typeClass,
                String baseName,
                String pattern,
                @Nullable Function<String, ? extends T> literalParser,
                Function<? super T, String> toStringFunction,
                @Nullable Changer<? super T> defaultChanger,
                @Nullable Arithmetic<T, ?> arithmetic, @Nullable TypeSerializer<T> serializer) {
        this.typeClass = typeClass;
        this.baseName = baseName;
        this.literalParser = literalParser;
        this.toStringFunction = (Function<Object, String>) toStringFunction;
        this.pluralForms = StringUtils.getForms(pattern.strip());
        this.defaultChanger = defaultChanger;
        this.arithmetic = arithmetic;
        this.serializer = serializer;
    }

    public Class<T> getTypeClass() {
        return typeClass;
    }

    public String getBaseName() {
        return baseName;
    }

    public String[] getPluralForms() {
        return pluralForms;
    }

    public Function<Object, String> getToStringFunction() {
        return toStringFunction;
    }

    public Optional<TypeSerializer<T>> getSerializer() {
        return Optional.ofNullable(serializer);
    }

    public Optional<Function<String, ? extends T>> getLiteralParser() {
        return Optional.ofNullable(literalParser);
    }

    public Optional<? extends Changer<? super T>> getDefaultChanger() {
        return Optional.ofNullable(defaultChanger);
    }

    public Optional<? extends Arithmetic<T, ?>> getArithmetic() {
        return Optional.ofNullable(arithmetic);
    }

    /**
     * Adds a proper English indefinite article to this type and applies the correct form.
     * @param plural whether this Type is plural or not
     * @return the applied form of this Type
     */
    public String withIndefiniteArticle(boolean plural) {
        return StringUtils.withIndefiniteArticle(pluralForms[plural ? 1 : 0], plural);
    }

    @Override
    public String toString() {
        return pluralForms[0];
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Type)) {
            return false;
        } else {
            var o = (Type<?>) obj;
            return typeClass.equals(o.typeClass) && baseName.equals(o.baseName) &&
                   Arrays.equals(pluralForms, o.pluralForms);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pluralForms);
    }
}