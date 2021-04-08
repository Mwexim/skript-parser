package io.github.syst3ms.skriptparser.lang.control;

import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.sections.SecLoop;
import io.github.syst3ms.skriptparser.sections.SecWhile;

import java.util.Optional;

/**
 * Statements implementing this interface are by convention returning themselves as the next statement
 * in {@link Statement#getNext()}.
 * <br>
 * When creating syntax, one may need to take this into account. This is mostly done by sections that
 * iterate over their respective contents multiple times.
 * @see SecWhile
 * @see SecLoop
 */
public interface SelfReferencing {
	/**
	 * This statement returns itself as the next statement to run
	 * in {@link Statement#getNext() getNext()}.
	 * <br>
	 * This method will return the actual statement that follows this statement.
	 * This means, by convention, the next element that is not nested more than this statement.
	 * @return the element that is actually after this section
	 */
	Optional<Statement> getActualNext();
}
