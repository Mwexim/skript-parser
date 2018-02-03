package io.github.syst3ms.skriptparser.pattern

import io.github.syst3ms.skriptparser.classes.Expression
import io.github.syst3ms.skriptparser.classes.PatternType
import io.github.syst3ms.skriptparser.classes.SkriptParser
import io.github.syst3ms.skriptparser.util.getEnclosedText
import java.util.*
import java.util.regex.Pattern


/**
 * The superclass of all elements of a pattern.
 */
sealed class PatternElement {
    /**
     * Attemps to match the [PatternElement] to a string at a specified index.
     * About the index :
     *
     *  * When passing the index to other methods (i.e substring), make sure to always increment it by 1
     *  * When returning, never increment
     *
     * @param s the string to match this PatternElement against
     * @param index the index of the string at which this PatternElement should be matched
     * @return the index at which the matching should continue afterwards if successful. Otherwise, -1
     */
    abstract fun match(s: String, index: Int, parser: SkriptParser): Int
}

/**
 * An element of a choice group.
 * Consists of a [PatternElement] and a parse mark (defaults to 0)
 */
data class ChoiceElement(val element: PatternElement, val parseMark: Int) {
    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is ChoiceElement) {
            false
        } else {
            element == other.element && parseMark == other.parseMark
        }
    }

    override fun hashCode(): Int {
        var result = element.hashCode()
        result = 31 * result + parseMark
        return result
    }
}

/**
 * Multiple [PatternElement]s put together in order.
 */
class CompoundElement(val elements: List<PatternElement>) : PatternElement() {

    /**
     * Only used for unit tests
     */
    constructor(vararg elements: PatternElement) : this(Arrays.asList<PatternElement>(*elements)) {}

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is CompoundElement) {
            false
        } else {
            elements == other.elements
        }
    }

    override fun match(s: String, index: Int, parser: SkriptParser): Int {
        var i = index
        for (element in elements) {
            if (parser.element == this)
                parser.advanceInPattern()
            val m = element.match(s, i, parser)
            if (m == -1) {
                return -1
            }
            i = m
        }
        return i
    }

    override fun toString() = elements.joinToString() { it.toString() }

    override fun hashCode() = elements.hashCode()
}

/**
 * A group of multiple choices, consisting of multiple [ChoiceElement]
 */
class ChoiceGroup(val choices: List<ChoiceElement>) : PatternElement() {

    /**
     * Only used in unit tests
     */
    constructor(vararg choices: ChoiceElement) : this(Arrays.asList<ChoiceElement>(*choices))

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is ChoiceGroup) {
            false
        } else {
            choices == other.choices
        }
    }

    override fun match(s: String, index: Int, parser: SkriptParser): Int {
        if (parser.element == this)
            parser.advanceInPattern()
        for (choice in choices) {
            val m = choice.element.match(s, index, parser)
            if (m != -1) {
                parser.addMark(choice.parseMark)
                return m
            }
        }
        return -1
    }

    override fun toString(): String {
        val joiner = StringJoiner("|", "(", ")")
        for (choice in choices) {
            if (choice.parseMark != 0) {
                joiner.add(choice.parseMark.toString() + "¦" + choice.element.toString())
            } else {
                joiner.add(choice.element.toString())
            }
        }
        return joiner.toString()
    }

    override fun hashCode(): Int = choices.hashCode()
}

/**
 * A variable/expression, declared in syntax using %type%
 * Has :
 *
 *  * a [List] of [PatternType]
 *  * a field determining what type of values this expression accepts : literals, expressions or both (%*type%, %~type% and %type% respectively)
 *
 */
class ExpressionElement(private val types: List<PatternType<*>>, private val acceptance: Acceptance, private val nullable: Boolean = false) : PatternElement() {

