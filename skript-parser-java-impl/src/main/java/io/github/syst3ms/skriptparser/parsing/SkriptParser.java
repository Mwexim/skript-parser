package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.pattern.ChoiceElement;
import io.github.syst3ms.skriptparser.pattern.ChoiceGroup;
import io.github.syst3ms.skriptparser.pattern.CompoundElement;
import io.github.syst3ms.skriptparser.pattern.ExpressionElement;
import io.github.syst3ms.skriptparser.pattern.OptionalGroup;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.pattern.RegexGroup;
import io.github.syst3ms.skriptparser.pattern.TextElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;

/**
 * A parser instance used for matching a pattern to a syntax
 */
public class SkriptParser {
    public static PatternElement WHETHER_PATTERN;
    private String originalPattern;
    private PatternElement originalElement;
    private int patternIndex = 0;
    private List<Expression<?>> parsedExpressions = new ArrayList<>();
    private List<MatchResult> regexMatches = new ArrayList<>();
    private int parseMark = 0;
    private String lastMatched = "";

    public SkriptParser(PatternElement e) {
        this.originalPattern = e.toString();
        this.originalElement = e;
    }

    public String getOriginalPattern() {
        return originalPattern;
    }

    public PatternElement getOriginalElement() {
        return originalElement;
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

    public List<MatchResult> getRegexMatches() {
        return regexMatches;
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

    public ParseResult toParseResult() {
        return new ParseResult(originalElement, regexMatches, parseMark);
    }

    public List<PatternElement> flatten(PatternElement element) {
        if (element instanceof CompoundElement) {
            return ((CompoundElement) element).getElements();
        } else {
            return Collections.singletonList(element);
        }
    }

    public List<PatternElement> getPossibleInputs(List<PatternElement> elements) {
        List<PatternElement> possibilities = new ArrayList<>();
        for (PatternElement element : elements) {
            if (element instanceof TextElement || element instanceof RegexGroup) {
                if (element instanceof TextElement) {
                    String text = ((TextElement) element).getText();
                    if (text.isEmpty() || text.matches("\\s*") && elements.size() == 1) {
                        return possibilities;
                    } else if (text.matches("\\s*")) {
                        continue;
                    }
                }
                possibilities.add(element);
                return possibilities;
            } else if (element instanceof ChoiceGroup) {
                for (ChoiceElement choice : ((ChoiceGroup) element).getChoices()) {
                    List<PatternElement> possibleInputs = getPossibleInputs(flatten(choice.getElement()));
                    possibilities.addAll(possibleInputs);
                }
                return possibilities;
            } else if (element instanceof ExpressionElement) {
                possibilities.add(element);
                return possibilities;
            } else if (element instanceof OptionalGroup) {
                possibilities.addAll(getPossibleInputs(flatten(((OptionalGroup) element).getElement())));
            }
        }
        possibilities.add(new TextElement("\0"));
        return possibilities;
    }

    public static void setWhetherPattern(PatternElement element) {
        if (WHETHER_PATTERN == null) { // We don't want people changing this that easily
            WHETHER_PATTERN = element;
        }
    }

    public String getLastMatched() {
        return lastMatched;
    }

    public void setLastMatched(String lastMatched) {
        this.lastMatched = lastMatched;
    }
}
