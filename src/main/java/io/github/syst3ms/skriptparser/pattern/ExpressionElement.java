package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.lang.VariableString;
import io.github.syst3ms.skriptparser.parsing.SkriptParser;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
    private boolean nullable, acceptsConditional;

    public ExpressionElement(List<PatternType<?>> types, Acceptance acceptance, boolean nullable, boolean acceptsConditional) {
        this.types = types;
        this.acceptance = acceptance;
        this.nullable = nullable;
        this.acceptsConditional = acceptsConditional;
    }

    @Override
    public int match(String s, int index, SkriptParser parser) {
        if (parser.getOriginalElement().equals(this))
            parser.advanceInPattern();
        PatternType<?>[] typeArray = types.toArray(new PatternType<?>[0]);
        if (index >= s.length()) {
            return -1;
        }
        List<PatternElement> flattened = parser.flatten(parser.getOriginalElement());
        List<PatternElement> possibleInputs = parser.getPossibleInputs(flattened.subList(parser.getPatternIndex(), flattened.size()));
        for (PatternElement possibleInput : possibleInputs) {
            if (possibleInput instanceof TextElement) {
                String text = ((TextElement) possibleInput).getText();
                if (text.isEmpty())
                    continue;
                if (text.equals("\0")) { // End of line
                    if (index == 0)
                        return -1;
                    String toParse = s.substring(index).trim();
                    Expression<?> expression = parse(toParse, typeArray);
                    if (expression != null) {
                        parser.addExpression(expression);
                        return index + toParse.length();
                    }
                    return -1;
                }
                int i = StringUtils.indexOfIgnoreCase(s, text, index);
                while (i != -1) {
                    String toParse = s.substring(index, i).trim();
                    Expression<?> expression = parse(toParse, typeArray);
                    if (expression != null) {
                        parser.addExpression(expression);
                        return index + toParse.length();
                    }
                    i = StringUtils.indexOfIgnoreCase(s, text, i + 1);
                }
            } else if (possibleInput instanceof RegexGroup) {
                Matcher m = ((RegexGroup) possibleInput).getPattern().matcher(s).region(index, s.length());
                while (m.lookingAt()) {
                    int i = m.start();
                    if (i == -1) {
                        continue;
                    }
                    String toParse = s.substring(index, i);
                    if (toParse.length() == parser.getOriginalPattern().length())
                        continue;
                    Expression<?> expression = parse(toParse, typeArray);
                    if (expression != null) {
                        parser.addExpression(expression);
                        return index + toParse.length();
                    }
                }
            } else {
                assert possibleInput instanceof ExpressionElement;
                List<PatternElement> nextPossibleInputs = parser
                    .getPossibleInputs(flattened.subList(parser.getPatternIndex() + 1, flattened.size()));
                if (nextPossibleInputs.stream()
                                      .anyMatch(pe -> !(pe instanceof TextElement))) { // Let's not get that deep
                    continue;
                }
                for (PatternElement nextPossibleInput : nextPossibleInputs) {
                    String text = ((TextElement) nextPossibleInput).getText();
                    if (text.equals("")) {
                        String rest = s.substring(index, s.length());
                        List<String> splits = splitAtSpaces(rest);
                        for (String split : splits) {
                            int i = StringUtils.indexOfIgnoreCase(s, split, index);
                            if (i != -1) {
                                String toParse = s.substring(index, i);
                                Expression<?> expression = parse(toParse, typeArray);
                                if (expression != null) {
                                    parser.addExpression(expression);
                                    return index + toParse.length();
                                }
                            }
                        }
                        return -1;
                    } else {
                        int bound = StringUtils.indexOfIgnoreCase(s, text, index);
                        if (bound == -1) {
                            continue;
                        }
                        String rest = s.substring(index, bound);
                        List<String> splits = splitAtSpaces(rest);
                        for (String split : splits) {
                            int i = StringUtils.indexOfIgnoreCase(s, split, index);
                            if (i != -1) {
                                String toParse = s.substring(index, i);
                                Expression<?> expression = parse(toParse, typeArray);
                                if (expression != null) {
                                    parser.addExpression(expression);
                                    return index + toParse.length();
                                }
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    private List<String> splitAtSpaces(String s) {
        List<String> splitted = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        char[] charArray = s.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == ' ') {
                if (sb.length() > 0) {
                    splitted.add(sb.toString());
                    sb.setLength(0);
                }
            } else if (c == '(') {
                String enclosed = StringUtils.getEnclosedText(s, '(', ')', i);
                if (enclosed == null) {
                    sb.append('(');
                    continue;
                }
                sb.append('(').append(enclosed).append(')');
                i += enclosed.length() + 1;
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            splitted.add(sb.toString());
        }
        return splitted;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T> Expression<? extends T> parse(String s, PatternType<?>[] types) {
        for (PatternType<?> type : types) {
            Expression<? extends T> expression;
            if (type.equals(SyntaxParser.BOOLEAN_PATTERN_TYPE)) {
                // NOTE : conditions call parseBooleanExpression straight away
                expression = (Expression<? extends T>) SyntaxParser.parseBooleanExpression(
                        s,
                        acceptsConditional
                );
            } else {
                expression = SyntaxParser.parseExpression(s, (PatternType<T>) type);
            }
            if (expression == null)
                continue;
            switch (acceptance) {
                case ALL:
                    break;
                case EXPRESSIONS_ONLY:
                    if (expression instanceof SimpleLiteral || expression instanceof VariableString) {
                        // REMIND error
                        return null;
                    }
                    break;
                case LITERALS_ONLY:
                    if (expression instanceof VariableString && !((VariableString) expression).isSimple() || !(expression instanceof SimpleLiteral)) {
                        // REMIND error
                        return null;
                    }
                    break;
                case VARIABLES_ONLY:
                    if (!(expression instanceof Variable)) {
                        // REMIND error
                        return null;
                    }
                    break;
            }
            return expression;
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ExpressionElement)) {
            return false;
        } else {
            ExpressionElement e = (ExpressionElement) obj;
            return types.equals(e.types) && acceptance == e.acceptance && acceptsConditional == e.acceptsConditional;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("%");
        if (nullable)
            sb.append('-');
        switch (acceptance) {
            case ALL:
                break;
            case EXPRESSIONS_ONLY:
                sb.append('~');
                break;
            case LITERALS_ONLY:
                sb.append('*');
                break;
            case VARIABLES_ONLY:
                sb.append('^');
                break;
        }
        if (acceptsConditional)
            sb.append('=');
        sb.append(
            String.join(
                "/",
                types.stream().map(PatternType::toString).toArray(CharSequence[]::new)
            )
        );
        return sb.append("%").toString();
    }

    public enum Acceptance {
        ALL,
        EXPRESSIONS_ONLY,
        LITERALS_ONLY,
        VARIABLES_ONLY
    }
}
