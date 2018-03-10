package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.util.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("ConstantConditions")
public class LiteralList<T> extends ExpressionList<T> implements Literal<T> {

    public LiteralList(Literal<? extends T>[] literals, Class<T> returnType, boolean and) {
        super(literals, returnType, and);
    }

    public LiteralList(Literal<? extends T>[] literals, Class<T> returnType, boolean and, LiteralList<?> source) {
        super(literals, returnType, and, source);
    }

    @Override
    public T[] getValues() {
        return getValues(null);
    }

    @Nullable
    @Override
    public T getSingle() {
        return getSingle(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> Expression<R> convertExpression(Class<R> to) {
        Literal<? extends R>[] exprs = new Literal[expressions.length];
        Class<?>[] classes = new Class[expressions.length];
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
