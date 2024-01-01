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

    private boolean isAndList = true;
    private final Class<T> returnType;
    private final T[] values;

    public SimpleLiteral(T[] values) {
        this.values = values;
        this.returnType = (Class<T>) values.getClass().getComponentType();
    }

    @SafeVarargs
    public SimpleLiteral(Class<T> c, T... values) {
        this.values = Arrays.copyOf(values, values.length);
        this.returnType = c;
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
    public T[] getArray(TriggerContext ctx) {
        return values;
    }

    @Override
    public boolean isSingle() {
        return values.length == 1;
    }

    public Class<? extends T> getReturnType() {
        return returnType;
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        return TypeManager.toString(values);
    }

    @Override
    public Iterator<T> iterator(TriggerContext context) {
        if (isSingle())
            throw new SkriptRuntimeException("Can't loop a single literal!");
        return Arrays.asList(values).iterator();
    }

    @Override
    public boolean isLoopOf(String loop) {
        return false;
    }

    @Override
    public boolean isAndList() {
        return isAndList;
    }

    @Override
    public void setAndList(boolean isAndList) {
        this.isAndList = isAndList;
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
}
