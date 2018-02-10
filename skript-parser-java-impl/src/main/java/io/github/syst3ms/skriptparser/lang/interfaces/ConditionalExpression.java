package io.github.syst3ms.skriptparser.lang.interfaces;

import io.github.syst3ms.skriptparser.lang.Expression;

/**
 * An empty interface, used to mark that an {@link Expression <Boolean>} can't be used as-is,
 * and that it must be used in a condition or inside {@code whether %boolean%}
 */
public interface ConditionalExpression {}
