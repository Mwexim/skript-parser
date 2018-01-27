package io.github.syst3ms.skriptparser.classes

/**
 * Thrown whenever something inherent to parsing goes wrong
 */
class SkriptParserException(msg: String) : RuntimeException(msg) {
    companion object {
        private const val serialVersionUID = 0L
    }
}