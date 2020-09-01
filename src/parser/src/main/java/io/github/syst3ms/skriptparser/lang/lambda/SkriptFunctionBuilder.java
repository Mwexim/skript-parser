package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Statement;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SkriptFunctionBuilder<S extends ReturnSection<T>, T> {
    private final S section;
    private Function<? super S, Optional<? extends Statement>> starterFunction = CodeSection::getFirst;
    private BiFunction<? super S, ? super Statement, Optional<T[]>> stepFunction = (sec, stat) -> sec.getReturned();
    private BiConsumer<? super S, ? super Statement> finisherFunction = (a, b) -> {};

    SkriptFunctionBuilder(S section) {
        this.section = section;
    }

    public SkriptFunctionBuilder<S, T> setStarterFunction(Function<? super S, Optional<? extends Statement>> starterFunction) {
        this.starterFunction = starterFunction;
        return this;
    }

    public SkriptFunctionBuilder<S, T> setStepFunction(BiFunction<? super S, ? super Statement, Optional<T[]>> stepFunction) {
        this.stepFunction = stepFunction;
        return this;
    }

    public SkriptFunctionBuilder<S, T> setFinisherFunction(BiConsumer<? super S, ? super Statement> finisherFunction) {
        this.finisherFunction = finisherFunction;
        return this;
    }

    public SkriptFunction<S, T> build() {
        return new SkriptFunction<>(section, starterFunction, stepFunction, finisherFunction);
    }
}
