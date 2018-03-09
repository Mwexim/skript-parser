package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.classes.ChangeMode;
import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.base.ConvertedExpression;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.util.CollectionUtils;

import java.util.Iterator;
import java.util.function.Predicate;

public interface Expression<T> extends SyntaxElement {
    T[] getValues(Event e);

    /*
     * This is staying until we figure out a better way to implement this
     */
    default T[] getArray(Event e) {
        return getValues(e);
    }

    default Class<?>[] acceptsChange(ChangeMode mode) {
        return null;
    }

    default void change(Event e, Object[] changeWith, ChangeMode changeMode) {}

    default T getSingle(Event e) {
        T[] values = getValues(e);
        if (values.length == 0) {
            return null;
        } else if (values.length > 1) {
            throw new SkriptRuntimeException("Can't call getSingle on an expression that returns multiple values !");
        } else {
            return values[0];
        }
    }

    default boolean isSingle() {
        for (ExpressionInfo<?, ?> info : SyntaxManager.getAllExpressions()) {
            if (info.getSyntaxClass() == getClass()) {
                return info.getReturnType().isSingle();
            }
        }
        throw new SkriptParserException("Unregistered expression class : " + getClass().getName());
    }

    default Class<? extends T> getReturnType() {
        ExpressionInfo<?, T> info = SyntaxManager.getExpressionExact(this);
        if (info == null) {
            throw new SkriptParserException("Unregistered expression class : " + getClass().getName());
        }
        return info.getReturnType().getType().getTypeClass();
    }

    default Iterator<? extends T> iterator(Event e) {
        return CollectionUtils.iterator(getValues(e));
    }

    default <C> Expression<C> convertExpression(Class<C> to) {
        return ConvertedExpression.newInstance(this, to);
    }

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

    default Expression<? extends T> simplify() {
        return this;
    }

    default boolean check(final Event e, final Predicate<? super T> c) {
        return check(e, c, false);
    }

    default boolean check(final Event e, final Predicate<? super T> c, final boolean negated) {
        return check(getValues(e), c, negated, isAndList());
    }

    static <T> boolean check(final T[] all, final Predicate<? super T> c, final boolean invert, final boolean and) {
        if (all == null) return false;
        boolean hasElement = false;
        for (final T t : all) {
            if (t == null) continue;
            hasElement = true;
            final boolean b = c.test(t);
            if (and && !b)
                return invert;
            if (!and && b)
                return !invert;
        }
        return hasElement && invert ^ and;
    }

}
