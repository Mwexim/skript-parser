package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.MatchContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A variable/expression, declared in syntax using {@literal %type%}
 * Has :
 * <ul>
 * <li>a {@link List} of {@link PatternType}</li>
 * <li>a field determining what type of values this expression accepts : literals, expressions or both ({@literal %*type%}, {@literal %~type%} and {@literal %type%} respectively)</li>
 * <li>a flag determining whether the expression resorts to default expressions or not, defaulting to {@literal null} instead</li>
 * <li>a flag determining whether the expression accepts condition expressions or not</li>
 * </ul>
 * @see PatternType
 * @see Literal
 * @see ConditionalExpression
 */
public class ExpressionElement implements PatternElement {
    private final List<PatternType<?>> types;
    private final Acceptance acceptance;
    private final boolean nullable;
    private final boolean acceptsConditional;

    public ExpressionElement(List<PatternType<?>> types, Acceptance acceptance, boolean nullable, boolean acceptsConditional) {
        this.types = types;
        this.acceptance = acceptance;
        this.nullable = nullable;
        this.acceptsConditional = acceptsConditional;
    }

    @Override
    public int match(String s, int index, MatchContext context) {
        var typeArray = types.toArray(new PatternType<?>[0]);
        if (index >= s.length()) {
            return -1;
        }
        var logger = context.getLogger();
        logger.recurse();
        var source = context.getSource();
        var possibilityIndex = context.getPatternIndex();
        var flattened = PatternElement.flatten(context.getOriginalElement());
        while (source.isPresent() && possibilityIndex >= flattened.size()) {
            flattened = PatternElement.flatten(source.get().getOriginalElement());
            possibilityIndex = source.get().getPatternIndex();
            source = source.get().getSource();
        }
        // We look at what could possibly be after the expression in the current syntax
        var possibleInputs = PatternElement.getPossibleInputs(flattened.subList(possibilityIndex, flattened.size()));
        for (var possibleInput : possibleInputs) {  // We iterate over those possibilities
            if (possibleInput instanceof TextElement) {
                var text = ((TextElement) possibleInput).getText();
                if (text.isEmpty())
                    continue;
                if (text.equals("\0")) { // End of line
                    if (index == 0) {
                        return -1;
                    }
                    var toParse = s.substring(index).strip();
                    var expression = parse(toParse, typeArray, context.getParserState(), logger);
                    if (expression.isPresent()) {
                        context.addExpression(expression.get());
                        return index + toParse.length();
                    }
                    return -1;
                }
                var i = StringUtils.indexOfIgnoreCase(s, text, index);
                while (i != -1) {
                    var toParse = s.substring(index, i).strip();
                    var expression = parse(toParse, typeArray, context.getParserState(), logger);
                    if (expression.isPresent()) {
                        context.addExpression(expression.get());
                        return index + toParse.length();
                    }
                    i = StringUtils.indexOfIgnoreCase(s, text, i + 1);
                }
            } else if (possibleInput instanceof RegexGroup) {
                var m = ((RegexGroup) possibleInput).getPattern().matcher(s).region(index, s.length());
                while (m.lookingAt()) {
                    var i = m.start();
                    if (i == -1) {
                        continue;
                    }
                    var toParse = s.substring(index, i);
                    if (toParse.length() == context.getOriginalPattern().length())
                        continue;
                    var expression = parse(toParse, typeArray, context.getParserState(), logger);
                    if (expression.isPresent()) {
                        context.addExpression(expression.get());
                        return index + toParse.length();
                    }
                }
            } else {
                assert possibleInput instanceof ExpressionElement;
                var nextPossibleInputs = PatternElement.getPossibleInputs(flattened.subList(context.getPatternIndex() + 1, flattened.size()));
                if (nextPossibleInputs.stream().anyMatch(pe -> !(pe instanceof TextElement))) {
                    continue;
                }
                for (var nextPossibleInput : nextPossibleInputs) {
                    var text = ((TextElement) nextPossibleInput).getText();
                    if (text.equals("")) {
                        var rest = s.substring(index);
                        var splits = splitAtSpaces(rest);
                        for (var split : splits) {
                            var i = StringUtils.indexOfIgnoreCase(s, split, index);
                            if (i != -1) {
                                var toParse = s.substring(index, i);
                                var expression = parse(toParse, typeArray, context.getParserState(), logger);
                                if (expression.isPresent()) {
                                    context.addExpression(expression.get());
                                    return index + toParse.length();
                                }
                            }
                        }
                        return -1;
                    } else {
                        var bound = StringUtils.indexOfIgnoreCase(s, text, index);
                        if (bound == -1) {
                            continue;
                        }
                        var rest = s.substring(index, bound);
                        var splits = splitAtSpaces(rest);
                        for (var split : splits) {
                            var i = StringUtils.indexOfIgnoreCase(s, split, index);
                            if (i != -1) {
                                var toParse = s.substring(index, i);
                                var expression = parse(toParse, typeArray, context.getParserState(), logger);
                                if (expression.isPresent()) {
                                    context.addExpression(expression.get());
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
        List<String> split = new ArrayList<>();
        var sb = new StringBuilder();
        var charArray = s.toCharArray();
        for (var i = 0; i < charArray.length; i++) {
            var c = charArray[i];
            if (c == ' ') {
                if (sb.length() > 0) {
                    split.add(sb.toString());
                    sb.setLength(0);
                }
            } else if (c == '(') {
                var enclosed = StringUtils.getEnclosedText(s, '(', ')', i)
                        .map(en -> "(" + en + ")");
                sb.append(enclosed.orElse("("));
                i += enclosed.map(s1 -> s1.length() + 1).orElse(0);
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            split.add(sb.toString());
        }
        return split;
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<? extends Expression<? extends T>> parse(String s, PatternType<?>[] types, ParserState parserState, SkriptLogger logger) {
        for (var type : types) {
            Optional<? extends Expression<? extends T>> expression;
            logger.recurse();
            if (type.equals(SyntaxParser.BOOLEAN_PATTERN_TYPE)) {
                // NOTE : conditions call parseBooleanExpression straight away
                expression = (Optional<? extends Expression<? extends T>>) SyntaxParser.parseBooleanExpression(
                        s,
                        acceptsConditional ? SyntaxParser.MAYBE_CONDITIONAL : SyntaxParser.NOT_CONDITIONAL,
                        parserState,
                        logger
                );
            } else {
                expression = SyntaxParser.parseExpression(s, (PatternType<T>) type, parserState, logger);
            }
            logger.callback();
            if (expression.isEmpty())
                continue;
            expression = expression.filter(e -> {
                switch (acceptance) {
                    case ALL:
                        break;
                    case EXPRESSIONS_ONLY:
                        if (Literal.isLiteral(e)) {
                            logger.error("Only expressions are allowed, found literal " + s, ErrorType.SEMANTIC_ERROR);
                            return false;
                        }
                        break;
                    case LITERALS_ONLY:
                        if (!Literal.isLiteral(e)) {
                            logger.error("Only literals are allowed, found expression " + s, ErrorType.SEMANTIC_ERROR);
                            return false;
                        }
                        break;
                    case VARIABLES_ONLY:
                        if (!(e instanceof Variable)) {
                            logger.error("Only variables are allowed, found " + s, ErrorType.SEMANTIC_ERROR);
                            return false;
                        }
                        break;
                }
                return true;
            });
            return expression;
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ExpressionElement)) {
            return false;
        } else {
            var e = (ExpressionElement) obj;
            return types.equals(e.types) && acceptance == e.acceptance && acceptsConditional == e.acceptsConditional;
        }
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("%");
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
