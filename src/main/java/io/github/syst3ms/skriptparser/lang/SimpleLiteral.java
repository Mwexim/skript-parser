package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * A simple literal with a fixed set of values
 * @param <T> the type of the values
 */
@SuppressWarnings("unchecked")
public class SimpleLiteral<T> implements Literal<T> {
    private T[] values;
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
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T[] getValues() {
        if (isAndList) {
            return values;
        } else {
            T[] copy = (T[]) Array.newInstance(getReturnType(), 1);
            copy[0] = CollectionUtils.getRandom(values);
            return copy;
        }
    }

    @Override
    public String toString(@Nullable TriggerContext e, boolean debug) {
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
        if (returnType == null)
            return (returnType = (Class<T>) values.getClass().getComponentType());
        else
            return returnType;
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
    public T[] getArray(TriggerContext e) {
        if (isAndList) {
            return values;
        } else {
            T[] newArray = (T[]) Array.newInstance(getReturnType(), 1);
            newArray[0] = CollectionUtils.getRandom(values);
            return newArray;
        }
    }

    @Override
    public <R> Expression<R> convertExpression(Class<R> to) {
        if (to.isAssignableFrom(getReturnType()))
            return (SimpleLiteral<R>) this;
        Class<R> superType = (Class<R>) ClassUtils.getCommonSuperclass(to);
        R[] converted = Converters.convertArray(values, to, superType);
        if (converted.length != values.length)
            return null;
        return new SimpleLiteral<>(superType, converted);
    }

    @Override
    public boolean isLoopOf(String loop) {
        return false;
    }

    @Override
    public Iterator iterator(TriggerContext context) {
        if (!isSingle())
            throw new SkriptRuntimeException("Can't loop a single literal !");
        return CollectionUtils.iterator(values);
    }
}
