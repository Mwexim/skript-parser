package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.interfaces.ConvertibleExpression;
import io.github.syst3ms.skriptparser.lang.interfaces.DynamicNumberExpression;
import io.github.syst3ms.skriptparser.lang.interfaces.ListExpression;

public interface Literal<T> extends NativeExpression<T> {
	T[] getValues();

	default T getSingle() {
		return getSingle(null);
	}

	@Override
	default T[] getValues(Event e) {
		return getValues();
	}
}
