package io.github.bensku.skript.pattern;

import io.github.bensku.skript.ParserState;

/**
 * Represents an element in parseable pattern.
 *
 */
public interface PatternElement {
    
    /**
     * Checks if this pattern element matches a part in given string beginning
     * from given start position. If a match if found, end of it is returned.
     * If no match is found, -1 will be returned.
     * @param str String to evaluate.
     * @param start Start position in string.
     * @return End position of match or -1 if string after start doesn't match.
     */
    int matches(String str, int start);
    
    /**
     * Checks if this pattern element matches a part in given string beginning
     * from given start position. If a match if found, end of it is returned.
     * If no match is found, -1 will be returned.
     * @param str String to evaluate.
     * @param start Start position in string.
     * @param state Parser state which should be notified if there are any
     * parts which require actions to be done runtime.
     * @return End position of match or -1 if string after start doesn't match.
     */
    default int matches(String str, int start, ParserState state) {
        return matches(str, start);
    }
}
