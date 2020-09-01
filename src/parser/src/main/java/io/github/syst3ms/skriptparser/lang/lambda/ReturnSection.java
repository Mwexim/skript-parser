package io.github.syst3ms.skriptparser.lang.lambda;

import java.util.Optional;

public abstract class ReturnSection<T> extends ArgumentSection {
    private T[] returned;

    public Optional<T[]> getReturned() {
        return Optional.ofNullable(returned);
    }

    @SuppressWarnings("unchecked")
    public void setReturned(Object[] returned) {
        this.returned = (T[]) returned;
    }

    public abstract Class<? extends T> getReturnType();

    public abstract boolean isSingle();
}
