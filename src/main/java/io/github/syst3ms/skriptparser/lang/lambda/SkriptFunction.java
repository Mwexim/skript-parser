package io.github.syst3ms.skriptparser.lang.lambda;

import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A wrapper that allows running the code contained inside of a section like a functtion using a simple method,
 * taking in a context and a variable amount of arguments, and returning a value determined by the code inside.
 *
 * @param <S> the section containing the code used by this {@code SkriptFunction}
 * @param <T> the type of values returned by this {@code SkriptFunction}
 */
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

    /**
     * Runs the {@code SkriptFunction} and returns the code's returned value.
     *
     * @param ctx the {@link TriggerContext} to run the code with
     * @param args the arguments passed to the code inside, accessible using {@link ArgumentSection#getArguments()}.
     * @return an Optional describing the values returned by the code inside, or an empty Optional if no values were
     *         returned.
     */
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

    /**
     * Creates a {@code SkriptFunction} from a given {@link ReturnSection}.
     *
     * @param section the {@link ReturnSection}
     * @param <S> the type of the {@link ReturnSection}
     * @return a {@code SkriptFunction} based on the given {@link ReturnSection}
     */
    public static <S extends ReturnSection<T>, T> SkriptFunction<S, T> create(S section) {
        return new SkriptFunctionBuilder<>(section).build();
    }

    /**
     * Creates a {@link SkriptFunctionBuilder} from a given {@link ReturnSection}.
     *
     * @param section the {@link ReturnSection}
     * @param <S> the type of the {@link ReturnSection}
     * @return a {@link SkriptFunctionBuilder} based on the given {@link ReturnSection}
     */
    public static <S extends ReturnSection<T>, T> SkriptFunctionBuilder<S, T> builder(S section) {
        return new SkriptFunctionBuilder<>(section);
    }
}
