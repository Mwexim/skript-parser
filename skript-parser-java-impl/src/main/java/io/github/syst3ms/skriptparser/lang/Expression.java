package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.classes.ChangeMode;
import io.github.syst3ms.skriptparser.file.SimpleFileLine;
import io.github.syst3ms.skriptparser.parsing.ParseResult;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Expression<T> extends SyntaxElement {
    T[] getValues();

    default boolean change(ChangeMode changeMode) {
        return false;
    }

    static <T> Expression<T> fromLambda(Supplier<? extends T> supplier, Function<Boolean, String> toString) {
        return new Expression<T>() {
            private final T value = supplier.get();

            @SuppressWarnings("unchecked")
            @Override
            public T[] getValues() {
                return (T[]) new Object[]{value};
            }

            @Override
            public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
                return true;
            }

            @Override
            public String toString(boolean debug) {
                return toString.apply(debug);
            }
        };
    }

    static <T> Expression<T> fromLambda(Supplier<? extends T> supplier) {
        return fromLambda(supplier, b -> Objects.toString(supplier.get()));
    }

    static Expression<?> parse(String s) {
        return fromLambda(() -> null);
    }
}
