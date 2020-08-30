package io.github.syst3ms.skriptparser.types.changers;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;

/**
 * An interface for anything that can be changed
 * @param <T> the type of the thing to change
 * @see Expression#change(TriggerContext, Object[], ChangeMode)
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
     * @param changeWith the values to change with
     * @param mode the change mode
     */
    void change(T[] toChange, Object[] changeWith, ChangeMode mode);
}
