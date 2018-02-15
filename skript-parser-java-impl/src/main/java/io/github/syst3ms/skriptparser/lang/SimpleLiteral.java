package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.interfaces.ConvertibleExpression;
import io.github.syst3ms.skriptparser.lang.interfaces.DynamicNumberExpression;
import io.github.syst3ms.skriptparser.lang.interfaces.ListExpression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.types.*;
import io.github.syst3ms.skriptparser.util.CollectionUtils;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public class SimpleLiteral<T> implements Literal<T>, ListExpression<T> {
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
    public String toString(Event e, boolean debug) {
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
    public T[] getArray(Event e) {
        if (isAndList) {
            return values;
        } else {
            T[] newArray = (T[]) Array.newInstance(getReturnType(), 1);
            newArray[0] = CollectionUtils.getRandom(values);
            return newArray;
        }
    }

    public <R> SimpleLiteral<? extends R> getConvertedExpression(final Class<R>... to) {
        if (CollectionUtils.containsSuperclass(to, getReturnType()))
            return (SimpleLiteral<? extends R>) this;
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
    public Iterator iterator(Event event) {
        return null;
    }
}
