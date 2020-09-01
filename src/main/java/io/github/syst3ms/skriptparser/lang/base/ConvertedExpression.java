package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

/**
 * An expression that has been converted to another type. Type conversion is only being done at runtime, since the values
 * of the source expression aren't known until runtime.
 * @param <F> The type of the source expression
 * @param <T> The new, converted type of this expression
 * @see Converters
 */
public class ConvertedExpression<F, T> implements Expression<T> {
    private final Expression<? extends F> source;
    private final Class<T> to;
    private final Function<? super F, Optional<? extends T>> converter;

    public ConvertedExpression(Expression<? extends F> source, Class<T> to, Function<? super F, Optional<? extends T>> converter) {
        this.source = source;
        this.to = to;
        this.converter = converter;
    }

    @SuppressWarnings("unchecked")
    public static <F, T> Optional<? extends ConvertedExpression<F, T>> newInstance(Expression<F> v, Class<T> to) {
        return Converters.getConverter(v.getReturnType(), to)
                .map(c -> new ConvertedExpression<>(v, to, (Function<? super F, Optional<? extends T>>) c));
    }

    @Override
    public T[] getValues(TriggerContext ctx) {
        return Converters.convert(source.getValues(ctx), to, converter);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        if (debug && ctx == null)
            return "(" + source.toString(null, true) + " >> " + converter + ": " + source.getReturnType().getName() + "->" + to.getName() + ")";
        return source.toString(ctx, debug);
    }

    @Override
    public boolean isSingle() {
        return source.isSingle();
    }

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
        var sourceIterator = source.iterator(context);
        if (!sourceIterator.hasNext())
            return Collections.emptyIterator();
        return new Iterator<>() {
            @Nullable T next = null;

            @Override
            public boolean hasNext() {
                if (next != null)
                    return true;
                while (next == null && sourceIterator.hasNext()) {
                    var f = sourceIterator.next();
                    next = Optional.ofNullable(f).flatMap(converter::apply).orElse(null);
                }
                return next != null;
            }

            @Override
            public T next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                var n = next;
                assert next != null;
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
