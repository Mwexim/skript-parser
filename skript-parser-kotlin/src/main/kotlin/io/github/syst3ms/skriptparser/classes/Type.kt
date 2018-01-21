package io.github.syst3ms.skriptparser.classes

import java.util.function.Predicate
import java.util.regex.Pattern

/**
 * A basic definition of a type. This doesn't handle number (single/plural), see [PatternType] for that.
 * Constructs a new Type. This consructor doesn't handle any exceptions inherent to the regex pattern.
 *
 * @param c the class this type represents
 * @param baseName the basic name to represent this type with. It should be more or less a lowercase version of the Java class.
 * @param pattern the regex pattern this type should match.
 * Plural form should be contained in a group named plural.
 * If the name has an irregular plural (i.e "party" becomes "parties"), then use the following contruct : part(y|(?&lt;plural&gt;ies))
 * @param literalParser the function that would parse literals for the given type. If the parser throws an exception on parsing, it will be
 * catched and the type will be ignored.
 */
class Type<T>(val c: Class<in T>, val baseName: String, pattern: String, val literalParser: ((String) -> T)?) {
    var syntaxPattern: Pattern
        private set

    /**
     * Constructs a new Type. This consructor doesn't handle any exceptions inherent to the regex pattern.
     *
     * @param c the class this type represents
     * @param baseName the basic name to represent this type with. It should be more or less a lowercase version of the Java class.
     * @param pattern the regex pattern this type should match.
     * Plural form should be contained in a group named plural.
     * If the name has an irregular plural (i.e "party" becomes "parties"), then use the following contruct : part(y|(?&lt;plural&gt;ies))
     */
    constructor(c: Class<in T>, baseName: String, pattern: String) : this(c, baseName, pattern, null)

    init {
        var pat = pattern
        pat = pat.trim { it <= ' ' }
        // Not handling exceptions here, developer responsability
        syntaxPattern = if (!pluralGroupChecker.test(pat)) {
            Pattern.compile(pat + "(?<plural>)??") // Lazy optional group is required in this case
        } else {
            Pattern.compile(pat)
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is Type<*>) {
            false
        } else {
            c == other.c && baseName == other.baseName && syntaxPattern.pattern() == other.syntaxPattern.pattern()
        }
    }

    override fun hashCode() = syntaxPattern.pattern().hashCode()

    companion object {
        val pluralGroupChecker : Predicate<String> = Pattern.compile("(?<!\\\\)\\(\\?<plural>[a-zA-Z]+\\)").asPredicate()
    }
}