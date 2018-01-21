package io.github.syst3ms.skriptparser.classes

import io.github.syst3ms.skriptparser.pattern.PatternElement

import java.util.ArrayList

open class SyntaxInfo<C>(val c: Class<C>, val patterns: List<PatternElement>)
