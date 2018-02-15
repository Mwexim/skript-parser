package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.interfaces.ListExpression;
import io.github.syst3ms.skriptparser.lang.interfaces.LoopableExpression;
import io.github.syst3ms.skriptparser.lang.interfaces.SourcedExpression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.types.ClassUtils;
import io.github.syst3ms.skriptparser.types.Converters;
import io.github.syst3ms.skriptparser.util.Expressions;

import java.lang.reflect.Array;
import java.util.*;

@SuppressWarnings("unchecked")
public class ExpressionList<T> implements ListExpression<T>, SourcedExpression {
	protected final boolean single;
	protected boolean and;
	protected Expression<? extends T>[] expressions;
	private Class<T> returnType;
	private ExpressionList<?> source;
	public ExpressionList(Expression<? extends T>[] expressions, Class<T> returnType, boolean and) {
		this(expressions, returnType, and, null);
	}

	protected ExpressionList(Expression<? extends T>[] expressions, Class<T> returnType, boolean and, ExpressionList<?> source) {
		assert expressions != null && expressions.length > 1;
		this.expressions = expressions;
		this.returnType = returnType;
		this.and = and;
		if (and) {
			single = false;
		} else {
			boolean single = true;
			for (final Expression<?> e : expressions) {
				if (!Expressions.isSingle(e)) {
					single = false;
					break;
				}
			}
			this.single = single;
		}
		this.source = source;
	}

	public Class<T> getReturnType() {
		return returnType;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T[] getArray(Event e) {
		if (and) {
			return getValues(e);
		} else {
			List<Expression<? extends T>> shuffle = Arrays.asList(expressions);
			Collections.shuffle(shuffle);
			for (Expression<? extends T> expr : shuffle) {
				T[] values = expr.getValues(e);
				if (values.length > 0)
					return values;
			}
		}
		return (T[]) Array.newInstance(returnType, 0);
	}

	@Override
	public T[] getValues(Event e) {
		List<T> values = new ArrayList<>();
		for (Expression<? extends T> expression : expressions) {
			Collections.addAll(values, expression.getValues(e));
		}
		return values.toArray((T[]) Array.newInstance(returnType, values.size()));
	}

	@Override
	public String toString(Event e, boolean debug) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < expressions.length; i++) {
			if (i > 0) {
				if (i == expressions.length - 1) {
					sb.append(and ? " and " : " or ");
				} else {
					sb.append(", ");
				}
			}
			Expression<? extends T> expr = expressions[i];
			sb.append(expr.toString(e, debug));
		}
		return sb.toString();
	}

	@Override
	public <R> Expression<? extends R> getConvertedExpression(Class<R>[] to) {
		final Expression<? extends R>[] exprs = new Expression[expressions.length];
		for (int i = 0; i < exprs.length; i++)
			if ((exprs[i] = (Expression<? extends R>) Expressions.convertExpression(expressions[i], to)) == null)
				return null;
		return new ExpressionList<>(exprs, (Class<R>) ClassUtils.getCommonSuperclass(to), and, this);
	}

	@Override
	public boolean isLoopOf(final String s) {
		for (final Expression<?> e : expressions)
			if (e instanceof LoopableExpression && ((LoopableExpression) e).isLoopOf(s) ||
				!Expressions.isSingle(e) && s.equals("value"))
				return true;
		return false;
	}

	public Iterator<? extends T> iterator(final Event e) {
		if (!and) {
			List<Expression<? extends T>> shuffle = Arrays.asList(expressions);
			Collections.shuffle(shuffle);
			for (Expression<? extends T> expression : shuffle) {
				Iterator<? extends T> it = Expressions.iterator(expression, e);
				if (it != null && it.hasNext())
					return it;
			}
			return null;
		}
		return new Iterator<T>() {
			private int i = 0;
			private Iterator<? extends T> current = null;

			@Override
			public boolean hasNext() {
				Iterator<? extends T> c = current;
				while (i < expressions.length && (c == null || !c.hasNext()))
					current = c = Expressions.iterator(expressions[i++], e);
				return c != null && c.hasNext();
			}

			@Override
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				final Iterator<? extends T> c = current;
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
