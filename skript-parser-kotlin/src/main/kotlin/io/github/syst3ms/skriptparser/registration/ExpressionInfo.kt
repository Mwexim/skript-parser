package io.github.syst3ms.skriptparser.registration

import io.github.syst3ms.skriptparser.classes.Expression
import io.github.syst3ms.skriptparser.pattern.PatternElement

/**
 * Represents something that returns something and has patterns which
 * may result it being matched
 */
class ExpressionInfo<C : Expression<out T>, out T>(c: Class<C>, patterns: List<PatternElement>, val returnType: Type<out T>) : SyntaxInfo<C>(c, patterns)