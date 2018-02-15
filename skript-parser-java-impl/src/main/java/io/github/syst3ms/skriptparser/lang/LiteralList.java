package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.types.ClassUtils;
import io.github.syst3ms.skriptparser.util.Expressions;

import java.lang.reflect.Array;

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
	public <R> Literal<? extends R> getConvertedExpression(final Class<R>... to) {
		final Literal<? extends R>[] exprs = new Literal[expressions.length];
		final Class<?>[] classes = new Class[expressions.length];
		for (int i = 0; i < exprs.length; i++) {
			if ((exprs[i] = (Literal<? extends R>) Expressions.convertExpression(expressions[i], to)) == null)
				return null;
			classes[i] = Expressions.getReturnType(exprs[i]);
		}
		return new LiteralList<>(exprs, (Class<R>) ClassUtils.getCommonSuperclass(classes), and, this);
	}

	@Override
	public boolean isSingle() {
		return single;
	}
}
