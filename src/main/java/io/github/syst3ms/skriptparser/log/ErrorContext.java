package io.github.syst3ms.skriptparser.log;

public enum ErrorContext {
    /**
     * A syntax has been successfully parsed all the way, but it's present in a section that doesn't allow it
     */
    RESTRICTED_SYNTAXES,
    /**
     * A syntax has been successfully initialized, and it's being checked for extra constraints such as type or number
     */
    CONSTRAINT_CHECKING,
    /**
     * A syntax has been successfully matched, and it's being initialized
     */
    INITIALIZATION,
    /**
     * Text is being matched against a pattern
     */
    MATCHING,
    /**
     * Nothing has matched the text
     */
    NO_MATCH
}
