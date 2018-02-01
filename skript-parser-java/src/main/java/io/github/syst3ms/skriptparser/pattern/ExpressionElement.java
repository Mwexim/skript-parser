package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.classes.Expression;
import io.github.syst3ms.skriptparser.classes.PatternType;
import io.github.syst3ms.skriptparser.classes.SkriptParser;
import io.github.syst3ms.skriptparser.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;

/**
 * A variable/expression, declared in syntax using {@literal %type%}
 * Has :
 * <ul>
 * <li>a {@link List} of {@link PatternType}</li>
 * <li>a field determining what type of values this expression accepts : literals, expressions or both ({@literal %*type%}, {@literal %~type%} and {@literal %type%} respectively)</li>
 * </ul>
 */
public class ExpressionElement implements PatternElement {
    private List<PatternType<?>> types;
    private Acceptance acceptance;

    @Override
    public int match(String s, int index, SkriptParser parser) {
        if (parser.getElement().equals(this))
            parser.advanceInPattern();
        if (s.charAt(index) == '(') {
            String enclosed = StringUtils.getEnclosedText(s, '(', ')', index);
            /*
             * We don't want to return here, a single bracket could come from a syntax (albeit a stupid one)
             * We also want to continue the code in any case
             */
            if (enclosed != null) {
                Expression<?> expression = parser.parseExpression(s);
                if (expression != null) {
                    parser.addExpression(expression);
                    return index + s.length();
                }
            }
        }

        List<PatternElement> flattened = parser.flatten(parser.getElement());
        List<PatternElement> possibleInputs = parser.getPossibleInputs(flattened.subList(parser.getPatternIndex(), flattened.size()));
        for (PatternElement possibleInput : possibleInputs) {
            if (possibleInput instanceof TextElement) {
                String text = ((TextElement) possibleInput).getText();
                if (text.equals("")) { // End of line
                    String toParse = s.substring(index);
                    Expression<?> expression = parser.parseExpression(toParse);
                    if (expression == null) {
                        return -1;
                    }
                    parser.addExpression(expression);
                    return index + toParse.length();
                }
                int i = s.indexOf(text, index);
                if (i == -1)
                    continue;
                String toParse = s.substring(index, i).trim();
                Expression<?> expression = parser.parseExpression(toParse);
                if (expression == null) {
                    continue;
                }
                parser.addExpression(expression);
                return index + toParse.length();
            } else {
                assert possibleInput instanceof RegexGroup;
                Matcher m = ((RegexGroup) possibleInput).getPattern().matcher(s).region(index, s.length());
                while (m.lookingAt()) {
                    int i = m.start();
                    if (i == -1)
                        continue;
                    String toParse = s.substring(index, i);
                    Expression<?> expression = parser.parseExpression(toParse);
                    if (expression == null) {
                        continue;
                    }
                    parser.addExpression(expression);
                    return index + toParse.length();
                }
            }
        }
        return -1;
    }

    public enum Acceptance {
        BOTH,
        EXPRESSIONS_ONLY,
        LITERALS_ONLY
    }

    public ExpressionElement(List<PatternType<?>> types, Acceptance acceptance) {
        this.types = types;
        this.acceptance = acceptance;
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
        StringBuilder sb = new StringBuilder();
        if (acceptance == Acceptance.EXPRESSIONS_ONLY) {
            sb.append('~');
        } else if (acceptance == Acceptance.LITERALS_ONLY) {
            sb.append('*');
        }
        for (int i = 0; i < types.size(); i++) {
            if (i > 0) {
                sb.append('/');
            }
            sb.append(types.get(i));
        }
        return sb.toString();
    }
}
