package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.ExpressionList;
import io.github.syst3ms.skriptparser.lang.TriggerContext;

/**
 * A tagged expression is a string expression that contains tags.
 * Tags are small structures of code that can affect a certain part of the string easily
 * by changing it accordingly. Default, these tags are not parsed unless a
 * {@link TaggedExpression} is used.
 */
public abstract class TaggedExpression implements Expression<String> {
	@Override
	public String[] getValues(TriggerContext ctx) {
		return new String[] {toString(ctx, "default")};
	}

	/**
	 * Returns the value of this string after applying the tags according to the given tag context.
	 * @param ctx the event
	 * @param tagCtx the tag context
	 * @return the applied string
	 */
	public abstract String toString(TriggerContext ctx, String tagCtx);

	/**
	 * Returns the string values of this expression after applying all tags.
	 * This means if the expression is a {@link TaggedExpression}
	 * or an {@link ExpressionList} containing a TaggedExpression,
	 * all the tags will be applied with the given tag context. If not, it will
	 * just return the string values of this expression.
	 * @param expr the single expression
	 * @param ctx the context
	 * @param tagCtx the tag context
	 * @return the strings with optional tags applied
	 */
	@SuppressWarnings("unchecked")
	public static String[] apply(Expression<String> expr, TriggerContext ctx, String tagCtx) {
		if (expr instanceof TaggedExpression) {
			return new String[] {((TaggedExpression) expr).toString(ctx, tagCtx)};
		} else if (expr instanceof ExpressionList) {
			return ((ExpressionList<String>) expr).getValues(val -> apply((Expression<String>) val, ctx, tagCtx));
		} else {
			return expr.getValues(ctx);
		}
	}
}
