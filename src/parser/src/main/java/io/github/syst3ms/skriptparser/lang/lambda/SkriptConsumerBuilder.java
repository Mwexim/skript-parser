package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Statement;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class SkriptConsumerBuilder<S extends ArgumentSection> {
    private final S section;
    private Function<? super S, Optional<? extends Statement>> starterFunction = CodeSection::getFirst;
    private BiPredicate<? super S, ? super Statement> stepFunction = (a, b) -> false;
    private BiConsumer<? super S, ? super Statement> finisherFunction = (a, b) -> {};

    SkriptConsumerBuilder(S section) {
        this.section = section;
    }

    public SkriptConsumerBuilder<S> setStarterFunction(Function<? super S, Optional<? extends Statement>> starterFunction) {
        this.starterFunction = starterFunction;
        return this;
    }

    public SkriptConsumerBuilder<S> setStepFunction(BiPredicate<? super S, ? super Statement> stepFunction) {
        this.stepFunction = stepFunction;
        return this;
    }

    public SkriptConsumerBuilder<S> setFinisherFunction(BiConsumer<? super S, ? super Statement> finisherFunction) {
        this.finisherFunction = finisherFunction;
        return this;
    }

    public SkriptConsumer<S> build() {
        return new SkriptConsumer<>(section, starterFunction, stepFunction, finisherFunction);
    }
}
