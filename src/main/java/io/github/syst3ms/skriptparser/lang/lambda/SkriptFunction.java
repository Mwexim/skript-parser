package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SkriptFunction<S extends ReturnSection<T>, T> {
    private final S section;
    private final Function<? super S, Optional<? extends Statement>> starterFunction;
    private final BiFunction<? super S, ? super Statement, Optional<T[]>> stepFunction;
    private final BiConsumer<? super S, ? super Statement> finisherFunction;

    SkriptFunction(S section,
                   Function<? super S, Optional<? extends Statement>> starterFunction,
                   BiFunction<? super S, ? super Statement, Optional<T[]>> stepFunction,
                   BiConsumer<? super S, ? super Statement> finisherFunction) {
        this.section = section;
        this.starterFunction = starterFunction;
        this.stepFunction = stepFunction;
        this.finisherFunction = finisherFunction;
    }

    public Optional<T[]> apply(TriggerContext ctx, Object... args) {
        this.section.setArguments(args);
        T[] result = null;
        var item = starterFunction.apply(section);
        while (item.isPresent()) {
            var cur = item.orElse(null);
            item = item.flatMap(s -> s.walk(ctx));
            var stepResult = stepFunction.apply(section, cur);
            if (stepResult.isPresent()) {
                result = stepResult.get();
                break;
            }
        }
        finisherFunction.accept(section, item.orElse(null));
        return Optional.ofNullable(result);
    }

    public static <S extends ReturnSection<T>, T> SkriptFunction<S, T> create(S section) {
        return new SkriptFunctionBuilder<>(section).build();
    }

    public static <S extends ReturnSection<T>, T> SkriptFunctionBuilder<S, T> builder(S section) {
        return new SkriptFunctionBuilder<>(section);
    }
}
