package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.parsing.SkriptParser;

/**
 * The superclass of all elements of a pattern.
 */
public interface PatternElement {
    /**
     * Attempts to match the {@link PatternElement} to a string at a specified index.
     * About the index, make sure to never increment the index by some arbitrary value when returning
     *
     * @param s the string to match this PatternElement against
     * @param index the index of the string at which this PatternElement should be matched
     * @return the index at which the matching should continue afterwards if successful. Otherwise, {@literal -1}
     */
    int match(String s, int index, SkriptParser parser);
}
