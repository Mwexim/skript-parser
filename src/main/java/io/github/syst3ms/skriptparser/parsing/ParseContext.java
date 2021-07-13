package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.List;
import java.util.Optional;
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
    private final List<String> marks;
    private final SkriptLogger logger;

    public ParseContext(ParserState parserState, PatternElement element, List<MatchResult> matches, List<String> marks, String expressionString, SkriptLogger logger) {
        this.parserState = parserState;
        this.element = element;
        this.expressionString = expressionString;
        this.matches = matches;
        this.marks = marks;
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
     * @return the parse marks
     */
    public List<String> getMarks() {
        return marks;
    }

    /**
     * Returns the single matched mark that was found, empty if no match was found.
     * @return the matched mark
     * @throws UnsupportedOperationException if multiple marks were found
     */
    public Optional<String> getSingleMark() {
        if (marks.size() == 0) {
            return Optional.empty();
        } else if (marks.size() > 1) {
            throw new UnsupportedOperationException("There should be exactly 1 mark, found " + marks.size());
        } else {
            return Optional.of(marks.get(0));
        }
    }

    /**
     * Parses and combines all valid numerical mark into one final result
     * by XOR-ing each match with the previous match.
     * @return the numerical parse mark
     */
    public int getNumericMark() {
        int numeric = 0;
        for (var mark : marks) {
            try {
                if (mark.startsWith("0b")) {
                    numeric ^= Integer.parseInt(mark.substring("0b".length()), 2);
                } else if (mark.startsWith("0x")) {
                    numeric ^= Integer.parseInt(mark.substring("0x".length()), 16);
                } else {
                    numeric ^= Integer.parseInt(mark);
                }
            } catch (NumberFormatException ignored) { /* Nothing */ }
        }
        return numeric;
    }

    /**
     * @return whether the given parse mark was included when matching
     */
    public boolean hasMark(String parseMark) {
        return marks.contains(parseMark);
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
