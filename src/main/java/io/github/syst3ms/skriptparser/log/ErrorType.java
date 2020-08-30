package io.github.syst3ms.skriptparser.log;

/**
 * A type describing an error, mainly used to indicate priority in errors.
 */
public enum ErrorType {
    /**
     * An exception was thrown. This is used to handle uncaught exceptions while running code.
     */
    EXCEPTION,
    /**
     * There is a semantic error, i.e the error doesn't come from the written input but rather from its underlying logic.
     */
    SEMANTIC_ERROR,
    /**
     * There is a code structure error that has to do with the structure and formatting of sections and triggers inside
     * a Skript file.
     */
    STRUCTURE_ERROR,
    /**
     * The input is malformed, usually due to special characters being wrongly used (unclosed brackets, quotes, etc.)
     */
    MALFORMED_INPUT,
    /**
     * No match was found for the given input.
     */
    NO_MATCH
}
