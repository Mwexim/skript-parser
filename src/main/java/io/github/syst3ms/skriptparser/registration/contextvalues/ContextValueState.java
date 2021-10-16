package io.github.syst3ms.skriptparser.registration.contextvalues;

/**
 * An enum to indicate the relative position in time between two similar context values.
 * Note that this is just to <b>indicate</b> time difference.
 */
public enum ContextValueState {
	/**
	 * The context value indicates something before the event happened.
	 */
	PAST,

	/**
	 * The context value indicates something during the event.
	 */
	PRESENT,

	/**
	 * The context value indicates something that changed during the event related to its state in the past,
	 * or something that will change after the event.
	 */
	FUTURE
}
