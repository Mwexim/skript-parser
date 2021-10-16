package io.github.syst3ms.skriptparser.registration.contextvalues;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.types.Type;

import java.util.function.Function;

/**
 * A class containing info about a context value.
 */
public class ContextValue<T> {
    private final Class<? extends TriggerContext> context;
    private final Type<T> type;
    private final boolean isSingle;
    private final String name;
    private final Function<TriggerContext, T[]> contextFunction;
    private final ContextValueState state;
    private final boolean standalone;

    /**
     * Construct a context value.
     * @param context the specific {@link TriggerContext} class.
     * @param type the return type
     * @param isSingle whether this value is single
     * @param name the name of this value
     * @param contextFunction the function to apply to the context
     */
    public ContextValue(Class<? extends TriggerContext> context, Type<T> type, boolean isSingle, String name, Function<TriggerContext, T[]> contextFunction) {
        this(context, type, isSingle, name, contextFunction, false);
    }

    /**
     * Construct a context value.
     * @param context the specific {@link TriggerContext} class.
     * @param type the return type
     * @param isSingle whether this value is single
     * @param name the name of this value
     * @param contextFunction the function to apply to the context
     * @param standalone whether or not this value can be used alone
     */
    public ContextValue(Class<? extends TriggerContext> context, Type<T> type, boolean isSingle, String name, Function<TriggerContext, T[]> contextFunction, boolean standalone) {
        this(context, type, isSingle, name, contextFunction, ContextValueState.PRESENT, standalone);
    }

    /**
     * Construct a context value.
     * @param context the specific {@link TriggerContext} class.
     * @param type the return type
     * @param isSingle whether this value is single
     * @param name the name of this value
     * @param contextFunction the function to apply to the context
     * @param state the time of this value
     */
    public ContextValue(Class<? extends TriggerContext> context, Type<T> type, boolean isSingle, String name, Function<TriggerContext, T[]> contextFunction, ContextValueState state) {
        this(context, type, isSingle, name, contextFunction, state, false);
    }

    /**
     * Construct a context value.
     * @param context the specific {@link TriggerContext} class.
     * @param type the return type
     * @param isSingle whether this value is single
     * @param name the name of this value
     * @param contextFunction the function to apply to the context
     * @param state the time of this value
     * @param standalone whether or not this value can be used alone
     */
    public ContextValue(Class<? extends TriggerContext> context, Type<T> type, boolean isSingle, String name, Function<TriggerContext, T[]> contextFunction, ContextValueState state, boolean standalone) {
        this.context = context;
        this.type = type;
        this.isSingle = isSingle;
        this.name = name;
        this.contextFunction = contextFunction;
        this.state = state;
        this.standalone = standalone;
    }

    public Class<? extends TriggerContext> getContext() {
        return context;
    }

    /**
     * @return the Type associated with this context value
     */
    public Type<T> getType() {
        return type;
    }

    public boolean isSingle() {
        return isSingle;
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
    public ContextValueState getState() {
        return state;
    }

    /**
     * @return whether this value can be used alone
     */
    public boolean isStandalone() {
        return standalone;
    }

    public boolean matches(String name, ContextValueState time, boolean standalone) {
        return this.name.equalsIgnoreCase(name)
                && this.state == time
                && (this.standalone || this.standalone == standalone);
    }

    public boolean matches(Class<? extends TriggerContext> handledContext, String name, ContextValueState time, boolean standalone) {
        return this.context.isAssignableFrom(handledContext)
                && this.name.equalsIgnoreCase(name)
                && this.state == time
                && (this.standalone || this.standalone == standalone);
    }
}
