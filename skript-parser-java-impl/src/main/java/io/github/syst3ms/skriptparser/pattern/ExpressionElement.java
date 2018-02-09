package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.SkriptParser;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.registration.PatternType;
import io.github.syst3ms.skriptparser.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;

/**
 * A variable/expression, declared in syntax using {@literal %type%}
 * Has :
 * <ul>
 * <li>a {@link List} of {@link PatternType}</li>
 * <li>a field determining what type of values this expression accepts : literals, expressions or both ({@literal %*type%}, {@literal %~type%} and {@literal %type%} respectively)</li>
 * <li>whether the expression resorts to default expressions or not, defaulting to {@literal null} instead</li>
 * </ul>
 */
public class ExpressionElement implements PatternElement {
    private List<PatternType<?>> types;
    private Acceptance acceptance;
    private boolean nullable;

    public ExpressionElement(List<PatternType<?>> types, Acceptance acceptance, boolean nullable) {
        this.types = types;
        this.acceptance = acceptance;
        this.nullable = nullable;
    }

    @Override
    public int match(String s, int index, SkriptParser parser) {
        if (parser.getOriginalElement().equals(this))
            parser.advanceInPattern();
        if (s.charAt(index) == '(') {
            String enclosed = StringUtils.getEnclosedText(s, '(', ')', index);
            /*
             * We don't want to return here, a single bracket could come from a syntax (albeit a stupid one)
             * We also want to continue the code in any case
             */
            if (enclosed != null) {
                for (PatternType<?> type : types) {
                    Expression<?> expression = parse(s, type, parser.getOriginalElement());
                    if (expression != null) {
                        parser.addExpression(expression);
                        return index + s.length();
                    }
                }
            }
        }
        List<PatternElement> flattened = parser.flatten(parser.getOriginalElement());
        List<PatternElement> possibleInputs = parser.getPossibleInputs(flattened.subList(parser.getPatternIndex(), flattened.size()));
        inputLoop: for (PatternElement possibleInput : possibleInputs) {
            if (possibleInput instanceof TextElement) {
                String text = ((TextElement) possibleInput).getText();
                if (text.equals("")) { // End of line
                    String toParse = s.substring(index);
                    for (PatternType<?> type : types) {
                        Expression<?> expression = parse(toParse, type, parser.getOriginalElement());
                        if (expression != null) {
                            parser.addExpression(expression);
                            return index + toParse.length();
                        }
                    }
                    return -1;
                }
                int i = s.indexOf(text, index);
                if (i == -1)
                    continue;
                String toParse = s.substring(index, i).trim();
                for (PatternType<?> type : types) {
                    Expression<?> expression = parse(toParse, type, parser.getOriginalElement());
                    if (expression != null) {
                        parser.addExpression(expression);
                        return index + toParse.length();
                    }
                    continue inputLoop;
                }
            } else {
                assert possibleInput instanceof RegexGroup;
                Matcher m = ((RegexGroup) possibleInput).getPattern().matcher(s).region(index, s.length());
                while (m.lookingAt()) {
                    int i = m.start();
                    if (i == -1)
                        continue;
                    String toParse = s.substring(index, i);
                    for (PatternType<?> type : types) {
                        Expression<?> expression = parse(toParse, type, parser.getOriginalElement());
                        if (expression != null) {
                            parser.addExpression(expression);
                            return index + toParse.length();
                        }
                        continue inputLoop;
                    }
                }
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    private <T> Expression<? extends T> parse(String s, PatternType<T> type, PatternElement originalPattern) {
        if (type.equals(SyntaxParser.BOOLEAN_PATTERN_TYPE)) {
            // REMINDER : conditions call parseBooleanExpression straight away
            return (Expression<? extends T>) SyntaxParser.parseBooleanExpression(s, originalPattern.equals(SkriptParser.WHETHER_PATTERN));
        } else {
            return SyntaxParser.parseExpression(s, type);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ExpressionElement)) {
            return false;
        } else {
            ExpressionElement e = (ExpressionElement) obj;
            return types.equals(e.types) && acceptance == e.acceptance;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("%");
        if (nullable)
            sb.append('-');
        if (acceptance == Acceptance.EXPRESSIONS_ONLY) {
            sb.append('~');
        } else if (acceptance == Acceptance.LITERALS_ONLY) {
            sb.append('*');
        }
        sb.append(
            String.join(
                "/",
                types.stream().map(PatternType::toString).toArray(CharSequence[]::new)
            )
        );
        return sb.append("%").toString();
    }

    public enum Acceptance {
        BOTH,
        EXPRESSIONS_ONLY,
        LITERALS_ONLY
    }
}
