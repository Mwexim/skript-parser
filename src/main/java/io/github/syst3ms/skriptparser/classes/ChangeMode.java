package io.github.syst3ms.skriptparser.classes;

import io.github.syst3ms.skriptparser.event.TriggerContext;

/**
 * An enum representing how an expression <em>could</em> be changed
 *
 * @see io.github.syst3ms.skriptparser.effects.EffChange
 * @see io.github.syst3ms.skriptparser.lang.Expression#acceptsChange(ChangeMode)
 * @see io.github.syst3ms.skriptparser.lang.Expression#change(TriggerContext, Object[], ChangeMode)
 */
public enum ChangeMode {

    /**
     * Indicates that an expression is being set to one or more values
     */
    SET,

    /**
     * Indicates that one or more values are being added to an expression
     */
    ADD,

    /**
     * Indicates that one or more values are being removed from an expression
     */
    REMOVE,

    /**
     * Indicates that an expression is being deleted
     */
    DELETE,

    /**
     * Indicates that an expression is being reset to a default value, that is entirely dependant on what the expression
     * is. This is NOT equivalent to {@code DELETE}
     */
    RESET,

    /**
     * Indicates that one or more values that are being described by some given expression are being removed from the
     * expression. This is also NOT equivalent to {@code DELETE}. For example, one could use this change mode to express
     * that all values of a specific type be removed from a list of values
     */
    REMOVE_ALL
}
