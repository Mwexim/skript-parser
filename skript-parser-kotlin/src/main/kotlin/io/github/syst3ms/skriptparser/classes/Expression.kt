package io.github.syst3ms.skriptparser.classes

interface Expression<T> : SyntaxElement {
    fun getValues() : Array<T>

    fun change() : Boolean = false
}
