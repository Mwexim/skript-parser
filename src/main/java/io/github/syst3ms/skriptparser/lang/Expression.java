package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.expressions.ExprLoopValue;
import io.github.syst3ms.skriptparser.lang.base.ConvertedExpression;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.sections.SecLoop;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.classes.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An expression, i.e a {@link SyntaxElement} representing a value with some type.
 * @param <T> the type of value this expression returns
 */
public interface Expression<T> extends SyntaxElement {
    /**
     * Retrieves all values of this Expression.
     * Note that this method may have possible side-effects.
     * @param ctx the event
     * @return an array of the values
     */
    T[] getValues(TriggerContext ctx);

    /*
     * This is staying until we figure out a better way to implement this
     */
    default T[] getArray(TriggerContext ctx) {
        return getValues(ctx);
    }

    /**
     * Determines whether this expression can be changed according to a specific {@link ChangeMode}, and what type
     * of values it can be changed with.
     * @param mode the mode this Expression would be changed with
     * @return an array of classes describing what types this Expression can be changed with, or {@code null} if it
     *         shouldn't be changed with the given {@linkplain ChangeMode change mode}. If the change mode is
     *         {@link ChangeMode#DELETE} or {@link ChangeMode#RESET}, then an empty array should be returned.
     */
    default Optional<Class<?>[]> acceptsChange(ChangeMode mode) {
        return Optional.empty();
    }

    /**
     * Changes this expression with the given values according to the given mode
     * @param ctx the event
     * @param changeWith the values to change this Expression with
     * @param changeMode the mode of change
     */
    default void change(TriggerContext ctx, Object[] changeWith, ChangeMode changeMode) {}

    /**
     * Gets a single value out of this Expression
     * @param ctx the event
     * @return the single value of this Expression, or {@code null} if it has no value
     * @throws SkriptRuntimeException if the expression returns more than one value
     */
    default Optional<? extends T> getSingle(TriggerContext ctx) {
        var values = getValues(ctx);
        if (values.length == 0) {
            return Optional.empty();
        } else if (values.length > 1) {
            throw new SkriptRuntimeException("Can't call getSingle on an expression that returns multiple values !");
        } else {
            return Optional.ofNullable(values[0]);
        }
    }

    /**
     * @return whether this expression returns a single value. By default, this is defined on registration, but it can
     * be overriden.
     */
    default boolean isSingle() {
        for (var info : SyntaxManager.getAllExpressions()) {
            if (info.getSyntaxClass() == getClass()) {
                return info.getReturnType().isSingle();
            }
        }
        throw new SkriptParserException("Unregistered expression class : " + getClass().getName());
    }

    /**
     * @return the return type of this expression. By default, this is defined on registration, but, like {@linkplain #isSingle()}, can be overriden.
     */
    default Class<? extends T> getReturnType() {
        return SyntaxManager.getExpressionExact(this)
                .orElseThrow(() -> new SkriptParserException("Unregistered expression class : " + getClass().getName()))
                .getReturnType()
                .getType()
                .getTypeClass();
    }

    /**
     * @param ctx the event
     * @return an iterator of the values of this expression
     */
    default Iterator<? extends T> iterator(TriggerContext ctx) {
        return Arrays.asList(getValues(ctx)).iterator();
    }

    /**
     * Converts this expression from it's current type ({@link T}) to another type, using
     * {@linkplain Converters converters}.
     * @param to the class of the type to convert this Expression to
     * @param <C> the type to convert this Expression to
     * @return a converted Expression, or {@code null} if it couldn't be converted
     */
    default <C> Optional<? extends Expression<C>> convertExpression(Class<C> to) {
        return ConvertedExpression.newInstance(this, to);
    }

    /**
     * When this expression is (possibly) looped, returns what the "loop type" (as in {@literal loop-<loop type>})
     * should be in order to describe each element of the values of this expression.
     * @param s the "loop type"
     * @return whether the given "loop type" describes this expression's elements. By default, returns {@code true} if
     * the parameter is {@code "value"}
     * @see ExprLoopValue
     * @see SecLoop
     */
    default boolean isLoopOf(String s) {
        return s.equals("value");
    }

    default boolean isAndList() {
        return true;
    }

    default void setAndList(boolean isAndList) {
    }

    default Expression<?> getSource() {
        return this;
    }

