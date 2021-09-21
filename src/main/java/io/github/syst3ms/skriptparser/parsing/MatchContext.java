package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.pattern.ChoiceGroup;
import io.github.syst3ms.skriptparser.pattern.CompoundElement;
import io.github.syst3ms.skriptparser.pattern.OptionalGroup;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;

/**
 * An object that provides contextual information during syntax matching.
 */
public class MatchContext {
    private final String originalPattern;
    private final PatternElement originalElement;
    // Provided to the syntax's class
    private final ParserState parserState;
    private final SkriptLogger logger;
    @Nullable
    private final MatchContext source;
    private final List<Expression<?>> parsedExpressions = new ArrayList<>();
    private final List<MatchResult> regexMatches = new ArrayList<>();
    private int patternIndex = 0;
    private final List<String> marks = new ArrayList<>();

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

    /**
     * @return the {@link PatternElement} that is currently being matched
     */
    public PatternElement getOriginalElement() {
        return originalElement;
    }

    /**
     * @return the string version of {@link #getOriginalElement()}
     */
    public String getOriginalPattern() {
        return originalPattern;
    }

    /**
     * @return an index indicating where the matching is currently at inside of the original {@link PatternElement}
     * (almost always when it is a {@link CompoundElement}).
     */
    public int getPatternIndex() {
        return patternIndex;
    }

    /**
     * Indicates that the next element inside the original pattern element (which must be a {@link CompoundElement}) is
     * now being matched.
     */
    public void advanceInPattern() {
        patternIndex++;
    }

    /**
     * @return a list of all successfully parsed expressions so far.
     */
    public List<Expression<?>> getParsedExpressions() {
        return parsedExpressions;
    }

    /**
     * Adds a new successfully parsed expression to the list.
     * @param expression a parsed expression
     */
    public void addExpression(Expression<?> expression) {
        parsedExpressions.add(expression);
    }

    /**
     * Adds a new successful regex match to the list.
     * @param match a regex match
     */
    public void addRegexMatch(MatchResult match) {
        regexMatches.add(match);
    }

    /**
     * @return the parse marks so far
     */
    public List<String> getMarks() {
        return marks;
    }

    /**
     * Adds the just matched parse marks to the list of all parse marks matched so far
     * @param mark the just matched parse mark
     */
    public void addMark(String mark) {
        marks.add(mark);
    }

    /**
     * When the original element must be updated (inside of an {@link OptionalGroup} or a {@link ChoiceGroup}), the source
     * tracks what the original MatchContext was. This is non-null only after {@link #branch(PatternElement)} is called.
     * @return the source of this MatchContext
     */
    public Optional<MatchContext> getSource() {
        return Optional.ofNullable(source);
    }

    /**
     * Creates a new MatchContext based on the given {@link PatternElement}
     * @param e the new original pattern element
     * @return the branched MatchContext
     */
    public MatchContext branch(PatternElement e) {
        return new MatchContext(e, parserState, logger, this);
    }

    /**
     * Merges a branched MatchContext back into the current one
     * @param branch the branched MatchContext
     */
    public void merge(MatchContext branch) {
        parsedExpressions.addAll(branch.parsedExpressions);
        regexMatches.addAll(branch.regexMatches);
        marks.addAll(branch.marks);
    }

    /**
     * Turns this {@link MatchContext} into a {@link ParseContext} used in {@linkplain SyntaxElement}s
     * @return a {@link ParseContext} based on this {@link MatchContext}
     */
    public ParseContext toParseResult() {
        return new ParseContext(parserState, originalElement, regexMatches, marks, originalPattern, logger);
    }

    public ParserState getParserState() {
        return parserState;
    }

    public SkriptLogger getLogger() {
        return logger;
    }
}
