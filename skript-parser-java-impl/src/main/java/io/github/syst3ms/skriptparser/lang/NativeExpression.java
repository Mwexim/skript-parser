package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.lang.interfaces.ConvertibleExpression;
import io.github.syst3ms.skriptparser.lang.interfaces.DynamicNumberExpression;
import io.github.syst3ms.skriptparser.lang.interfaces.LoopableExpression;

/**
 * An interface made for unregistered, native expressions such as {@link Literal} or {@link ExpressionList}
 */
interface NativeExpression<T> extends Expression<T>, DynamicNumberExpression, ConvertibleExpression {}
