package io.github.syst3ms.skriptparser.classes

import java.util.Arrays

class Literal<T>(c: Class<T>, vararg values: T) : Expression<T> {
    private var values: Array<T>

    init {
        @Suppress("UNCHECKED_CAST")
        val array : Array<T> = java.lang.reflect.Array.newInstance(c, values.size) as Array<T>
        this.values = array
    }

    // This is only necessary here, as literals aren't registered per-say
    val isSingle: Boolean
        get() = values.size == 1

    override fun init(expressions: Array<Expression<out Any>>, matchedPattern: Int, parseResult: ParseResult) = true

    override fun getValues() = values

    override fun toString(debug: Boolean) = if (isSingle) {
        values[0].toString()
    } else {
        Arrays.toString(values) // For now TODO make a proper way of doing this
    }
}
