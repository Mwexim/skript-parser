package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.classes.ChangeMode;
import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.base.ConvertedExpression;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import io.github.syst3ms.skriptparser.parsing.SkriptRuntimeException;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import jdk.nashorn.internal.objects.annotations.Constructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Predicate;

public interface Expression<T> extends SyntaxElement {
    @NotNull
    T[] getValues(Event e);

    /*
     * This is staying until we figure out a better way to implement this
     */
    @NotNull
    default T[] getArray(Event e) {
        return getValues(e);
    }

    @Nullable
    default Class<?>[] acceptsChange(ChangeMode mode) {
        return null;
    }

    default void change(Event e, Object[] changeWith, ChangeMode changeMode) {}

    @Nullable
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

    @NotNull
    default Iterator<? extends T> iterator(Event e) {
        return CollectionUtils.iterator(getValues(e));
    }

    @Nullable
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

    @NotNull
    default Expression<?> getSource() {
        return this;
    }

    /*
     * Maybe later.
     */
    @NotNull
    default Expression<? extends T> simplify() {
        return this;
    }

    default boolean check(Event e, Predicate<? super T> c) {
        return check(e, c, false);
    }

    default boolean check(Event e, Predicate<? super T> c, boolean negated) {
        return check(getValues(e), c, negated, isAndList());
    }

    @Contract("null, _, _, _ -> false")
    static <T> boolean check(@Nullable T[] all, Predicate<? super T> c, boolean invert, boolean and) {
        if (all == null)
            return false;
        boolean hasElement = false;
        for (T t : all) {
            if (t == null)
                continue;
            hasElement = true;
            boolean b = c.test(t);
            if (and && !b)
                return invert;
            if (!and && b)
                return !invert;
        }
        return hasElement && invert ^ and;
    }

}
