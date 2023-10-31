package io.github.syst3ms.skriptparser.registration.context;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.Type;

import java.util.function.Function;

/**
 * A class containing info about a context value.
 */
public class ContextValue<C extends TriggerContext, T> {
    private final Class<C> context;
    private final PatternType<T> returnType;
    private final PatternElement pattern;

    private final Function<C, T[]> function;
    private final State state;
    private final Usage usage;

    private final Class<? extends C>[] excluded;

	@SuppressWarnings("unchecked")
	public ContextValue(Class<C> context,
						Type<T> returnType, boolean isSingle,
						PatternElement pattern,
						Function<C, T[]> function,
						State state, Usage usage) {
		this(context, returnType, isSingle, pattern, function, state, usage, new Class[0]);
	}

	public ContextValue(Class<C> context,
						Type<T> returnType, boolean isSingle,
						PatternElement pattern,
						Function<C, T[]> function,
						State state, Usage usage,
						Class<? extends C>[] excluded) {
        this.context = context;
        this.returnType = new PatternType<>(returnType, isSingle);
        this.pattern = pattern;
        this.function = function;
        this.state = state;
        this.usage = usage;
        this.excluded = excluded;
    }

    public Class<C> getContext() {
        return context;
    }

    public PatternType<T> getReturnType() {
        return returnType;
    }

    /**
     * Returns the pattern of this context value.
     * @return the pattern
     */
    public PatternElement getPattern() {
        return pattern;
    }

    /**
	 * Returns the function that will be applied onto the {@linkplain TriggerContext context}
	 * in order to retrieve the correct value.
     * @return the function
     */
    public Function<C, T[]> getFunction() {
        return function;
    }

    /**
	 * A contextual expression mostly refers to things that happen because of the event,
	 * but, sometimes, an event can have a former and future aspect. The state of the context
	 * value describes these aspects.
     * @return whether this happens in the past, present or future
     */
    public State getState() {
        return state;
    }

    /**
     * @return whether this value can be used alone
     */
    public Usage getUsage() {
        return usage;
    }

	/**
	 * Some subclasses don't want to inherit the context values of their parents.
	 * The returned array contains all the subclasses that should be excluded when parsing
	 * this context value.
	 * @return the excluded contexts
	 */
	public Class<? extends C>[] getExcluded() {
		return excluded;
	}

	/**
	 * An enum to indicate the relative position in time between two similar context values.
	 * Note that this is just to <b>indicate</b> time difference. There isn't a different treatment
	 * for different states.
	 */
	public enum State {
		/**
		 * The context value indicates something before the event happened.
		 */
		PAST,

		/**
		 * The context value indicates something during the event.
		 */
		PRESENT,

		/**
		 * The context value indicates something that changed during the event related to its state in the past,
		 * or something that will change after the event.
		 */
		FUTURE
	}

	/**
	 * One can use context values in two different ways:
	 * <ul>
	 *     <li>{@code context-something} - the value is used with the common prefix 'context'; or</li>
	 *     <li>{@code something} - the value is used {@linkplain #ALONE_ONLY alone} as an expression.</li>
	 * </ul>
	 * The usage determines which of the two variants, or both, are applicable.
	 */
    public enum Usage {
        EXPRESSION_ONLY, ALONE_ONLY, EXPRESSION_OR_ALONE;

        /**
         * Checks if the usage of this context value corresponds to the actual used syntax.
         * @param alone whether the context value was used alone
         * @return whether or not the context value was used correctly
         */
        public boolean isCorrect(boolean alone) {
            return alone && this != EXPRESSION_ONLY || !alone && this != ALONE_ONLY;
        }
    }
}
