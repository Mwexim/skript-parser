package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class SkriptConsumer<S extends ArgumentSection> {
    private final S section;
    private final Function<? super S, Optional<? extends Statement>> starterFunction;
    private final BiPredicate<? super S, ? super Statement> stepFunction;
    private final BiConsumer<? super S, ? super Statement> finisherFunction;

    SkriptConsumer(S section,
                   Function<? super S, Optional<? extends Statement>> starterFunction,
                   BiPredicate<? super S, ? super Statement> stepFunction,
                   BiConsumer<? super S, ? super Statement> finisherFunction) {
        this.section = section;
        this.starterFunction = starterFunction;
        this.stepFunction = stepFunction;
        this.finisherFunction = finisherFunction;
    }

    public void run(TriggerContext ctx, Object... args) {
        this.section.setArguments(args);
        var item = starterFunction.apply(section);
        while (item.isPresent()) {
            var cur = item.orElse(null);
            item = item.flatMap(s -> s.walk(ctx));
            if (stepFunction.test(section, cur)) {
                break;
            }
        }
        finisherFunction.accept(section, item.orElse(null));
    }

    public static <S extends ArgumentSection> SkriptConsumer<S> create(S section) {
        return new SkriptConsumerBuilder<>(section).build();
    }
    
    public static <S extends ArgumentSection> SkriptConsumerBuilder<S> builder(S section) {
        return new SkriptConsumerBuilder<>(section);
    }
}
