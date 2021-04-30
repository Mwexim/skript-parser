package io.github.syst3ms.skriptparser.util.classes;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A double-valued version of an {@link Optional}, containing very similar methods.
 *
 * Note that the two values aren't really independent : either both are set, or both are empty.
 * @param <T1> the type of the first value
 * @param <T2> the type of the second value
 */
public class DoubleOptional<T1, T2> {
    /**
     * Common instance for {@code empty()}
     */
    private static final DoubleOptional<?, ?> EMPTY = new DoubleOptional<>();

    /**
     * If non-null, the first value is present ; if null, it is absent.
     */
    @Nullable
    private final T1 first;
    /**
     * If non-null, the second value is present ; if null, it is absent.
     */
    @Nullable
    private final T2 second;

    /**
     * Constructs an empty instance.
     */
    private DoubleOptional() {
        this.first = null;
        this.second = null;
    }

    /**
     * Constructs a non-empty instance.
     *
     * @param first the first value
     * @param second the second value
     * @throws NullPointerException if either parameters are {@literal null}.
     */
    private DoubleOptional(T1 first, T2 second) {
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);
    }

    /**
     * Returns an empty {@code DoubleOptional} instance.
     *
     * @param <T1> the type of the first non-existent value
     * @param <T2> the type of the second non-existent value
     * @return an empty {@code DoubleOptional}
     */
    @SuppressWarnings("unchecked")
    public static <T1, T2> DoubleOptional<T1, T2> empty() {
        return (DoubleOptional<T1, T2>) EMPTY;
    }

    /**
     * Returns an {@code Optional} describing the given non-{@code null}
     * value.
     *
     * @param first the first value to describe, which must be non-{@code null}
     * @param second the second value to describe, which must be non-{@code null}
     * @param <T1> the type of the first value
     * @param <T2> the type fo the second value
     * @return a {@code DoubleOptional} with the value present
     * @throws NullPointerException if either first or second are {@code null}
     */
    public static <T1, T2> DoubleOptional<T1, T2> of(T1 first, T2 second) {
        return new DoubleOptional<>(first, second);
    }

    /**
     * Returns a {@code DoubleOptional} describing the given values, if
     * both non-{@code null}, otherwise returns an empty {@code DoubleOptional}.
     *
     * @param first the first possibly {@code null} value to describe
     * @param second the secpond possibly {@code null} value to describe
     * @param <T1> the type of the first value
     * @param <T2> the type of the second value
     * @return a {@code DoubleOptional} with both values present if the specified values
     *         are both non-{@code null}, otherwise an empty {@code DoubleOptional}
     */
    public static <T1, T2> DoubleOptional<T1, T2> ofNullable(@Nullable T1 first, @Nullable T2 second) {
        return first == null || second == null ? empty() : new DoubleOptional<>(first, second);
    }

    /**
     * Returns a {@code DoubleOptional} describing the values represented by two {@link Optional}s if both
     * are present, otherwise returns an empty {@code DoubleOptional}.
     *
     * @param first a possibly empty {@link Optional} describing the first value
     * @param second a possibly empty {@link Optional} describing the second value
     * @param <T1> the type of the first value
     * @param <T2> the type of the second value
     * @return a {@code DoubleOptional} describing the two values described by the two {@link Optional}s,
     *         if both are present, otherwise an empty {@code DoubleOptional}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T1, T2> DoubleOptional<T1, T2> ofOptional(Optional<T1> first, Optional<T2> second) {
        return first.isEmpty() || second.isEmpty() ? empty() : new DoubleOptional<>(first.get(), second.get());
    }

    /**
     * If the first value is present, returns the value, otherwise throws a {@link NoSuchElementException}.
     *
     * @return the non-null first value of this {@code DoubleOptional}
     * @throws NoSuchElementException if no first value is present
     */
    public T1 getFirst() {
        if (first == null) {
            throw new NoSuchElementException("No first value present");
        }
        return first;
    }

    /**
     * If the second value is present, returns the value, otherwise throws a {@link NoSuchElementException}.
     *
     * @return the non-null second value of this {@code DoubleOptional}
     * @throws NoSuchElementException if no second value is present
     */
    public T2 getSecond() {
        if (second == null) {
            throw new NoSuchElementException("No second value present");
        }
        return second;
    }

    /**
     * If both values are present, returns {@code true}, otherwise {@code false}.
     *
     * @return {@code true} if both values are present, otherwise {@code false}.
     */
    public boolean isPresent() {
        return first != null && second != null;
    }

    /**
     * If either values are absent, returns {@code true}, otherwise {@code false}.
     *
     * @return {@code true} if either values are absent, otherwise {@code false}
     */
    public boolean isEmpty() {
        return first == null || second == null;
    }

    /**
     * If both values are present, does the given action using both values, otherwise does nothing.
     *
     * @param action the action to be performed if both values are present
     */
    public void ifPresent(BiConsumer<? super T1, ? super T2> action) {
        if (first != null && second != null) {
            action.accept(first, second);
        }
    }

    /**
     * If both values are present, does the first action using both values, otherwise does the second action.
     *
     * @param action the action to be performed if both values are present
     * @param emptyAction the action to be performed if either values are absent
     */
    public void ifPresentOrElse(BiConsumer<? super T1, ? super T2> action, Runnable emptyAction) {
        if (isPresent()) {
            action.accept(first, second);
        } else {
            emptyAction.run();
        }
    }

    /**
     * If both values are present and match the given predicate, returns a {@code DoubleOptional} describing
     * these values, otherwise returns an empty {@code DoubleOptional}.
     *
     * @param predicate the predicate to test both values against, if present
     * @return a {@code DoubleOptional} describing these values, if they are present and match the given predicate,
     *         otherwise an empty {@code DoubleOptional}
     */
    public DoubleOptional<T1, T2> filter(BiPredicate<? super T1, ? super T2> predicate) {
        if (isEmpty()) {
            return this;
        } else {
            return predicate.test(first, second) ? this : empty();
        }
    }

    /**
     * If both values are present, returns a {@code DoubleOptional} describing the result
     * (as if by {@link #ofNullable(Object, Object)}) of applying each given mapping function
     * to its respective value, and otherwise returns an empty {@code DoubleOptional}.
     *
     * If either mapping function returns {@code null}, the result will be an empty {@code DoubleOptional}.
     *
     * @param firstMapper the mapping function to apply to the first value, if present
     * @param secondMapper the mapping function to apply to the second value, if present
     * @param <U> the new type of the first value
     * @param <V> the new type of the second value
     * @return a {@code DoubleOptional} describing the result of applying mapping functions
     *         to each of the values if present, and an empty {@code DoubleOptional} otherwise.
     */
    public <U, V> DoubleOptional<U, V> map(
            Function<? super T1, ? extends U> firstMapper,
            Function<? super T2, ? extends V> secondMapper) {
        if (isEmpty()) {
            return empty();
        } else {
            return DoubleOptional.ofNullable(firstMapper.apply(first), secondMapper.apply(second));
        }
    }

    /**
     * If both values are present, returns an {@link Optional} constructed from the result of the given mapping
     * function applied to both values, otherwise returns an empty {@link Optional}.
     *
     * If the mapping function returns {@code null}, an empty {@link Optional} is returned.
     *
     * @param mapper the mapping function taking both values as inputs
     * @param <U> the type of the resulting {@link Optional}
     * @return an {@link Optional} describing the result of applying the mapping function to both values if present,
     *         and an empty {@link Optional} otherwise
     */
    public <U> Optional<U> mapToOptional(BiFunction<? super T1, ? super T2, ? extends U> mapper) {
        if (isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(mapper.apply(first, second));
        }
    }

    /**
     * If both values are present, returns the result of applying the given {@code DoubleOptional}-returning
     * mapping function to both values, and otherwise returns an empty {@code DoubleOptional}.
     *
     * This is similar to {@link #map(Function, Function)}, but it doesn't wrap the result in an additional
     * {@code DoubleOptional}.
     *
     * @param mapper the mapping function taking in both values (if present) and returning a {@code DoubleOptional}
     * @param <U> the type of the first value of the new {@code DoubleOptional}
     * @param <V> the type of the second value of the new {@code DoubleOptional}
     * @return the result of applying the mapping function to both values if present, and an empty {@code DoubleOptional}
     *         otherwise
     * @throws NullPointerException if the mapping function returns {@code null}
     */
    @SuppressWarnings("unchecked")
    public <U, V> DoubleOptional<U, V> flatMap(BiFunction<? super T1, ? super T2, DoubleOptional<? extends U, ? extends V>> mapper) {
        if (isEmpty()) {
            return empty();
        } else {
            DoubleOptional<U, V> opt = (DoubleOptional<U, V>) mapper.apply(first, second);
            return Objects.requireNonNull(opt);
        }
    }

    /**
     * If both values are present, returns the result of applying the given {@link Optional}-returning
     * mapping function to both values, and otherwise returns an empty {@link Optional}.
     *
     * This is similar to {@link #mapToOptional(BiFunction)}, but it doesn't wrap the result in an additional
     * {@link Optional}.
     *
     * @param mapper the mapping function taking in both values (if present) and returning an {@link Optional}
     * @param <U> the type of the new {@link Optional}
     * @return the result of applying the mapping function to both values if present, and an empty {@link Optional}
     *         otherwise
     * @throws NullPointerException if the mapping function returns {@code null}
     */
    @SuppressWarnings("unchecked")
    public <U> Optional<U> flatMapToOptional(BiFunction<? super T1, ? super T2, Optional<? extends U>> mapper) {
        if (isEmpty()) {
            return Optional.empty();
        } else {
            Optional<U> opt = (Optional<U>) mapper.apply(first, second);
            return Objects.requireNonNull(opt);
        }
    }

    /**
     * If both values are present, returns a {@code DoubleOptional} describing them, otherwise returns
     * a {@code DoubleOptional} obtained from the given supplier function.
     *
     * @param supplier the supplier producing the {@code DoubleOptional} returned if this one is empty
     * @return a {@code DoubleOptional} describing the two values if present, and otherwise the {@code DoubleOptional}
     *         produced by the given supplier function
     */
    @SuppressWarnings("unchecked")
    public DoubleOptional<T1, T2> or(Supplier<? extends DoubleOptional<? extends T1, ? extends T2>> supplier) {
        if (isPresent()) {
            return this;
        } else {
            DoubleOptional<T1, T2> opt = (DoubleOptional<T1, T2>) supplier.get();
            return Objects.requireNonNull(opt);
        }
    }

    /**
     * If the first value is present, returns it, otherwise returns {@code other}.
     *
     * @param other the value to be returned if the first value is not present (may be {@code null})
     * @return the first value if present, otherwise {@code other}
     */
    @Contract("!null -> !null")
    @Nullable
    public T1 firstOrElse(@Nullable T1 other) {
        return first != null ? first : other;
    }

    /**
     * If the second value is present, returns it, otherwise returns {@code other}.
     *
     * @param other the value to be returned if the second value is not present (may be {@code null})
     * @return the second value if present, otherwise {@code other}
     */
    @Contract("!null -> !null")
    @Nullable
    public T2 secondOrElse(@Nullable T2 other) {
        return second != null ? second : other;
    }

    /**
     * If the first value is present, returns it, otherwise throws an exception produced by the given
     * exception-supplying function.
     *
     * @param exceptionSupplier the function producing the exception to be thrown if the first value is not present
     * @param <X> the type of exception that may be thrown
     * @return the first value if present
     * @throws X if the first value is not present
     */
    public <X extends Throwable> T1 firstOrElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (first != null) {
            return first;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * If the second value is present, returns it, otherwise throws an exception produced by the given
     * exception-supplying function.
     *
     * @param exceptionSupplier the function producing the exception to be thrown if the second value is not present
     * @param <X> the type of exception that may be thrown
     * @return the second value if present
     * @throws X if the second value is not present
     */
    public <X extends Throwable> T2 secondOrElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (second != null) {
            return second;
        } else {
            throw exceptionSupplier.get();
        }
    }
}
