package io.github.bensku.skript;

/**
 * Thrown when a pattern is invalid.
 * 
 */
public class InvalidPatternException extends RuntimeException {

    private static final long serialVersionUID = 0L;
    
    public InvalidPatternException(String msg) {
        super(msg);
    }
}
