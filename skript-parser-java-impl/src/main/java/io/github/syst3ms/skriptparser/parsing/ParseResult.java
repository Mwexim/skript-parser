package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.List;
import java.util.regex.MatchResult;

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

    public List<MatchResult> getMatches() {
        return matches;
    }

    public PatternElement getElement() {
        return element;
    }

    public int getParseMark() {
        return parseMark;
    }

    public String getExpressionString() {
        return expressionString;
    }
}
