package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.classes.ChangeMode;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConvertedExpression;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * An expression, i.e a {@link SyntaxElement syntax element} representing a value with some type.
 * @param <T> the type of value this expression returns
 */
public interface Expression<T> extends SyntaxElement {
    /**
     * Retrieves all values of this Expression. This should not return null ! Doing so will most likely throw a NPE in
     * the next few instructions.
     * @param e the event
     * @return an array of the values
     */
    T[] getValues(TriggerContext e);

    /*
     * This is staying until we figure out a better way to implement this
     */
    default T[] getArray(TriggerContext e) {
        return getValues(e);
    }

    /**
     * Determines whether this expression can be changed according to a specific {@link ChangeMode}, and what type
     * of values it can be changed with.
     * @param mode the mode this Expression would be changed with
     * @return an array of classes describing what types this Expression can be changed with, or {@code null} if it
     *         shouldn't be changed with the given {@linkplain ChangeMode change mode}. If the change mode is
     *         {@link ChangeMode#DELETE} or {@link ChangeMode#RESET}, then an empty array should be returned.
     */
    @Nullable
    default Class<?>[] acceptsChange(ChangeMode mode) {
        return null;
    }

    /**
     * Changes this expression with the given values according to the given mode
     * @param e the event
     * @param changeWith the values to change this Expression with
     * @param changeMode the mode of change
     */
    default void change(TriggerContext e, Object[] changeWith, ChangeMode changeMode) {}

    /**
     * Gets a single value out of this Expression
     * @param e the event
     * @return the single value of this Expression, or {@code null} if it has no value
     * @throws SkriptRuntimeException if the expression returns more than one value
     */
    @Nullable
    default T getSingle(TriggerContext e) {
        T[] values = getValues(e);
        if (values.length == 0) {
            return null;
        } else if (values.length > 1) {
            throw new SkriptRuntimeException("Can't call getSingle on an expression that returns multiple values !");
        } else {
            return values[0];
        }
    }

    /**
     * @return whether this expression returns a single value. By default, this is defined on registration, but it can
     * be overriden.
     */
    default boolean isSingle() {
        for (ExpressionInfo<?, ?> info : SyntaxManager.getAllExpressions()) {
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
        ExpressionInfo<?, T> info = SyntaxManager.getExpressionExact(this);
        if (info == null) {
            throw new SkriptParserException("Unregistered expression class : " + getClass().getName());
        }
        return info.getReturnType().getType().getTypeClass();
    }

    /**
     * @param e the event
     * @return an iterator, used inside of a {@linkplain Loop loop}
     */
    default Iterator<? extends T> iterator(TriggerContext e) {
        return CollectionUtils.iterator(getValues(e));
    }

    /**
     * Converts this expression from it's current type ({@link T}) to another type, using
     * {@linkplain io.github.syst3ms.skriptparser.types.conversions.Converters converters}.
     * @param to the class of the type to convert this Expression to
     * @param <C> the type to convert this Expression to
     * @return a converted Expression, or {@code null} if it couldn't be converted
     */
    @Nullable
    default <C> Expression<C> convertExpression(Class<C> to) {
        return ConvertedExpression.newInstance(this, to);
    }

    /**
     * When this expression is (possibly) looped, returns what the "loop type" (as in {@literal loop-<loop type>})
     * should be in order to describe each element of the values of this expression.
     * @param s the "loop type"
     * @return whether the given "loop type" describes this expression's elements. By default, returns {@code true} if
     * the parameter is {@code "value"}
     * @see io.github.syst3ms.skriptparser.expressions.ExprLoopValue
     * @see Loop
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
     * @param e the event
     * @param predicate the predicate
     * @return whether the expression matches the predicate
     */
    default boolean check(TriggerContext e, Predicate<? super T> predicate) {
        return check(e, predicate, false);
    }

    /**
     * Checks this expression against the given {@link Predicate}
     * @param e the event
     * @param predicate the predicate
     * @param negated whether the result should be inverted
     * @return whether the expression matches the predicate
     */
    default boolean check(TriggerContext e, Predicate<? super T> predicate, boolean negated) {
        return check(getValues(e), predicate, negated, isAndList());
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
    @Contract("null, _, _, _ -> false")
    static <T> boolean check(@Nullable T[] all, Predicate<? super T> predicate, boolean invert, boolean and) {
        if (all == null)
            return false;
        boolean hasElement = false;
        for (T t : all) {
            if (t == null)
                continue;
            hasElement = true;
            boolean b = predicate.test(t);
            if (and && !b)
                return invert;
            if (!and && b)
                return !invert;
        }
        return hasElement && invert ^ and;
    }

}
