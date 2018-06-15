package io.github.syst3ms.skriptparser.classes;

import io.github.syst3ms.skriptparser.event.TriggerContext;

/**
 * An enum representing how an expression <em>could</em> be changed :
 * <ul>
 *     <li>{@code SET} : an expression is being set to one or more values</li>
 *     <li>{@code ADD} : one or more values are being added to an expression</li>
 *     <li>{@code REMOVE} : one or more values are being removed from an expression</li>
 *     <li>{@code DELETE} : an expression is being deleted.</li>
 *     <li>{@code RESET} : an expression is being reset to a default value, that is entirely dependant on what the expression is.<br>
 *     This is NOT equivalent to {@code DELETE}</li>
 *     <li>{@code REMOVE_ALL} : one or more values that are being described by some given expression are being removed from the expression.
 *     This is also NOT equivalent to {@code DELETE}.<br>
 *     For example, one could use this change mode to express that all values of a specific type be removed from a list of values</li>
 * </ul>
 * @see io.github.syst3ms.skriptparser.effects.EffChange
 * @see io.github.syst3ms.skriptparser.lang.Expression#acceptsChange(ChangeMode)
 * @see io.github.syst3ms.skriptparser.lang.Expression#change(TriggerContext, Object[], ChangeMode)
 */
public enum ChangeMode {
    SET, ADD, REMOVE, DELETE, RESET, REMOVE_ALL
}
