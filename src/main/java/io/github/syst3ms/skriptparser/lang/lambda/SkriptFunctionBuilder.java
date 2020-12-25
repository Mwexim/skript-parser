package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.effects.EffReturn;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Statement;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A builder for {@link SkriptFunction}s.
 *
 * @param <S> the section the resulting {@link SkriptFunction} will be based on
 */
public class SkriptFunctionBuilder<S extends ReturnSection<T>, T> {
    private final S section;
    private Function<? super S, Optional<? extends Statement>> starterFunction = CodeSection::getFirst;
    private BiFunction<? super S, ? super Statement, Optional<T[]>> stepFunction = (sec, stat) -> sec.getReturned().filter(__ -> stat instanceof EffReturn);
    private BiConsumer<? super S, ? super Statement> finisherFunction = (a, b) -> {};

    SkriptFunctionBuilder(S section) {
        this.section = section;
    }

    /**
     * Sets the {@link SkriptFunction}'s starter function.
     *
     * This function is fed with the section containing the code, and returns an Optional describing
     * the first {@link Statement} that should be run in the {@link SkriptFunction}.
     *
     * By default, returns {@link CodeSection#getFirst()}.
     *
     * @param starterFunction the starter function
     */
    public SkriptFunctionBuilder<S, T> setStarterFunction(Function<? super S, Optional<? extends Statement>> starterFunction) {
        this.starterFunction = starterFunction;
        return this;
    }

    /**
     * Sets the {@link SkriptFunction}'s step function.
     *
     * This function is fed with the section containing the code and the {@link Statement} that was just processed,
     * and returns an Optional describing the values that should be returned by the section, or an empty Optional
     * if no values should be returned yet. If the Optional is non-empty, executing is halted, and the values are
     * returned (after executing the finisher function).
     *
     * By default, this predicate always returns {@link ReturnSection#getReturned()}.
     *
     * @param stepFunction the step function
     */
    public SkriptFunctionBuilder<S, T> setStepFunction(BiFunction<? super S, ? super Statement, Optional<T[]>> stepFunction) {
        this.stepFunction = stepFunction;
        return this;
    }

    /**
     * Set the {@link SkriptFunction}'s finisher function.
     *
     * After execution has stopped (be it because there are no more statements to run, or because the step function
     * returning {@code true}), this consumer is fed with the section containing the code and the {@link Statement}
     * that would have been processed on the next iteration.
     *
     * By default, this consumer does nothing.
     *
     * @param finisherFunction the finisher function
     */
    public SkriptFunctionBuilder<S, T> setFinisherFunction(BiConsumer<? super S, ? super Statement> finisherFunction) {
        this.finisherFunction = finisherFunction;
        return this;
    }

    public SkriptFunction<S, T> build() {
        return new SkriptFunction<>(section, starterFunction, stepFunction, finisherFunction);
    }
}
