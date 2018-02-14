package io.github.syst3ms.skriptparser.lang.interfaces;

import io.github.syst3ms.skriptparser.lang.Expression;

public interface ConvertibleExpression {
	<R> Expression<? extends R> getConvertedExpression(Class<?>[] to);
}
