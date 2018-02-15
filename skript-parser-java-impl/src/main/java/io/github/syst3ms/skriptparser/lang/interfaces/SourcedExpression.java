package io.github.syst3ms.skriptparser.lang.interfaces;

import io.github.syst3ms.skriptparser.lang.Expression;

public interface SourcedExpression {
	Expression<?> getSource();
}
