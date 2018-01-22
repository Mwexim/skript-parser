package io.github.syst3ms.skriptparser.classes;

public interface Expression<T> extends SyntaxElement {
    T[] getValues();

    Class<? extends T> getReturnType();

    boolean isSingle();

    @Override
    String toString();
}
