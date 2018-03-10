package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ConvertedExpression<F, T> implements Expression<T> {
    private Expression<? extends F> source;
    private Class<T> to;
    private Function<? super F, ? extends T> converter;

    ConvertedExpression(Expression<? extends F> source, Class<T> to, Function<? super F, ? extends T> converter) {
        this.source = source;
        this.to = to;
        this.converter = converter;
    }

    @Nullable
    public static <F, T> ConvertedExpression<F, T> newInstance(Expression<F> v, Class<T> to) {
        // casting <? super ? extends F> to <? super F> is wrong, but since the converter is only used for values returned by the expression
        // (which are instances of "<? extends F>") this won't result in any ClassCastExceptions.
        @SuppressWarnings("unchecked")
        Function<? super F, ? extends T> conv = (Function<? super F, ? extends T>) Converters
                .getConverter(v.getReturnType(), to);
        if (conv == null)
            return null;
        return new ConvertedExpression<>(v, to, conv);

    }

    @NotNull
    @Override
    public T[] getValues(Event e) {
        return Converters.convert(source.getValues(e), to, converter);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        if (debug && e == null)
            return "(" + source.toString(null, true) + " >> " + converter + ": " + source.getReturnType().getName() + "->" + to.getName() + ")";
        return source.toString(e, debug);
    }

    @Override
    public boolean isSingle() {
        return source.isSingle();
    }

    @SuppressWarnings("unchecked")
    public Class<T> getReturnType() {
        return to;
    }

    @Override
    @Contract("_ -> false")
    public boolean isLoopOf(String loop) {
        return false;
    }

    @NotNull
    @Override
    public Expression<? extends F> getSource() {
        return source;
    }

    @NotNull
    @Override
    public Iterator<? extends T> iterator(Event event) {
        Iterator<? extends F> sourceIterator = source.iterator(event);
        if (!sourceIterator.hasNext())
            return Collections.emptyIterator();
        return new Iterator<T>() {
            @Nullable T next = null;

            @Override
            public boolean hasNext() {
                if (next != null)
                    return true;
                while (next == null && sourceIterator.hasNext()) {
                    F f = sourceIterator.next();
                    next = f == null ? null : converter.apply(f);
                }
                return next != null;
            }

            @Nullable
            @Override
            public T next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                T n = next;
                next = null;
                return n;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
