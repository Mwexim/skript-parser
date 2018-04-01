package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.List;
import java.util.regex.MatchResult;

/**
 * An object that stores data about how an object was parsed.
 * By opposition to {@link SkriptParser}, this object is immutable.
 */
public class ParseResult {
    private final PatternElement element;
    private final String expressionString;
    private final List<MatchResult> matches;
    private final int parseMark;

    public ParseResult(PatternElement element, List<MatchResult> matches, int parseMark, String expressionString) {
        this.element = element;
        this.expressionString = expressionString;
        this.matches = matches;
        this.parseMark = parseMark;
    }

    /**
     * @return all the regex that were matched
     */
    public List<MatchResult> getMatches() {
        return matches;
    }

    /**
     * @return the {@link PatternElement} that was successfully matched
     */
    public PatternElement getElement() {
        return element;
    }

    /**
     * @return the parse mark
     */
    public int getParseMark() {
        return parseMark;
    }

    /**
     * @return {@linkplain #getElement() the pattern element} in string form
     */
    public String getExpressionString() {
        return expressionString;
    }
}
