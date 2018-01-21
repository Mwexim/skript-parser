package io.github.syst3ms.skriptparser.classes;

/**
 * Thrown whenever something inherent to parsing goes wrong
 */
public class SkriptParserException extends RuntimeException {
	private static final long serialVersionUID = 0L;

	public SkriptParserException(String msg) {
		super(msg);
	}
}