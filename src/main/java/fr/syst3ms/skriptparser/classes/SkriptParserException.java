package fr.syst3ms.skriptparser.classes;

/**
 * Thrown when a pattern is invalid.
 *
 */
public class SkriptParserException extends RuntimeException {
	private static final long serialVersionUID = 0L;

	public SkriptParserException(String msg) {
		super(msg);
	}
}