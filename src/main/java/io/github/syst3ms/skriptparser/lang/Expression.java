package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.expressions.ExprLoopValue;
import io.github.syst3ms.skriptparser.lang.base.ConvertedExpression;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.sections.SecLoop;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.types.conversions.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An expression, i.e a {@link SyntaxElement} representing a value with some type.
 * @param <T> the type of value this expression returns
 */
public interface Expression<T> extends SyntaxElement {
    /**
     * Retrieves all values of this Expression, accounting for possible modifiers.
     * This means that if this is an {@linkplain #isAndList() or-list}, it will choose
     * a random value to return.
     * @param ctx the event
     * @return an array of the values
     * @see #getArray(TriggerContext)
     */
    T[] getValues(TriggerContext ctx);

    /**
     * Retrieves all values of this Expressions, without accounting for possible modifiers.
     * This means that if this is an {@linkplain #isAndList() or-list}, it will still
     * return all possible values.
     * @param ctx the event
     * @return an array of the raw values
     */
    default T[] getArray(TriggerContext ctx) {
        return getValues(ctx);
    }

    /**
     * Gets a single value out of this Expression
     * @param ctx the event
     * @return the single value of this Expression, or empty if it has no value
     * @throws SkriptRuntimeException if the expression returns more than one value
     */
    default Optional<? extends T> getSingle(TriggerContext ctx) {
        var values = getValues(ctx);
        if (values.length == 0) {
            return Optional.empty();
        } else if (values.length > 1) {
            throw new SkriptRuntimeException("Can't call getSingle on an expression that returns multiple values!");
        } else {
            return Optional.ofNullable(values[0]);
        }
    }

    /**
     * @return whether this expression returns a single value. By default, this is defined on registration, but it can
     * be overridden.
     */
    default boolean isSingle() {
        return SyntaxManager.getExpressionExact(this)
                .orElseThrow(() -> new SkriptParserException("Unregistered expression class: " + getClass().getName()))
                .getReturnType()
                .isSingle();
    }

    /**
     * @return the return type of this expression. By default, this is defined on registration, but, like {@linkplain #isSingle()}, can be overriden.
     */
    default Class<? extends T> getReturnType() {
        return SyntaxManager.getExpressionExact(this)
                .orElseThrow(() -> new SkriptParserException("Unregistered expression class: " + getClass().getName()))
                .getReturnType()
                .getType()
                .getTypeClass();
    }

    /**
     * Determines whether this expression can be changed according to a specific {@link ChangeMode}, and what type
     * of values it can be changed with.
     * @param mode the mode this Expression would be changed with
     * @return an array of classes describing what types this Expression can be changed with, empty if it
     *         shouldn't be changed with the given {@linkplain ChangeMode change mode}. If the change mode is
     *         {@link ChangeMode#DELETE} or {@link ChangeMode#RESET}, then an empty array should be returned.
     */
    default Optional<Class<?>[]> acceptsChange(ChangeMode mode) {
        return Optional.empty();
    }

    /**
     * Determines whether this expression can be changed to a specific {@link ChangeMode} and type class.
     * @param mode the mode this Expression would be changed with
     * @param changeWith the Expression you want to change with
     * @return whether or not this Expression should be changed with the given {@linkplain ChangeMode change mode} and class.
     */
    default boolean acceptsChange(ChangeMode mode, Expression<?> changeWith) {
        return acceptsChange(mode, changeWith.getReturnType(), changeWith.isSingle());
    }

    /**
     * Determines whether this expression can be changed to a specific {@link ChangeMode} and type class.
     * @param mode the mode this Expression would be changed with
     * @param needle the type class of the instance this Expression would be changed with
     * @param isSingle whether or not the instance this Expression would be changed with is single
     * @return whether or not this Expression should be changed with the given {@linkplain ChangeMode change mode} and class.
     */
    default boolean acceptsChange(ChangeMode mode, Class<?> needle, boolean isSingle) {
        if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET)
            throw new UnsupportedOperationException();
        for (var haystack : acceptsChange(mode).orElse(new Class[0])) {
            if (!haystack.isArray() && !isSingle)
                continue;
            if (haystack.isArray())
                haystack = haystack.getComponentType();
            if (haystack.isAssignableFrom(needle))
                return true;
        }
        return false;
    }

    /**
     * Changes this expression with the given values according to the given mode
     * @param ctx the event
     * @param changeMode the mode of change
     * @param changeWith the values to change this Expression with
     */
    default void change(TriggerContext ctx, ChangeMode changeMode, Object[] changeWith) { /* Nothing */ }

    /**
     * @param ctx the event
     * @return an iterator of the values of this expression
     */
    default Iterator<? extends T> iterator(TriggerContext ctx) {
        return Arrays.asList(getValues(ctx)).iterator();
    }

    /**
     * @param ctx the event
     * @return a stream of the values of this expression
     */
    default Stream<? extends T> stream(TriggerContext ctx) {
        return Arrays.stream(getValues(ctx));
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
     * When this expression is looped, returns what the loop reference (as in {@literal loop-<reference>})
     * should be in order to describe each element of the values of this expression.
     * @param s the loop reference
     * @return whether the given reference describes this expression's elements. By default, returns {@code true} if
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

    default void setAndList(boolean isAndList) { /* Nothing*/ }

    default Expression<?> getSource() {
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
        return check(getArray(ctx), predicate, negated, isAndList());
    }

    /**
     * Checks an array of elements against a given predicate
     * @param all the array to check
     * @param predicate the predicate
     * @param negated whether the result should be inverted
     * @param and whether all elements of the array should match the predicate, or only one
     * @param <T> the type of the elements to check
     * @return whether the elements match the given predicate
     */
    static <T> boolean check(T[] all, Predicate<? super T> predicate, boolean negated, boolean and) {
        boolean hasElement = false;
        for (var t : all) {
            if (t == null)
                continue;
            hasElement = true;
            boolean b = predicate.test(t);
            if (and && !b)
                return negated;
            if (!and && b)
                return !negated;
        }
        if (!hasElement)
            return negated;
        return negated != and;
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
