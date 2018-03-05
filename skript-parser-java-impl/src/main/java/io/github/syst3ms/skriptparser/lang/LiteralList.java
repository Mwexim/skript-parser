package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.types.ClassUtils;

public class LiteralList<T> extends ExpressionList<T> implements Literal<T> {

    public LiteralList(final Literal<? extends T>[] literals, final Class<T> returnType, final boolean and) {
        super(literals, returnType, and);
    }

    public LiteralList(final Literal<? extends T>[] literals, final Class<T> returnType, final boolean and, final LiteralList<?> source) {
        super(literals, returnType, and, source);
    }

    @Override
    public T[] getValues() {
        return getValues(null);
    }

    @SuppressWarnings("null")
    @Override
    public T getSingle() {
        return getSingle(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> Expression<R> convertExpression(final Class<R>... to) {
        final Literal<? extends R>[] exprs = new Literal[expressions.length];
        final Class<?>[] classes = new Class[expressions.length];
        for (int i = 0; i < exprs.length; i++) {
            if ((exprs[i] = (Literal<? extends R>) expressions[i].convertExpression(to)) == null)
                return null;
            classes[i] = exprs[i].getReturnType();
        }
        return new LiteralList<>(exprs, (Class<R>) ClassUtils.getCommonSuperclass(classes), and, this);
    }

    @Override
    public boolean isSingle() {
        return single;
    }
}
