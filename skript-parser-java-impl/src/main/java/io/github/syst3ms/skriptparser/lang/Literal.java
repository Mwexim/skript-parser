package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Literal<T> extends Expression<T> {
    T[] getValues();

    @SuppressWarnings("ConstantConditions")
    @Nullable
    default T getSingle() {
        return getSingle(null);
    }

    @Override
    default T[] getValues(Event e) {
        return getValues();
    }
}
