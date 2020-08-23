package io.github.syst3ms.skriptparser.parsing;

/**
 * An exception thrown whenever something goes wrong at parse time.
 */
public class SkriptParserException extends RuntimeException {
    public SkriptParserException(String msg) {
        super(msg);
    }
}