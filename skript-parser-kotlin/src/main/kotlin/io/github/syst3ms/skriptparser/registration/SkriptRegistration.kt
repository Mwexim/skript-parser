package io.github.syst3ms.skriptparser.registration

import io.github.syst3ms.skriptparser.PatternParser
import io.github.syst3ms.skriptparser.classes.Expression
import io.github.syst3ms.skriptparser.pattern.PatternElement
import io.github.syst3ms.skriptparser.util.MultiMap

@Suppress("UNCHECKED_CAST")
class SkriptRegistration(val registerer: String) {
    val expressions = MultiMap<Class<out Any>, ExpressionInfo<out Expression<out Any>, Any>>()
    val effects = arrayListOf<SyntaxInfo<*>>()
    val types = arrayListOf<Type<out Any>>()
    private val patternParser = PatternParser()

    fun <C : Expression<out T>, T> addExpression(c: Class<C>, returnType: Class<T>, vararg patterns: String) {
        val elements = arrayListOf<PatternElement>()
        patterns.mapNotNullTo(elements) { patternParser.parsePattern(it) }
        val t = TypeManager.getByClass(returnType as Class<in Any>) ?: //TODO error
                return
        val info = ExpressionInfo(c, elements, t)
        expressions.putOne(returnType as Class<out Any>, info as ExpressionInfo<out Expression<out Any>, Any>)
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
        types.add(Type(c, name, pattern) as Type<out Any>)
    }

    fun <T> addType(c: Class<T>, name: String, pattern: String, literalParser: ((String) -> T?)?) {
        types.add(Type(c, name, pattern, literalParser) as Type<out Any>)
    }

    fun register() {
        SyntaxManager.register(this)
        TypeManager.register(this)
    }
}