    /*
     * Maybe later.
     */
    default Expression<? extends T> simplify() {
        return this;
    }

    /**
     * Checks this expression against the given {@link Predicate}
     * @param ctx the event
     * @param predicate the predicate
     * @return whether the expression matches the predicate
     */
    default boolean check(TriggerContext ctx, Predicate<? super T> predicate) {
        return check(ctx, predicate, false);
    }

    /**
     * Checks this expression against the given {@link Predicate}
     * @param ctx the event
     * @param predicate the predicate
     * @param negated whether the result should be inverted
     * @return whether the expression matches the predicate
     */
    default boolean check(TriggerContext ctx, Predicate<? super T> predicate, boolean negated) {
        return check(getValues(ctx), predicate, negated, isAndList());
    }

    /**
     * Checks an array of elements against a given predicate
     * @param all the array to check
     * @param predicate the predicate
     * @param invert whether the result should be inverted
     * @param and whether all elements of the array should match the predicate, or only one
     * @param <T> the type of the elements to check
     * @return whether the elements match the given predicate
     */
    static <T> boolean check(T[] all, Predicate<? super T> predicate, boolean invert, boolean and) {
        var hasElement = false;
        for (var t : all) {
            if (t == null)
                continue;
            hasElement = true;
            var b = predicate.test(t);
            if (and && !b)
                return invert;
            if (!and && b)
                return !invert;
        }
        if (!hasElement)
            return invert;
        return invert != and;
    }

    /**
     * Checks if these two expressions have a common superclass. If not, the following process is started:
     * <ol>
     *     <li>The first expression is converted to the second one.</li>
     *     <li>If failed, the second one is converted to the first one.</li>
     *     <li>If failed, the same instances are returned as a pair.</li>
     * </ul>
     * Otherwise, a pair of the same instances is returned.
     * Note that all these operation fail if the converted type would be the Object type.
     * @param first the first expression
     * @param second the second expression
     * @return the converted expressions as a pair
     */
    static <T, U> Pair<Expression<?>, Expression<?>> convertPair(Expression<T> first, Expression<U> second) {
        // If the common superclass is an invalid type or is the Object type representation, we'll need toi convert.
        var commonType = TypeManager.getByClass(
                ClassUtils.getCommonSuperclass(first.getReturnType(), second.getReturnType())
        );
        Type<Object> objectType = TypeManager.getByClassExact(Object.class).orElseThrow(AssertionError::new);

        if (commonType.isPresent() && !commonType.get().equals(objectType))
            return new Pair<>(first, second);

        // We convert the expressions because their types currently are not similar.
        var firstConverted = first.convertExpression(second.getReturnType());
        if (firstConverted.isPresent()) {
            commonType = TypeManager.getByClass(
                    ClassUtils.getCommonSuperclass(firstConverted.get().getReturnType(), second.getReturnType())
            );
            if (commonType.isPresent() && !commonType.get().equals(objectType)) {
                return new Pair<>(firstConverted.get(), second);
            }
        }

        var secondConverted = second.convertExpression(first.getReturnType());
        if (secondConverted.isPresent()) {
            commonType = TypeManager.getByClass(
                    ClassUtils.getCommonSuperclass(secondConverted.get().getReturnType(), first.getReturnType())
            );
            if (commonType.isPresent() && !commonType.get().equals(objectType)) {
                return new Pair<>(first, secondConverted.get());
            }
        }

        return new Pair<>(first, second);
    }

    @SuppressWarnings("unchecked")
    static <S extends CodeSection> List<? extends S> getMatchingSections(ParserState parserState,
                                                                         Class<? extends S> sectionClass) {
        List<S> result = new ArrayList<>();
        for (var section : parserState.getCurrentSections()) {
            if (sectionClass.isAssignableFrom(section.getClass())) {
                result.add((S) section);
            }
        }
        return result;
    }

    static <S extends CodeSection> Optional<? extends S> getLinkedSection(ParserState parserState,
                                                                      Class<? extends S> sectionClass) {
        return getLinkedSection(parserState, sectionClass, l -> l.stream().findFirst());
    }

    static <S extends CodeSection> Optional<? extends S> getLinkedSection(ParserState parserState,
                                                                          Class<? extends S> sectionClass,
                                                                          Function<? super List<? extends S>, Optional<? extends S>> selector) {
        return selector.apply(getMatchingSections(parserState, sectionClass));
    }
}
