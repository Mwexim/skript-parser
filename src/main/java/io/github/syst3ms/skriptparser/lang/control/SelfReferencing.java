package io.github.syst3ms.skriptparser.lang.control;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.sections.SecLoop;
import io.github.syst3ms.skriptparser.sections.SecWhile;

import java.util.Optional;

/**
 * Shows that this section references itself. This can be due to multiple iterations of the same code,
 * like a while-loop or a normal loop.
 * @see SecWhile
 * @see SecLoop
 */
public interface SelfReferencing {
	/**
	 * This section actually sets itself as its next element with {@link CodeSection#getNext() getNext()}.
	 * This way it has full control over when to stop iterating.
	 * @return the element that is actually after this section
	 */
	Optional<Statement> getActualNext();
}