    override fun match(s: String, index: Int, parser: SkriptParser): Int {
        if (parser.element == this)
            parser.advanceInPattern()
        if (s[index] == '(') {
            val enclosed = s.getEnclosedText('(', ')', index)
            if (enclosed != null) {
                val expression = parser.parseExpression(enclosed)
                if (expression != null) {
                    parser.addExpression(expression)
                    return index + enclosed.length
                }
            }
        }
        val flattened = parser.flatten(parser.element)
        val possibleInputs = parser.getPossibleInputs(flattened.subList(parser.patternIndex, flattened.size))
        inputLoop@ for (possibleInput in possibleInputs) {
            when (possibleInput) {
                is TextElement -> {
                    val text = possibleInput.text
                    if (text == "") { // End of line
                        val toParse = s.substring(index)
                        val expression = parser.parseExpression(toParse) ?: return -1
                        parser.addExpression(expression)
                        return index + toParse.length
                    }
                    val i = s.indexOf(text, index)
                    if (i == -1)
                        continue@inputLoop
                    val toParse = s.substring(index, i).trim { it <= ' ' }
                    val expression = parser.parseExpression(toParse) ?: continue@inputLoop
                    parser.addExpression(expression)
                    return index + toParse.length
                }
                is RegexGroup -> {
                    val m = possibleInput.pattern.findAll(s, index)
                    for (result in m) {
                        val toParse = s.substring(result.range)
                        val expression = parser.parseExpression(toParse) ?: continue@inputLoop
                        parser.addExpression(expression)
                        return index + toParse.length
                    }
                }
            }
        }
        return -1
    }

    enum class Acceptance {
        BOTH,
        EXPRESSIONS_ONLY,
        LITERALS_ONLY
    }


    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is ExpressionElement) {
            false
        } else {
            types == other.types && acceptance == other.acceptance
        }
    }

    override fun toString(): String {
        val sb = StringBuilder("%")
        if (nullable)
            sb.append('-')
        when (acceptance) {
            Acceptance.EXPRESSIONS_ONLY -> sb.append('~')
            Acceptance.LITERALS_ONLY -> sb.append('*')
            else -> {}
        }
        sb.append(types.joinToString(separator = "/"))
        return sb.append("%").toString()
    }

    override fun hashCode(): Int {
        var result = types.hashCode()
        result = 31 * result + acceptance.hashCode()
        return result
    }
}

/**
 * A group containing an optional [PatternElement], that can be omitted
 */
class OptionalGroup(val element: PatternElement) : PatternElement() {

    override fun equals(other: Any?) = other != null && other is OptionalGroup && element == other.element

    override fun match(s: String, index: Int, parser: SkriptParser): Int {
        if (parser.element == this)
            parser.advanceInPattern()
        val m = element.match(s, index, parser)
        return if (m != -1) m else index
    }

    override fun toString() = "[$element]"

    override fun hashCode() = element.hashCode()
}

/**
 * A group containing a regex in the form of a [Pattern].
 */
class RegexGroup(val pattern: Regex) : PatternElement() {

    override fun equals(other: Any?) =
            other != null && other is RegexGroup && pattern.pattern == other.pattern.pattern

    override fun match(s: String, index: Int, parser: SkriptParser): Int {
        if (parser.element == this)
            parser.advanceInPattern()
        val m = pattern.find(s, index) ?: return -1
        parser.addRegexMatch(m)
        return index + m.value.length
    }

    override fun toString(): String = "<${pattern.pattern}>"

    override fun hashCode(): Int = pattern.hashCode()
}

/**
 * Text inside of a pattern. Is case and whitespace insensitive.
 */
class TextElement(val text: String) : PatternElement() {

    override fun equals(other: Any?): Boolean = other != null && other is TextElement && text.equals(other.text, ignoreCase = true)

    override fun match(s: String, index: Int, parser: SkriptParser): Int {
        var i = index
        if (parser.element == this)
            parser.advanceInPattern()
        val trimmed = text.trim { it <= ' ' }
        while (i < s.length && s[i] == ' ') { // Hopefully fix some spacing issues
            i++
        }
        if (i + trimmed.length > s.length) {
            return -1
        }
        val substr = s.substring(i, i + trimmed.length)
        return if (substr.equals(trimmed, ignoreCase = true)) {
            index + text.length // Let's not forget the spaces we removed earlier
        } else {
            -1
        }
    }

    override fun toString(): String = text

    override fun hashCode(): Int = text.hashCode()
}