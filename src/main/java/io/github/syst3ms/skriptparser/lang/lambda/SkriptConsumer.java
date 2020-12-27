package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * A wrapper that allows running the code contained inside of a section using a simple method, taking in a context and
 * a variable amount of arguments.
 *
 * @param <S> the section containing the code used by this {@code SkriptConsumer}
 */
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

    /**
     * Runs the {@code SkriptConsumer}.
     *
     * @param ctx the {@link TriggerContext} to run the code with
     * @param args the arguments passed to the code inside, accessible using {@link ArgumentSection#getArguments()}.
     */
    public void accept(TriggerContext ctx, Object... args) {
        this.section.setArguments(args);
        var item = starterFunction.apply(section);
        while (item.isPresent()) {
            var cur = item.orElse(null);
            item = item.flatMap(s -> s.walk(ctx, true));
            if (stepFunction.test(section, cur)) {
                break;
            }
        }
        finisherFunction.accept(section, item.orElse(null));
    }

    /**
     * Creates a {@code SkriptConsumer} from a given {@link ArgumentSection}.
     *
     * @param section the {@link ArgumentSection}
     * @param <S> the type of the {@link ArgumentSection}
     * @return a {@code SkriptConsumer} based on the given {@link ArgumentSection}
     */
    public static <S extends ArgumentSection> SkriptConsumer<S> create(S section) {
        return new SkriptConsumerBuilder<>(section).build();
    }

    /**
     * Creates a {@link SkriptConsumerBuilder} from a given {@link ArgumentSection}.
     *
     * @param section the {@link ArgumentSection}
     * @param <S> the type of the {@link ArgumentSection}
     * @return a {@link SkriptConsumerBuilder} based on the given {@link ArgumentSection}
     */
    public static <S extends ArgumentSection> SkriptConsumerBuilder<S> builder(S section) {
        return new SkriptConsumerBuilder<>(section);
    }
}
