package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import org.jetbrains.annotations.Contract;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

/**
 * A simple literal with a fixed set of values
 * @param <T> the type of the values
 */
@SuppressWarnings("unchecked")
public class SimpleLiteral<T> implements Literal<T> {
    private final T[] values;
    private boolean isAndList = true;
    private Class<T> returnType;

    public SimpleLiteral(T[] values) {
        this.values = values;
    }

    @SafeVarargs
    public SimpleLiteral(Class<T> c, T... values) {
        this.values = (T[]) Array.newInstance(c, values.length);
        System.arraycopy(values, 0, this.values, 0, values.length);
    }

    @Override
    @Contract("_, _, _ -> fail")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T[] getValues() {
        if (isAndList) {
            return values;
        } else {
            var copy = (T[]) Array.newInstance(getReturnType(), 1);
            copy[0] = CollectionUtils.getRandom(values);
            return copy;
        }
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        if (isSingle()) {
            return values[0].toString();
        } else {
            return TypeManager.toString((Object[]) values);
        }
    }

    @Override
    public boolean isSingle() {
        return values.length == 1;
    }

    public Class<? extends T> getReturnType() {
        if (returnType != null) {
            return returnType;
        } else {
            return (returnType = (Class<T>) values.getClass().getComponentType());
        }
    }

    @Override
    public void setAndList(boolean isAndList) {
        this.isAndList = isAndList;
    }

    @Override
    public boolean isAndList() {
        return isAndList;
    }

    @Override
    public T[] getArray(TriggerContext ctx) {
        if (isAndList) {
            return values;
        } else {
            var newArray = (T[]) Array.newInstance(getReturnType(), 1);
            newArray[0] = CollectionUtils.getRandom(values);
            return newArray;
        }
    }

    @Override
    public <R> Optional<? extends Expression<R>> convertExpression(Class<R> to) {
        if (to.isAssignableFrom(getReturnType()))
            return Optional.of((SimpleLiteral<R>) this);
        var superType = (Class<R>) ClassUtils.getCommonSuperclass(to);
        var converted = Converters.convertArray(values, to, superType);
        if (converted.length != values.length)
            return Optional.empty();
        return Optional.of(new SimpleLiteral<>(superType, converted));
    }

    @Override
    public boolean isLoopOf(String loop) {
        return false;
    }

    @Override
    public Iterator<T> iterator(TriggerContext context) {
        if (isSingle())
            throw new SkriptRuntimeException("Can't loop a single literal !");
        return Arrays.asList(values).iterator();
    }
}
