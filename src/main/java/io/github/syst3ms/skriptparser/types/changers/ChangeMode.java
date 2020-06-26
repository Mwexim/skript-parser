package io.github.syst3ms.skriptparser.types.changers;

import io.github.syst3ms.skriptparser.effects.EffChange;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;

/**
 * An enum representing how an expression <em>could</em> be changed
 *
 * @see EffChange
 * @see Expression#acceptsChange(ChangeMode)
 * @see Expression#change(TriggerContext, Object[], ChangeMode)
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
