package io.github.syst3ms.skriptparser.registration.contextvalues;

import io.github.syst3ms.skriptparser.lang.TriggerContext;

import java.util.function.Function;

/**
 * A class containing info about a context value.
 */
public class ContextValue<T> {
    private final Class<? extends TriggerContext> context;
    private final Class<T> type;
    private final String name;
    private final Function<TriggerContext, T[]> contextFunction;
    private final ContextValueTime time;

    /**
     * Construct a context value.
     *
     * @param context         the specific {@link TriggerContext} class
     * @param name            the suffix of this value
     * @param contextFunction the function to apply to the context
     */
    public ContextValue(Class<? extends TriggerContext> context, Class<T> type, String name, Function<TriggerContext, T[]> contextFunction) {
        this(context, type, name, contextFunction, ContextValueTime.PRESENT);
    }

    /**
     * Construct a context value.
     *
     * @param context         the specific {@link TriggerContext} class
     * @param name            the suffix of this value
     * @param contextFunction the function to apply to the context
     * @param time            whether this value represent a present, past or future state
     */
    public ContextValue(Class<? extends TriggerContext> context, Class<T> type, String name, Function<TriggerContext, T[]> contextFunction, ContextValueTime time) {
        this.context = context;
        this.type = type;
        this.name = name;
        this.contextFunction = contextFunction;
        this.time = time;
    }

    public Class<? extends TriggerContext> getContext() {
        return context;
    }

    /**
     * @return the returned type of this context value
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Returns the name of the context value.
     * If the name, for example, is 'test', the use case will be 'context-test'
     * @return the name of this context value
     */
    public String getName() {
        return name;
    }

    /**
     * @return the function that needs to be applied in order to get the context value
     */
    public Function<TriggerContext, T[]> getContextFunction() {
        return contextFunction;
    }

    /**
     * @return whether this happens in the present, past or future
     */
    public ContextValueTime getTime() {
        return time;
    }

    public boolean matches(Class<? extends TriggerContext> handledContext, String name, ContextValueTime time) {
        return handledContext.equals(this.context)
                && this.name.equalsIgnoreCase(name)
                && this.time.equals(time);
    }
}
