package io.github.syst3ms.skriptparser.classes;

import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.List;
import java.util.regex.MatchResult;

public class ParseResult {
    private final PatternElement element;
    private final List<MatchResult> matches;
    private final int parseMark;

    public ParseResult(PatternElement element, List<MatchResult> matches, int parseMark) {
        this.element = element;
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
}
