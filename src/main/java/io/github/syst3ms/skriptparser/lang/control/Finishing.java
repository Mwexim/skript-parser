package io.github.syst3ms.skriptparser.lang.control;

import io.github.syst3ms.skriptparser.effects.EffContinue;
import io.github.syst3ms.skriptparser.effects.EffExit;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.lambda.ArgumentSection;
import io.github.syst3ms.skriptparser.sections.SecLoop;
import io.github.syst3ms.skriptparser.sections.SecMap;
import io.github.syst3ms.skriptparser.sections.SecSwitch;

/**
 * {@linkplain CodeSection}s implementing this interface need to be 'finished' by convention. When this method is not called
 * when moving on to another statement, negative side-effects <b>will always</b> occur if this same
 * statement is being {@linkplain Statement#walk(TriggerContext) walked} on again.
 * <br>
 * Examples of this are cache clearing, iterator clearing or result clearing in respectively sections like
 * {@linkplain SecLoop}, {@linkplain SecSwitch} and {@linkplain SecMap}.
 * <br>
 * {@link ArgumentSection} implements this interface by default, because of their intrinsic nature of needing
 * a finishing method like this.
 * @see SecLoop
 * @see SecSwitch
 * @see SecMap
 */
public interface Finishing {
	/**
	 * By convention, this method should be fired in one of the following occasions:
	 * <ol>
	 *     <li>The execution of the section is completely done and the (actual) next element is
	 *     referenced to be {@linkplain Statement#walk(TriggerContext) walked} on.</li>
	 *     <li>This method is <b>completely</b> stopped by any means whatsoever.</li>
	 * </ol>
	 * An example of this second occasion is {@link EffContinue} continuing over multiple loops.
	 * If that effect continues 3 loops in one go, this means that the 2 most inner-loops need to be
	 * completely reset, since they have the possibility to be looped over again. EffContinue therefore
	 * calls this method on those loops.
	 * <br>
	 * Another example is {@link EffExit}, which finishes every section that implements this interface,
	 * because of the same reasons specified above.
	 * @see EffExit
	 * @see EffContinue
	 */
	void finish();
}
