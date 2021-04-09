package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.effects.EffReturn;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * An {@link ArgumentSection} that can hold information about valued returned by the code inside it (typically through
 * {@link EffReturn}).
 * @param <T> the type of the return value.
 */
public abstract class ReturnSection<T> extends ArgumentSection {
    @Nullable
    private T[] returned;

    /**
     * The values being returned from inside this section.
     * @return an Optional describing the returned values, or an empty Optional if no values have been returned so far.
     */
    public Optional<T[]> getReturned() {
        return Optional.ofNullable(returned);
    }

    /**
     * Sets the values returned from inside this section.
     * @param returned the returned values
     * @throws ClassCastException if the values passed aren't of the type {@link T}
     */
    @SuppressWarnings("unchecked")
    public void setReturned(Object[] returned) {
        this.returned = (T[]) returned;
    }

    /**
     * @return the Class of the values that should be returned from inside this section
     */
    public abstract Class<? extends T> getReturnType();

    /**
     * @return the number of values that should be returned from inside this section
     */
    public abstract boolean isSingle();
}
