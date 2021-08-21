package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

/**
 * A list of expressions
 * @param <T> the common supertype of all expressions in this list
 */
@SuppressWarnings("unchecked")
public class ExpressionList<T> implements Expression<T> {
    protected final boolean single;
    protected boolean and;
    protected Expression<? extends T>[] expressions;
    private final Class<T> returnType;
    @Nullable
    private final ExpressionList<?> source;

    public ExpressionList(Expression<? extends T>[] expressions, Class<T> returnType, boolean and) {
        this(expressions, returnType, and, null);
    }

    protected ExpressionList(Expression<? extends T>[] expressions, Class<T> returnType, boolean and, @Nullable ExpressionList<?> source) {
        assert expressions.length > 1;
        this.expressions = expressions;
        this.returnType = returnType;
        this.and = and;
        if (and) {
            single = false;
        } else {
            var single = true;
            for (Expression<?> e : expressions) {
                assert e != null;
                if (!e.isSingle()) {
                    single = false;
                    break;
                }
            }
            this.single = single;
        }
        this.source = source;
    }

    @Override
    @Contract("_, _, _ -> fail")
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T[] getValues(TriggerContext ctx) {
        return getValues(expr -> expr.getValues(ctx));
    }

    /**
     * Retrieves all values of this Expression using a function that will be applied to each expression.
     * @param function the function
     * @return an array of the values
     */
    public T[] getValues(Function<Expression<? extends T>, T[]> function) {
        if (and) {
            List<T> values = new ArrayList<>();
            for (var expr : expressions) {
                Collections.addAll(values, function.apply(expr));
            }
            return values.toArray((T[]) Array.newInstance(returnType, values.size()));
        } else {
            var shuffle = Arrays.asList(expressions);
            Collections.shuffle(shuffle);
            for (var expr : shuffle) {
                var values = function.apply(expr);
                if (values.length > 0)
                    return values;
            }
        }
        return (T[]) Array.newInstance(returnType, 0);
    }

    @Override
    public T[] getArray(TriggerContext ctx) {
        List<T> values = new ArrayList<>();
        for (var expr : expressions) {
            Collections.addAll(values, expr.getArray(ctx));
        }
        return values.toArray((T[]) Array.newInstance(returnType, values.size()));
    }

    @Override
    public boolean isSingle() {
        return single;
    }

    @Override
    public Class<T> getReturnType() {
        return returnType;
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        var sb = new StringBuilder();
        for (var i = 0; i < expressions.length; i++) {
            if (i > 0) {
                if (i == expressions.length - 1) {
                    sb.append(and ? " and " : " or ");
                } else {
                    sb.append(", ");
                }
            }
            sb.append(expressions[i].toString(ctx, debug));
        }
        return sb.toString();
    }

    @Override
    public <R> Optional<? extends Expression<R>> convertExpression(Class<R> to) {
        Expression<? extends R>[] exprs = new Expression[expressions.length];
        for (var i = 0; i < exprs.length; i++)
            if ((exprs[i] = expressions[i].convertExpression(to).orElse(null)) == null)
                return Optional.empty();
        return Optional.of(new ExpressionList<>(exprs, (Class<R>) ClassUtils.getCommonSuperclass(to), and, this));
    }

    @Override
    public Iterator<? extends T> iterator(TriggerContext ctx) {
        if (!and) {
            var shuffle = Arrays.asList(expressions);
            Collections.shuffle(shuffle);
            for (var expression : shuffle) {
                var it = expression.iterator(ctx);
                if (it.hasNext())
                    return it;
            }
            return Collections.emptyIterator();
        }
        return new Iterator<>() {
            private int i = 0;
            @Nullable
            private Iterator<? extends T> current = null;

            @Override
            public boolean hasNext() {
                var c = current;
                while (i < expressions.length && (c == null || !c.hasNext()))
                    current = c = expressions[i++].iterator(ctx);
                return c != null && c.hasNext();
            }

            @Override
            public T next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                var c = current;
                if (c == null)
                    throw new NoSuchElementException();
                return c.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public boolean isLoopOf(String s) {
        for (Expression<?> e : expressions)
            if (!e.isSingle() && e.isLoopOf(s))
                return true;
        return false;
    }

    @Override
    public boolean isAndList() {
        return and;
    }

    @Override
    public void setAndList(boolean isAndList) {
        this.and = isAndList;
    }

    @Override
    public Expression<?> getSource() {
        return source != null ? source : this;
    }
}
