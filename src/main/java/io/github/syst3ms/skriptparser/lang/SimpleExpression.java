package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Contract;

import java.util.function.Function;

/**
 * A simple expression with a fixed function to retrieve its values
 * @param <T> the type of the values
 */
public class SimpleExpression<T> implements Expression<T> {
    private final Class<? extends T> returnType;
    private final boolean isSingle;
    private final Function<TriggerContext, T[]> function;
    private final String toString;

    public SimpleExpression(Class<? extends T> returnType, boolean isSingle, Function<TriggerContext, T[]> function, String toString) {
        this.returnType = returnType;
        this.isSingle = isSingle;
        this.function = function;
        this.toString = toString;
    }

    @Override
    @Contract("_, _, _ -> fail")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T[] getValues(TriggerContext ctx) {
        return function.apply(ctx);
    }

    @Override
    public boolean isSingle() {
        return isSingle;
    }

    public Class<? extends T> getReturnType() {
        return returnType;
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return toString;
    }
}
