package io.github.syst3ms.skriptparser.types.attributes;

import io.github.syst3ms.skriptparser.types.Type;

/**
 * Information about a range function
 *
 * @param <B> the type of the two endpoints
 * @param <R> the type of the range that is returned
 */
public interface Range<B, R> extends Type.Attribute<B> {
	/**
	 * Calculates the range of values between two endpoints. If the lower bound is
	 * smaller than the upper bound, an empty array must be returned by convention.
	 * @param from the lower bound
	 * @param to the upper bound
	 * @return the range
	 */
	R[] apply(B from, B to);

	Class<? extends R> getRelativeType();
}
