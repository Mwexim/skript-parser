package io.github.syst3ms.skriptparser.log;

/**
 * A type describing an error, mainly used to indicate priority in errors.
 */
public enum ErrorType {
    EXCEPTION, SEMANTIC_ERROR, STRUCTURE_ERROR, MALFORMED_INPUT, NO_MATCH
}
