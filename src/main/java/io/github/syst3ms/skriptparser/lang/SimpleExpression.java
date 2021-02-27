package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A simple expression with a fixed function to retrieve its values
 * @param <T> the type of the values
 */
public class SimpleExpression<T> implements Expression<T> {
    private final Class<? extends T> returnType;
    private final boolean isSingle;
    private final Function<TriggerContext, T[]> function;
    @Nullable
    private final String representation;

    public SimpleExpression(Class<? extends T> returnType, boolean isSingle, Function<TriggerContext, T[]> function) {
        this(returnType, isSingle, function, null);
    }

    public SimpleExpression(Class<? extends T> returnType, boolean isSingle, Function<TriggerContext, T[]> function, @Nullable String representation) {
        this.returnType = returnType;
        this.isSingle = isSingle;
        this.function = function;
        this.representation = representation;
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
        // If the representation is not given, the default TypeManager#toString will be used to convert each value separately
        if (representation == null) {
            return TypeManager.toString((Object[]) function.apply(ctx));
        } else {
            return representation;
        }
    }
}
