package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * An expression that has been converted to another type. Type conversion is only being done at runtime, since the values
 * of the source expression aren't known until runtime.
 * @param <F> The type of the source expression
 * @param <T> The new, converted type of this expression
 * @see Converters
 */
public class ConvertedExpression<F, T> implements Expression<T> {
    private Expression<? extends F> source;
    private Class<T> to;
    private Function<? super F, ? extends T> converter;

    public ConvertedExpression(Expression<? extends F> source, Class<T> to, Function<? super F, ? extends T> converter) {
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

    @Override
    public T[] getValues(TriggerContext e) {
        return Converters.convert(source.getValues(e), to, converter);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(@Nullable TriggerContext e, boolean debug) {
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

    @Override
    public Expression<? extends F> getSource() {
        return source;
    }

    @Override
    public Iterator<? extends T> iterator(TriggerContext context) {
        Iterator<? extends F> sourceIterator = source.iterator(context);
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
