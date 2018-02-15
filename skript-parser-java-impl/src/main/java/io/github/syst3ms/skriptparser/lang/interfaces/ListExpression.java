package io.github.syst3ms.skriptparser.lang.interfaces;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;

/**
 * An interface used to take advantage of the behaviour of 'and' and 'or' lists in Skript.
 * <strong>NOT TO BE CONFUSED WITH AN EXPRESSION RETURNING MULTIPLE OBJECTS</strong>
 */
public interface ListExpression<T> extends Expression<T>, LoopableExpression<T>, ConvertibleExpression {
	void setAndList(boolean isAndList);

	boolean isAndList();

	T[] getArray(Event e);
}
