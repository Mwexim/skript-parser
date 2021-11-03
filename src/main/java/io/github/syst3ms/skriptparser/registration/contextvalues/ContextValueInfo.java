package io.github.syst3ms.skriptparser.registration.contextvalues;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.Type;

import java.util.function.Function;

/**
 * A class containing info about a context value.
 */
public class ContextValueInfo<C extends TriggerContext, T> {
    private final SkriptAddon registerer;

    private final Class<C> context;
    private final PatternType<T> returnType;
    private final PatternElement pattern;

    private final Function<C, T[]> function;
    private final State state;
    private final Usage usage;

    public ContextValueInfo(SkriptAddon registerer,
							Class<C> context,
							Type<T> returnType, boolean isSingle,
							PatternElement pattern,
							Function<C, T[]> function,
							State state, Usage usage) {
        this.registerer = registerer;
        this.context = context;
        this.returnType = new PatternType<>(returnType, isSingle);
        this.pattern = pattern;
        this.function = function;
        this.state = state;
        this.usage = usage;
    }

    public SkriptAddon getRegisterer() {
        return registerer;
    }

    public Class<C> getContext() {
        return context;
    }

    public PatternType<T> getReturnType() {
        return returnType;
    }

    /**
     * Returns the pattern of the context value.
     * If the pattern, for example, is 'test', the it can be used as 'context-test'.
     * @return the pattern of this context value
     */
    public PatternElement getPattern() {
        return pattern;
    }

    /**
     * @return the function that needs to be applied in order to get the context value
     */
    public Function<C, T[]> getFunction() {
        return function;
    }

    /**
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
	 * An enum to indicate the relative position in time between two similar context values.
	 * Note that this is just to <b>indicate</b> time difference.
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

    public enum Usage {
        EXPRESSION_ONLY, STANDALONE_ONLY, EXPRESSION_OR_STANDALONE;

        /**
         * Checks if the usage of this context value corresponds to the actual used syntax.
         * @param standalone whether the context value was used standalone
         * @return whether or not the context value was used correctly
         */
        public boolean isCorrect(boolean standalone) {
            return standalone && this != EXPRESSION_ONLY || !standalone && this != STANDALONE_ONLY;
        }
    }
}
