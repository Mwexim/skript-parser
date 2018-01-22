package io.github.syst3ms.skriptparser.registration

import io.github.syst3ms.skriptparser.util.MultiMap

object SyntaxManager {
    private val expressions = MultiMap<Class<*>, ExpressionInfo<*, *>>()
    private val syntaxes = MultiMap<String, SyntaxInfo<*>>()
    private val effects = arrayListOf<SyntaxInfo<*>>()

    val allExpressions: Iterable<ExpressionInfo<*, *>>
        get() = expressions.allValues

    internal fun register(reg: SkriptRegistration) {
        for (info in reg.effects) {
            effects.add(info)
            syntaxes.putOne(reg.registerer, info)
        }
        for ((key, infos) in reg.expressions) {
            for (info in infos) {
                expressions.putOne(key, info)
                syntaxes.putOne(reg.registerer, info)
            }
        }
    }

    fun getAddonSyntaxes(name: String) = syntaxes[name] ?: emptyList<SyntaxInfo<*>>()

    fun <T> getExpressionsByReturnType(c: Class<out T>): List<ExpressionInfo<*, *>> {
        val infos = arrayListOf<ExpressionInfo<*, *>>()
        for (returnType in expressions.keys) {
            if (returnType.isAssignableFrom(c)) {
                infos.addAll(expressions[returnType]!!)
            }
        }
        return infos
    }
}
