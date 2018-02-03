package io.github.syst3ms.skriptparser.classes

import io.github.syst3ms.skriptparser.pattern.PatternElement
import java.util.regex.MatchResult

data class ParseResult(val element: PatternElement, val matches: List<MatchResult>, val parseMark: Int)
