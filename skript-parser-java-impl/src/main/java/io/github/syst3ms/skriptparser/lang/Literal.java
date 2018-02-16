package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.Event;

public interface Literal<T> extends Expression<T> {
	T[] getValues();

	default T getSingle() {
		return getSingle(null);
	}

	@Override
	default T[] getValues(Event e) {
		return getValues();
	}
}
