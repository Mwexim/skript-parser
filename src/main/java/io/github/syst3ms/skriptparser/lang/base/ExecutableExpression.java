package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SelfRegistrable;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;

import java.util.Arrays;
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
 * Finally, this implements {@link SelfRegistrable}, enabling an easy registration process.
 */
public abstract class ExecutableExpression<T>
        extends Effect
        implements Expression<T>, SelfRegistrable {

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

    @SuppressWarnings("unchecked")
    @Override
    public void register(SkriptRegistration reg, Object... args) {
        Class<T> type = (Class<T>) args[0];
        boolean isSingle = (boolean) args[1];
        String[] patterns = Arrays.copyOfRange(args, 2, args.length, String[].class);

        // The actual registration
        reg.addExpression(getClass(), type, isSingle, patterns);
        reg.addEffect(getClass(), patterns);
    }

    public static Map<ExecutableExpression<?>, Object[]> getCachedValues() {
        return cachedValues;
    }
}
