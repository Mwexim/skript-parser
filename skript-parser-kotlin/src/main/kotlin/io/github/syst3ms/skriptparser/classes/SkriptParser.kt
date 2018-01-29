package io.github.syst3ms.skriptparser.classes

import io.github.syst3ms.skriptparser.pattern.*

/**
 * A parser instance used for matching a pattern to a syntax, stores a parse mark
 */
class SkriptParser(val element: PatternElement) {
    val pattern: String = element.toString()
    var patternIndex = 0
        private set
    val parsedExpressions = arrayListOf<Expression<*>>()
    val regexMatches = arrayListOf<MatchResult>()
    var parseMark = 0
        private set

    fun advanceInPattern() {
        patternIndex++
    }

    fun addExpression(expression: Expression<*>) {
        parsedExpressions += expression
    }

    fun addRegexMatch(match: MatchResult) {
        regexMatches.add(match)
    }

    fun addMark(mark: Int) {
        parseMark = parseMark xor mark
    }

    fun flatten(element: PatternElement): List<PatternElement> {
        val flattened = arrayListOf<PatternElement>()
        if (element is CompoundElement) {
            for (e in element.elements) {
                flattened.addAll(flatten(e))
            }
            return flattened
        } else {
            flattened.add(element)
            return flattened
        }
    }

    fun getPossibleInputs(elements: List<PatternElement>): List<PatternElement> {
        val possibilities = arrayListOf<PatternElement>()
        elementLoop@ for (element in elements) {
            when (element) {
                is TextElement, is RegexGroup -> {
                    if (element is TextElement && element.text.isBlank())
                        continue@elementLoop
                    possibilities.add(element)
                    return possibilities
                }
                is ChoiceGroup -> {
                    for ((choice) in element.choices) {
                        possibilities.addAll(getPossibleInputs(flatten(choice)))
                    }
                    return possibilities
                }
                is ExpressionElement -> { // Can't do much about this
                    possibilities.add(RegexGroup(".+".toRegex().toPattern()))
                    return possibilities
                }
                is OptionalGroup -> possibilities.addAll(getPossibleInputs(flatten(element.element)))
            }
        }
        possibilities.add(TextElement(""))
        return possibilities
    }

    fun parseExpression(s: String): Expression<*>? { // empty implementation
        return if (s == "2") object : Expression<Number> {
            override fun hashCode(): Int {
                return 2
            }
        } else null
    }
}
