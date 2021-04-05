package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Statement;

import java.util.Optional;

/**
 * A {@link CodeSection} that can hold information about arguments.
 */
public abstract class ArgumentSection extends CodeSection {
    private Object[] arguments;

    /**
     * This function is called from the section containing the code, and returns an Optional describing
     * the first {@link Statement} that should be run in the consumer.
     * <br>
     * By default, returns {@link CodeSection#getFirst()}.
     */
    protected Optional<? extends Statement> start() {
        return getFirst();
    }

    /**
     * After execution has stopped, because there are no more statements to run,
     * this consumer is fed with the last {@link Statement}
     * that would have been processed on the next iteration.
     * @param item the last statement
     */
    protected void finish(Statement item) { /* Nothing */ }

    /**
     * @return the arguments passed to this section's code
     */
    public Object[] getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments that should be passed to the section code.
     * @param arguments this section's arguments
     */
    public void setArguments(Object... arguments) {
        this.arguments = arguments;
    }
}
