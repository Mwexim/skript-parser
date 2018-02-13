package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.interfaces.DynamicNumberExpression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;

import java.lang.reflect.Array;

public class Literal<T> implements Expression<T>, DynamicNumberExpression {
    private T[] values;

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public Literal(Class<T> c, T... values) {
        this.values = (T[]) Array.newInstance(c, values.length);
        System.arraycopy(values, 0, this.values, 0, values.length);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        return true;
    }

    @Override
    public T[] getValues(Event e) {
        return values;
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

    @SuppressWarnings("unchecked")
    public Class<? extends T> getReturnType() {
        return (Class<? extends T>) values.getClass().getComponentType();
    }
}
