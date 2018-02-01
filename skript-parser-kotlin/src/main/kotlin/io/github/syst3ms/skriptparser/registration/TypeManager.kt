package io.github.syst3ms.skriptparser.registration

import io.github.syst3ms.skriptparser.classes.PatternType

@Suppress("UNCHECKED_CAST")
/**
 * Manages all different types.
 */
object TypeManager {
    private val nameToType = mutableMapOf<String, Type<out Any>>()
    private val classToType: MutableMap<Class<in Any>, Type<out Any>> = mutableMapOf()

    /**
     * Gets a [Type] by its exact name (the baseName parameter used in [Type]'s constructor])
     * @param name the name to get the Type from
     * @return the corresponding Type, or null if nothing matched
     */
    fun getByExactName(name: String): Type<*>? {
        return nameToType[name]
    }

    /**
     * Gets a [Type] using [Type.syntaxPattern], which means this matches any alternate and/or plural form.
     * @param name the name to get a Type from
     * @return the matching Type, or null if nothing matched
     */
    fun getByName(name: String): Type<out Any>? {
        return nameToType.values.firstOrNull { it.syntaxPattern matches name }
    }

    /**
     * Gets a [Type] from its associated [Class].
     * @param c the Class to get the Type from
     * @return the associated Type, or null
     */
    fun getByClass(c: Class<in Any>): Type<out Any>? {
        return classToType[c]
    }

    /**
     * Gets a [PatternType] from a name. This determines the number (single/plural) from the input.
     * If the input happens to be the base name of a type, then a single PatternType (as in "not plural") of the corresponding type is returned.
     * @param name the name input
     * @return a corresponding PatternType, or null if nothing matched
     */
    fun getPatternType(name: String): PatternType<*>? {
        if (nameToType.containsKey(name)) { // Might as well avoid the for loop in this case
            return PatternType((nameToType[name] as Type<Any>?)!!, false)
        }
        for (t in nameToType.values) {
            val m = t.syntaxPattern.matchEntire(name)
            if (m != null) {
                return PatternType(t, m.groups["plural"] == null)
            }
        }
        return null
    }

    internal fun register(reg: SkriptRegistration) {
        for (type in reg.types) {
            nameToType[type.baseName] = type
            classToType[type.c as Class<in Any>] = type
        }
    }
}