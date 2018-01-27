package io.github.syst3ms.skriptparser.registration

import io.github.syst3ms.skriptparser.PatternParser
import io.github.syst3ms.skriptparser.classes.Expression
import io.github.syst3ms.skriptparser.pattern.PatternElement
import io.github.syst3ms.skriptparser.util.MultiMap

class SkriptRegistration(val registerer: String) {
    val expressions = MultiMap<Class<*>, ExpressionInfo<*, *>>()
    val effects = arrayListOf<SyntaxInfo<*>>()
    val types = arrayListOf<Type<*>>()
    private val patternParser = PatternParser()

    fun <C : Expression<T>, T> addExpression(c: Class<C>, returnType: Class<T>, vararg patterns: String) {
        val elements = arrayListOf<PatternElement>()
        patterns.mapNotNullTo(elements) { patternParser.parsePattern(it) }
        val t = TypeManager.getByClass(returnType) ?: //TODO error
                return
        val info = ExpressionInfo(c, elements, t)
        effects.add(info)
        expressions.putOne(returnType, info)
    }

    fun <C> addEffect(c: Class<C>, vararg patterns: String) {
        val elements = arrayListOf<PatternElement>()
        for (s in patterns) {
            elements.add(patternParser.parsePattern(s) ?: continue)
        }
        val info = SyntaxInfo(c, elements)
        effects.add(info)
    }

    fun <T> addType(c: Class<T>, name: String, pattern: String) {
        types.add(Type(c, name, pattern))
    }

    fun <T> addType(c: Class<T>, name: String, pattern: String, literalParser: ((String) -> T)?) {
        types.add(Type(c, name, pattern, literalParser))
    }

    fun register() {
        SyntaxManager.register(this)
        TypeManager.register(this)
    }
}
