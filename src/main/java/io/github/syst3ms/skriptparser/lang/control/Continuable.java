package io.github.syst3ms.skriptparser.lang.control;

import io.github.syst3ms.skriptparser.effects.EffContinue;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.sections.SecLoop;
import io.github.syst3ms.skriptparser.sections.SecWhile;

import java.util.Optional;

/**
 * Shows that your section can be continued by using the {@link EffContinue continue} effect.
 */
public interface Continuable {
	/**
	 * Sections that need to execute their items multiple times (like loops, maps and while-loops),
	 * can do this in 2 different ways:
	 * <ol>
	 *     <li>by referencing themselves as the next item (like {@link SecWhile})</li>
	 *     <li>by executing the items internally and then proceeding to the next item
	 *     (like {@link SecLoop})</li>
	 * </ol>
	 * The continue-statement needs to handle items in the following way, depending on the way
	 * it is implemented.
	 * <ol>
	 *     <li>It will reference the section as the next item, without walking over it.</li>
	 *     <li>It will {@link Statement#walk(TriggerContext) walk} over the section and
	 *     return nothing, essentially blocking the current chain and creating a new one.</li>
	 * </ol>
	 * If none if these behaviors are desired, one can use their own implementation.
	 * @return the statement to iterate over
	 */
	Optional<? extends Statement> getContinued(TriggerContext ctx);
}
