package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Statement;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * A builder for {@link SkriptConsumer}s.
 *
 * @param <S> the section the resulting {@link SkriptConsumer} will be based on
 */
public class SkriptConsumerBuilder<S extends ArgumentSection> {
    private final S section;
    private Function<? super S, Optional<? extends Statement>> starterFunction = CodeSection::getFirst;
    private BiPredicate<? super S, ? super Statement> stepFunction = (a, b) -> false;
    private BiConsumer<? super S, ? super Statement> finisherFunction = (a, b) -> {};

    SkriptConsumerBuilder(S section) {
        this.section = section;
    }

    /**
     * Sets the {@link SkriptConsumer}'s starter function.
     *
     * This function is fed with the section containing the code, and returns an Optional describing
     * the first {@link Statement} that should be run in the {@link SkriptConsumer}.
     *
     * By default, returns {@link CodeSection#getFirst()}.
     *
     * @param starterFunction the starter function
     */
    public SkriptConsumerBuilder<S> setStarterFunction(Function<? super S, Optional<? extends Statement>> starterFunction) {
        this.starterFunction = starterFunction;
        return this;
    }

    /**
     * Sets the {@link SkriptConsumer}'s step function.
     *
     * This predicate is fed with the section containing the code and the {@link Statement} that was just processed,
     * and returns whether the execution should continue or not.
     *
     * By default, this predicate always returns {@code false}.
     *
     * @param stepFunction the step function
     */
    public SkriptConsumerBuilder<S> setStepFunction(BiPredicate<? super S, ? super Statement> stepFunction) {
        this.stepFunction = stepFunction;
        return this;
    }

    /**
     * Set the {@link SkriptConsumer}'s finisher function.
     *
     * After execution has stopped (be it because there are no more statements to run, or because the step function
     * returning {@code true}), this consumer is fed with the section containing the code and the {@link Statement}
     * that would have been processed on the next iteration.
     *
     * By default, this consumer does nothing.
     *
     * @param finisherFunction the finisher function
     */
    public SkriptConsumerBuilder<S> setFinisherFunction(BiConsumer<? super S, ? super Statement> finisherFunction) {
        this.finisherFunction = finisherFunction;
        return this;
    }

    /**
     * Builds the {@link SkriptConsumer}.
     *
     * @return the built {@link SkriptConsumer}
     */
    public SkriptConsumer<S> build() {
        return new SkriptConsumer<>(section, starterFunction, stepFunction, finisherFunction);
    }
}
