package io.github.syst3ms.skriptparser.types.changers;

import io.github.syst3ms.skriptparser.classes.ChangeMode;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;

/**
 * An interface for anything that can be changed
 * @param <T> the type of the thing to change
 * @see Expression#change(TriggerContext, Object[], ChangeMode)
 * @see Expression#acceptsChange(ChangeMode)
 */
public interface Changer<T> {
    Class<?>[] acceptsChange(ChangeMode mode);

    void change(T[] toChange, Object[] changeWith, ChangeMode mode);
}
