package io.github.syst3ms.skriptparser.lang.control;

import io.github.syst3ms.skriptparser.effects.EffContinue;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.sections.SecLoop;

import java.util.Optional;

/**
 * Sections implementing this interface are able to be 'continued' using the {@link EffContinue} effect.
 * Most of the time, these sections are iterating over multiple values, and this interface serves as an
 * indicator to be able to continue to the next iteration instead of executing the rest of the statements.
 * <br>
 * One can easily compare this with Java's {@code continue} keyword.
 * @see EffContinue
 */
public interface Continuable {
	/**
	 * This function is called on the section where the {@link EffContinue} effect will
	 * continue to, taking all Continuable sections into account. Most of the time, this will just
	 * return the section it is referring to, but in rare cases, one might want to change this
	 * behaviour, hence this method.
	 * @param ctx the context
	 * @return the next statement
	 * @see EffContinue
	 * @see SecLoop SecLoop (implementation)
	 */
	Optional<? extends Statement> getContinued(TriggerContext ctx);
}
