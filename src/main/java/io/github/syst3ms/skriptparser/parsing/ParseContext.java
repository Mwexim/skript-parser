package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.List;
import java.util.regex.MatchResult;

/**
 * An object that stores data about how an object was parsed.
 * By opposition to {@link MatchContext}, this object is immutable, and generated after matching is complete.
 * @see Expression#init(Expression[], int, ParseContext)
 */
public class ParseContext {
    private final ParserState parserState;
    private final PatternElement element;
    private final String expressionString;
    private final List<MatchResult> matches;
    private final int parseMark;
    private final SkriptLogger logger;

    public ParseContext(ParserState parserState, PatternElement element, List<MatchResult> matches, int parseMark, String expressionString, SkriptLogger logger) {
        this.parserState = parserState;
        this.element = element;
        this.expressionString = expressionString;
        this.matches = matches;
        this.parseMark = parseMark;
        this.logger = logger;
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

    public ParserState getParserState() {
        return parserState;
    }

    public SkriptLogger getLogger() {
        return logger;
    }
}
