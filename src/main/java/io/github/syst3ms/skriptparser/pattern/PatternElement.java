package io.github.syst3ms.skriptparser.pattern;

/**
 * The superclass of all elements of a pattern.
 */
public interface PatternElement {
    /**
     * Attemps to match the {@link PatternElement} to a string at a specified index.
     * @param s the string to match this PatternElement against
     * @param index the index of the string at which this PatternElement should be matched
     * @return the index at which the matching should continue afterwards if successful. Otherwise, {@literal -1}
     */
    int match(String s, int index);
}
