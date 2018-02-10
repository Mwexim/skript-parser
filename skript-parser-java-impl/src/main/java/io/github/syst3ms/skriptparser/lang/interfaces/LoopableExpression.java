package io.github.syst3ms.skriptparser.lang.interfaces;

import java.util.Iterator;

public interface LoopableExpression<T> {
	boolean isLoopOf(String loop);

	Iterator<? extends T> iterator();
}
