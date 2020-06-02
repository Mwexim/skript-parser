package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;

/**
 * A parser instance used for matching a pattern to a syntax
 */
public class MatchContext {
    private String originalPattern;
    private PatternElement originalElement;
    // Provided to the syntax's class
    private final ParserState parserState;
    private final SkriptLogger logger;
    private final MatchContext source;
    private List<Expression<?>> parsedExpressions = new ArrayList<>();
    private List<MatchResult> regexMatches = new ArrayList<>();
    private int patternIndex = 0;
    private int parseMark = 0;

    public MatchContext(PatternElement e, ParserState parserState, SkriptLogger logger) {
        this(e, parserState, logger, null);
    }

    public MatchContext(PatternElement e, ParserState parserState, SkriptLogger logger, @Nullable MatchContext source) {
        this.originalPattern = e.toString();
        this.originalElement = e;
        this.parserState = parserState;
        this.logger = logger;
        this.source = source;
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

    @Nullable
    public MatchContext getSource() {
        return source;
    }

    public MatchContext branch(PatternElement e) {
        return new MatchContext(e, parserState, logger, this);
    }

    public void merge(MatchContext branch) {
        parsedExpressions.addAll(branch.parsedExpressions);
        regexMatches.addAll(branch.regexMatches);
        addMark(branch.parseMark);
    }

    /**
     * Turns this {@link MatchContext} into a {@link ParseContext} used in {@linkplain io.github.syst3ms.skriptparser.lang.SyntaxElement}s
     * @return a {@link ParseContext} based on this {@link MatchContext}
     */
    public ParseContext toParseResult() {
        return new ParseContext(parserState, originalElement, regexMatches, parseMark, originalPattern, logger);
    }

    public ParserState getParserState() {
        return parserState;
    }

    public SkriptLogger getLogger() {
        return logger;
    }
}
