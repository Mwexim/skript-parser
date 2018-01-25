package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.classes.PatternType;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A basic definition of a type. This doesn't handle number (single/plural), see {@link PatternType} for that.
 *
 */
public class Type<T> {
    public static final Predicate<String> pluralGroupChecker = Pattern.compile("(?<!\\\\)\\(\\?<plural>[a-zA-Z]+\\)").asPredicate();
    private Class<T> c;
    private String baseName;
    private Pattern syntaxPattern;
    private Function<String, ? extends T> literalParser;

    /**
     * Constructs a new Type. This consructor doesn't handle any exceptions inherent to the regex pattern.
     *
     * @param c the class this type represents
     * @param baseName the basic name to represent this type with. It should be more or less a lowercase version of the Java class.
     * @param pattern the regex pattern this type should match.
     *                Plural form should be contained in a group named {@literal plural}.
     *                If the name has an irregular plural (i.e "party" becomes "parties"), then use the following contruct : {@literal part(y|(?<plural>ies))}
     */
    public Type(Class<T> c, String baseName, String pattern) {
        this(c, baseName, pattern, null);
    }

    /**
     * Constructs a new Type. This consructor doesn't handle any exceptions inherent to the regex pattern.
     *
     * @param c the class this type represents
     * @param baseName the basic name to represent this type with. It should be more or less a lowercase version of the Java class.
     * @param pattern the regex pattern this type should match.
     *                Plural form should be contained in a group named {@literal plural}.
     *                If the name has an irregular plural (i.e "party" becomes "parties"), then use the following contruct : {@literal part(y|(?<plural>ies))}
     * @param literalParser the function that would parse literals for the given type. If the parser throws an exception on parsing, it will be
     *                      catched and the type will be ignored.
     */
    public Type(Class<T> c, String baseName, String pattern, Function<String, ? extends T> literalParser) {
        this.c = c;
        this.baseName = baseName;
        this.literalParser = literalParser;
        pattern = pattern.trim();
        // Not handling exceptions here, developer responsability
        if (!pluralGroupChecker.test(pattern)) {
            syntaxPattern = Pattern.compile(pattern + "(?<plural>)??"); // Lazy optional group is required in this case
        } else {
            syntaxPattern = Pattern.compile(pattern);
        }
    }

    public Function<String, ? extends T> getLiteralParser() {
        return literalParser;
    }

    public Pattern getSyntaxPattern() {
        return syntaxPattern;
    }

    public Class<T> getC() {
        return c;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Type)) {
            return false;
        } else {
            Type<?> o = (Type<?>) obj;
            return c.equals(o.c) && baseName.equals(o.baseName) && syntaxPattern.pattern().equals(o.syntaxPattern.pattern());
        }
    }

    @Override
    public int hashCode() {
        return syntaxPattern.pattern().hashCode();
    }

    public String getBaseName() {
        return baseName;
    }
}