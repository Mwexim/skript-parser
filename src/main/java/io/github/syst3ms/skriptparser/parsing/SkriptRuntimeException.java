package io.github.syst3ms.skriptparser.parsing;

/**
 * An exception thrown when something goes wrong at runtime
 */
public class SkriptRuntimeException extends RuntimeException {
    public SkriptRuntimeException(String msg) {
        super(msg);
    }
}
