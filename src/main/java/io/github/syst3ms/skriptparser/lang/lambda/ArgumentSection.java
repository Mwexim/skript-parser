package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.effects.EffContinue;
import io.github.syst3ms.skriptparser.effects.EffReturn;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.control.Finishing;

import java.util.Optional;

/**
 * A {@link CodeSection} that can hold information about arguments.
 */
public abstract class ArgumentSection extends CodeSection implements Finishing {
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
     * After execution has stopped, because a statement has forcefully ended the execution,
     * this consumer is fed with the last {@link Statement}
     * that has been processed on the next iteration.
     * <br>
     * Note that this function only needs to be called for <b>iterative</b> sections, like
     * loops and maps, that need to execute certain actions after <i>each</i> iteration, instead
     * of only when the execution has finished (see {@linkplain #finish()} for that)
     * <br>
     * By default, does nothing.
     * @param item the last statement
     * @see EffContinue
     * @see EffReturn
     */
    public void step(Statement item) { /* Nothing */ }

    @Override
    public void finish() { /* Nothing */ }

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
