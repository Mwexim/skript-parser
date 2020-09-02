package io.github.syst3ms.skriptparser.lang.base;

import io.github.syst3ms.skriptparser.lang.Expression;
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

}
