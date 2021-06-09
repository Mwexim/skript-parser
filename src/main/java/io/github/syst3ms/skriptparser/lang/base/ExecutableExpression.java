package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;

import java.util.HashMap;
import java.util.Map;

/**
 * A base class for syntax that can be used as {@link Expression} or {@link Effect}.
 * An example of this could be:
 * <ul>
 *     {@code replace all {x} in {y} with {z}} <br>
 *     {@code print "%replace all {x} in {y} with {z}%}
 * </ul>
 * Both syntaxes are valid and this enables productive and better coding practices.
 * <br>
 * The class automatically implements the {@link #execute(TriggerContext)} to just call
 * {@link #getValues(TriggerContext)}, ignoring the results.
 * This behavior can obviously be overridden.
 */
public abstract class ExecutableExpression<T> extends Effect implements Expression<T>{

    private final static Map<ExecutableExpression<?>, Object[]> cachedValues = new HashMap<>();

    @Override
    protected void execute(TriggerContext ctx) {
        getAppliedValues(ctx);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T[] getValues(TriggerContext ctx) {
        if (!cachedValues.containsKey(this))
            cachedValues.put(this, getAppliedValues(ctx));
        return (T[]) cachedValues.get(this);
    }

    public abstract T[] getAppliedValues(TriggerContext ctx);

    public static Map<ExecutableExpression<?>, Object[]> getCachedValues() {
        return cachedValues;
    }
}
