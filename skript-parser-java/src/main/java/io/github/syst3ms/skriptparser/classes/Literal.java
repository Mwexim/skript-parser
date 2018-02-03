package io.github.syst3ms.skriptparser.classes;

import java.lang.reflect.Array;
import java.util.Arrays;

public class Literal<T> implements Expression<T> {
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
    public T[] getValues() {
        return values;
    }

    @Override
    public String toString(boolean debug) {
        if (isSingle()) {
            return values[0].toString();
        } else {
            return Arrays.toString(values); // For now TODO make a proper way of doing this
        }
    }

    // This is only necessary here, as literals aren't registered per-say
    public boolean isSingle() {
        return values.length == 1;
    }
}
