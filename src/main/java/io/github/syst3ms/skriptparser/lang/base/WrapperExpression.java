package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.classes.ChangeMode;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Represents an expression which is a wrapper of another one. Remember to set the wrapped expression with {@link #setExpr(Expression)} in
 * {@link SyntaxElement#init(Expression[], int, ParseResult) init()}.<br/>
 * If you override {@link #getValues(TriggerContext)} (Event)} you must override {@link #iterator(TriggerContext)} as well. Effects of not
 * doing so are unspecified.
 *
 * @author Peter Güttinger
 */
public abstract class WrapperExpression<T> implements Expression<T> {

    private Expression<? extends T> expr;

    protected WrapperExpression() {}

    /**
     * Sets wrapped expression. Parser instance is automatically copied from
     * this expression.
     * @param expr Wrapped expression.
     */
    protected void setExpr(Expression<? extends T> expr) {
        this.expr = expr;
    }

    public Expression<?> getExpr() {
        return expr;
    }

    @Override
    @Nullable
    public final <R> Expression<R> convertExpression(Class<R> to) {
        @SuppressWarnings("unchecked")
        Function<? super T, ? extends R> conv = (Function<? super T, ? extends R>) Converters.getConverter(getReturnType(), to);
        if (conv == null)
            return null;
        return new ConvertedExpression<T, R>(expr, to, conv) {
            @Override
            public String toString(@Nullable TriggerContext e, boolean debug) {
                if (debug && e == null)
                    return "(" + WrapperExpression.this.toString(null, true) + ")->" + to.getName();
                return WrapperExpression.this.toString(e, debug);
            }
        };
    }

    @Override
    public T[] getValues(TriggerContext e) {
        return expr.getValues(e);
    }

    @Override
    public T[] getArray(TriggerContext e) {
        return expr.getArray(e);
    }

    @Override
    public Iterator<? extends T> iterator(TriggerContext e) {
        return expr.iterator(e);
    }

    @Override
    public boolean isSingle() {
        return expr.isSingle();
    }

    @Override
    public boolean isAndList() {
        return expr.isAndList();
    }

    @Override
    public Class<? extends T> getReturnType() {
        return expr.getReturnType();
    }

    @Override
    public Class<?>[] acceptsChange(ChangeMode mode) {
        return expr.acceptsChange(mode);
    }

    @Override
    public void change(TriggerContext e, Object[] changeWith, ChangeMode mode) {
        expr.change(e, changeWith, mode);
    }

    @Override
    public Expression<? extends T> simplify() {
        return expr;
    }
}