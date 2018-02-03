package io.github.syst3ms.skriptparser.classes

interface SyntaxElement {
    fun init(expressions: Array<Expression<out Any>>, matchedPattern : Int, parseResult: ParseResult): Boolean

    fun toString(debug: Boolean): String
}
