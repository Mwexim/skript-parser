package io.github.syst3ms.skriptparser.types.changers;

import io.github.syst3ms.skriptparser.classes.ChangeMode;

public interface Changer<T> {
    Class<?>[] acceptsChange(ChangeMode mode);

    void change(T[] toChange, Object[] changeWith, ChangeMode mode);
}
