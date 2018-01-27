package io.github.syst3ms.skriptparser.classes

import io.github.syst3ms.skriptparser.registration.Type

/**
 * A type used in a pattern.
 * Groups a [Type] and a number together (in contrast to [Type] itself)
 */
class PatternType<out T>(private val type: Type<out T>, private val isSingle: Boolean) {

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is PatternType<*>) {
            false
        } else {
            type == other.type && isSingle == other.isSingle
        }
    }

    override fun toString(): String { // Not perfect, but good enough for toString()
        val baseName = type.baseName
        return when {
            baseName.endsWith("child") -> // This exception seems likely enough
                baseName.replace("child", "children")
            ".*(s|ch|sh|x|z)".toRegex().matches(baseName) -> baseName + "es"
            else -> baseName + "s"
        }
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + isSingle.hashCode()
        return result
    }
}
