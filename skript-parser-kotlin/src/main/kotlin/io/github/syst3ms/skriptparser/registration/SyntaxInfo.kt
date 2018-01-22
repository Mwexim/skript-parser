package io.github.syst3ms.skriptparser.registration

import io.github.syst3ms.skriptparser.pattern.PatternElement

open class SyntaxInfo<C>(val c: Class<C>, val patterns: List<PatternElement>)
