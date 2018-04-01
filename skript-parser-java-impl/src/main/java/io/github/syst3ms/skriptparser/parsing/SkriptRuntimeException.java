package io.github.syst3ms.skriptparser.parsing;

/**
 * Is thrown when something pertaining to misusage/unexpected behaviour occurs
 */
public class SkriptRuntimeException extends RuntimeException {
    public SkriptRuntimeException(String msg) {
        super(msg);
    }
}
