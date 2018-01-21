package io.github.syst3ms.skriptparser.classes

import io.github.syst3ms.skriptparser.pattern.PatternElement

/**
 * Represents something that returns something and has patterns which
 * may result it being matched
 */
class ExpressionInfo<C, T>(c: Class<C>, patterns: List<PatternElement>, val returnType: Type<T>) : SyntaxInfo<C>(c, patterns)