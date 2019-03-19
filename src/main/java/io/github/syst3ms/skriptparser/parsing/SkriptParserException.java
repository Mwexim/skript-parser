package io.github.syst3ms.skriptparser.parsing;

/**
 * An exception thrown whenever something goes wrong at parsetime.
 */
public class SkriptParserException extends RuntimeException {
    private static final long serialVersionUID = 0L;

    public SkriptParserException(String msg) {
        super(msg);
    }
}