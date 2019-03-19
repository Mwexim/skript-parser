package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;

/**
 * A parser instance used for matching a pattern to a syntax
 */
public class MatchContext {
    private String originalPattern;
    public PatternElement originalElement;
    // Provided to the syntax's class
    private final Class<? extends TriggerContext>[] currentContext;
    private List<Expression<?>> parsedExpressions = new ArrayList<>();
    private List<MatchResult> regexMatches = new ArrayList<>();
    private int patternIndex = 0;
    private int parseMark = 0;

    public MatchContext(PatternElement e, Class<? extends TriggerContext>[] currentContext) {
        this.originalPattern = e.toString();
        this.originalElement = e;
        this.currentContext = currentContext;
    }

    public PatternElement getOriginalElement() {
        return originalElement;
    }

    public String getOriginalPattern() {
        return originalPattern;
    }

    public int getPatternIndex() {
        return patternIndex;
    }

    public void advanceInPattern() {
        patternIndex++;
    }
    public List<Expression<?>> getParsedExpressions() {
        return parsedExpressions;
    }

    public void addExpression(Expression<?> expression) {
        parsedExpressions.add(expression);
    }

    public void addRegexMatch(MatchResult match) {
        regexMatches.add(match);
    }

    public int getParseMark() {
        return parseMark;
    }

    public void addMark(int mark) {
        parseMark ^= mark;
    }

    /**
     * Turns this {@link MatchContext} into a {@link ParseContext} used in {@linkplain io.github.syst3ms.skriptparser.lang.SyntaxElement}s
     * @return a {@link ParseContext} based on this {@link MatchContext}
     */
    public ParseContext toParseResult() {
        return new ParseContext(currentContext, originalElement, regexMatches, parseMark, originalPattern);
    }

}
