package io.github.syst3ms.skriptparser.lang.control;

import io.github.syst3ms.skriptparser.effects.EffContinue;
import io.github.syst3ms.skriptparser.effects.EffExit;

public interface Finishing {
	/**
	 * When the execution of this section is completely done, this method, by convention, needs to be fired.
	 * One could reset cache or delete iterations as this method will also be called by various other
	 * syntaxes to delete cache.
	 * @see EffExit
	 * @see EffContinue
	 */
	void finish();
}
