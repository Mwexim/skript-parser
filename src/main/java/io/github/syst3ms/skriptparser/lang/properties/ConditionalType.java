package io.github.syst3ms.skriptparser.lang.properties;

/**
 * @see PropertyConditional
 */
public enum ConditionalType {
	/**
	 * The property is of the form {@code something is something},
	 * plurality and negation supported.
	 */
	BE,

	/**
	 * The property is of the form {@code something can something},
	 * plurality and negation supported.
	 */
	CAN,

	/**
	 * The property is of the form {@code something has something},
	 * plurality and negation supported.
	 */
	HAVE
}
