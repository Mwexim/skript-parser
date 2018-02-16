package io.github.syst3ms.skriptparser.types;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ConvertedExpression<F, T> implements Expression<T> {
	private Expression<? extends F> source;
	private Class<T> to;
	private Function<? super F, ? extends T> converter;

	private ConvertedExpression(Expression<? extends F> source, Class<T> to, Function<? super F, ? extends T> converter) {
		this.source = source;
		this.to = to;
		this.converter = converter;
	}

	@SafeVarargs
	public static <F, T> ConvertedExpression<F, T> newInstance(final Expression<F> v, Class<T>... to) {
		for (final Class<T> c : to) {
			assert c != null;
			// casting <? super ? extends F> to <? super F> is wrong, but since the converter is only used for values returned by the expression
			// (which are instances of "<? extends F>") this won't result in any ClassCastExceptions.
			@SuppressWarnings("unchecked")
			final Function<? super F, ? extends T> conv = (Function<? super F, ? extends T>) Converters.getConverter(v.getReturnType(), c);
			if (conv == null)
				continue;
			return new ConvertedExpression<>(v, c, conv);
		}
		return null;
	}

	@Override
	public T[] getValues(Event e) {
		return Converters.convert(source.getValues(e), to, converter);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString(Event e, boolean debug) {
		if (debug && e == null)
			return "(" + source.toString(null, true) + " >> " + converter + ": " + source.getReturnType().getName() + "->" + to.getName() + ")";
		return source.toString(e, debug);
	}

	@Override
	public boolean isSingle() {
		return source.isSingle();
	}

	@SuppressWarnings("unchecked")
	public Class<T> getReturnType() {
		return to;
	}

	@Override
	public boolean isLoopOf(String loop) {
		return false;
	}

	@Override
	public Expression<? extends F> getSource() {
		return source;
	}

	@Override
	public Iterator<? extends T> iterator(Event event) {
		Iterator<? extends F> sourceIterator = source.iterator(event);
		if (sourceIterator == null)
			return Collections.emptyIterator();
		return new Iterator<T>() {
			T next = null;

			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				while (next == null && sourceIterator.hasNext()) {
					final F f = sourceIterator.next();
					next = f == null ? null : converter.apply(f);
				}
				return next != null;
			}

			@Override
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				final T n = next;
				next = null;
				assert n != null;
				return n;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
