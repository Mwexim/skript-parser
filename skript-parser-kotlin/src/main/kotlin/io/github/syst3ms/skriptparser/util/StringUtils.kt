package io.github.syst3ms.skriptparser.util

fun String.getEnclosedText(opening: Char, closing: Char, start: Int = 0): String? {
    var n = 0
    var i = start
    while (i < length) {
        val c = this[i]
        if (c == '\\') {
            i++
        } else if (c == closing) {
            n--
            if (n == 0) {
                return substring(start + 1, i) // We don't want the beginning bracket in there
            }
        } else if (c == opening) {
            n++
        }
        i++
    }
    return null
}