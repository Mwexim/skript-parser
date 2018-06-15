package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import org.jetbrains.annotations.Nullable;

/**
 * An expression whose value is known at parse time
 * @param <T> the type of the literal
 */
public interface Literal<T> extends Expression<T> {
    T[] getValues();

    @Nullable
    default T getSingle() {
        return getSingle(TriggerContext.DUMMY);
    }

    @Override
    default T[] getValues(TriggerContext e) {
        return getValues();
    }
}
