package io.github.syst3ms.skriptparser.types.changers;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;

/**
 * An interface for anything that can be changed
 * @param <T> the type of the thing to change
 * @see Expression#change(TriggerContext, ChangeMode, Object[])
 * @see Expression#acceptsChange(ChangeMode)
 */
public interface Changer<T> {
    /**
     * @param mode the given mode
     * @return the classes of the objects that the implementing object can be changed to
     */
    Class<?>[] acceptsChange(ChangeMode mode);

    /**
     * Changes the implementing object
     * @param toChange the current values
     * @param mode the change mode
     * @param changeWith the values to change with
     */
    void change(T[] toChange, ChangeMode mode, Object[] changeWith);
}
