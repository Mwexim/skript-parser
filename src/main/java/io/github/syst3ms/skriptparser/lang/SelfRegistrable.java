package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;

/**
 * An interface for {@link SyntaxElement SyntaxElements} that
 * register following a certain pattern.
 * Because of this reoccurring pattern, there is no need to make a separate method for
 * it in {@link SkriptRegistration}.
 */
public interface SelfRegistrable {
	/**
	 * Register this syntax class.
	 * @param reg the registration that wants to registers this syntax class
	 * @param args the arguments that were use to register this syntax class
	 */
	void register(SkriptRegistration reg, Object... args);
}
