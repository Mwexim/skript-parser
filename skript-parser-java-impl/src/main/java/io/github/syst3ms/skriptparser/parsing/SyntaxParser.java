package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.ExpressionList;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.LiteralList;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.lang.VariableString;
import io.github.syst3ms.skriptparser.lang.interfaces.ConditionalExpression;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class SyntaxParser {
    public static final Pattern LIST_SPLIT_PATTERN = Pattern.compile("\\s*(,)\\s*|\\s+(and|or)\\s+", Pattern.CASE_INSENSITIVE);
    public static final PatternType<Boolean> BOOLEAN_PATTERN_TYPE = new PatternType<>((Type<Boolean>) TypeManager.getByClass(Boolean.class), true);
    public static final PatternType<Object> OBJECT_PATTERN_TYPE = new PatternType<>((Type<Object>) TypeManager.getByClass(Object.class), true);
    private static final LinkedList<SyntaxInfo<? extends Effect>> recentEffects = new LinkedList<>();
    private static final LinkedList<SyntaxInfo<? extends CodeSection>> recentSections = new LinkedList<>();
    private static final LinkedList<ExpressionInfo<?, ?>> recentExpressions = new LinkedList<>();

    public static <T> Expression<? extends T> parseExpression(String s, PatternType<T> expectedType) {
        if (s.isEmpty())
            return null;
        Expression<? extends T> literal = parseLiteral(s, expectedType);
        if (literal != null)
            return literal;
        if (expectedType.equals(BOOLEAN_PATTERN_TYPE)) {
            throw new IllegalStateException("Parsing of boolean expressions should be delegated to parseBooleanExpression");
        }
        for (ExpressionInfo<?, ?> info : recentExpressions) {
            Expression<? extends T> expression = matchExpressionInfo(s, info, expectedType);
            if (expression != null) {
                recentExpressions.removeFirstOccurrence(info);
                recentExpressions.addFirst(info);
                return expression;
            }
        }
        // Let's not loop over the same elements again
        Collection<ExpressionInfo<?, ?>> remainingExpressions = SyntaxManager.getAllExpressions();
        remainingExpressions.removeAll(recentExpressions);
        for (ExpressionInfo<?, ?> info : remainingExpressions) {
            Expression<? extends T> expression = matchExpressionInfo(s, info, expectedType);
            if (expression != null) {
                recentExpressions.removeFirstOccurrence(info);
                recentExpressions.addFirst(info);
                return expression;
            }
        }
        if (!expectedType.isSingle()) {
            Expression<? extends T> list = parseListLiteral(s, expectedType);
            if (list != null)
                return list;
        }
        return null;
    }

    public static <T> Expression<? extends T> parseListLiteral(String s, PatternType<T> expectedType) {
        assert !expectedType.isSingle();
        List<String> parts = new ArrayList<>();
        Matcher m = LIST_SPLIT_PATTERN.matcher(s);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i = StringUtils.nextSimpleCharacterIndex(s, i + 1)) {
            if (i == -1)
                break;
            char c = s.charAt(i);
            if (c == ' ' || c == ',') {
                m.region(i, s.length());
                if (m.lookingAt()) {
                    if (sb.length() == 0)
                        return null;
                    parts.add(sb.toString());
                    parts.add(m.group());
                    sb.setLength(0);
                    i = m.end() - 1;
                }
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0)
            parts.add(sb.toString());
        if (parts.size() == 1)
            return null;
        Boolean isAndList = null; // Hello nullable booleans, it had been a pleasure NOT using you
        for (int i = 0; i < parts.size(); i++) {
            if ((i & 1) == 1) { // Odd index == separator
                String separator = parts.get(i).trim();
                if (separator.equalsIgnoreCase("and") || separator.equalsIgnoreCase("nor")) {
                    isAndList = true;
                } else if (separator.equalsIgnoreCase("or")) {
                    isAndList = isAndList != null && isAndList;
                }
            }
        }
        isAndList = isAndList == null || isAndList; // Defaults to true
        List<Expression<? extends T>> expressions = new ArrayList<>();
        boolean isLiteralList = true;
        for (int i = 0; i < parts.size(); i++) {
            if ((i & 1) == 0) { // Even index == element
                String part = parts.get(i);
                Expression<? extends T> expression = parseExpression(part, expectedType);
                if (expression == null)
                    return null;
                isLiteralList &= expression instanceof SimpleLiteral;
                expressions.add(expression);
            }
        }
        if (expressions.size() == 1)
            return expressions.get(0);
        if (isLiteralList) {
            return new LiteralList<>(
                expressions.toArray(new Literal[expressions.size()]),
                expectedType.getType().getTypeClass(),
                isAndList
            );
        } else {
            return new ExpressionList<>(
                expressions.toArray(new Expression[expressions.size()]),
                expectedType.getType().getTypeClass(),
                isAndList
            );
        }
    }

    public static <T> Expression<? extends T> parseLiteral(String s, PatternType<T> expectedType) {
        Map<Class<?>, Type<?>> classToTypeMap = TypeManager.getClassToTypeMap();
        for (Class<?> c : classToTypeMap.keySet()) {
            Class<T> expectedClass = expectedType.getType().getTypeClass();
            if (expectedClass.isAssignableFrom(c) || Converters.converterExists(c, expectedClass)) {
                Function<String, ?> literalParser = classToTypeMap.get(c).getLiteralParser();
                if (literalParser != null) {
                    T literal = (T) literalParser.apply(s);
                    if (literal != null && expectedClass.isAssignableFrom(c)) {
                        return new SimpleLiteral<>(expectedClass, literal);
                    } else if (literal != null) {
                        return new SimpleLiteral<>((Class<T>) c, literal).convertExpression(new Class[]{expectedType.getType().getTypeClass()});
                    }
                } else if (expectedClass == String.class || c == String.class) {
                    VariableString vs = VariableString.newInstanceWithQuotes(s);
                    if (vs != null && expectedClass.isAssignableFrom(c)) {
                        return (Expression<? extends T>) vs;
                    }
                }
            }
        }
        return null;
    }

    public static Effect parseEffect(String s) {
        for (SyntaxInfo<? extends Effect> info : recentEffects) {
            Effect effect = matchEffectInfo(s, info);
            if (effect != null) {
                recentEffects.removeFirstOccurrence(info);
                recentEffects.addFirst(info);
                return effect;
            }
        }
        // Let's not loop over the same elements again
        Collection<SyntaxInfo<? extends Effect>> remainingEffects = SyntaxManager.getEffects();
        remainingEffects.removeAll(recentEffects);
        for (SyntaxInfo<? extends Effect> info : remainingEffects) {
            Effect effect = matchEffectInfo(s, info);
            if (effect != null) {
                recentEffects.removeFirstOccurrence(info);
                recentEffects.addFirst(info);
                return effect;
            }
        }
        return null;
    }

    /**
	 * Tries to match an {@link ExpressionInfo} against the given {@link String} expression.
	 * Made for DRY purposes inside of {@link #parseExpression(String, PatternType)}
	 * @param <T> The return type of the {@link Expression}
	 * @return the Expression instance of matching, or {@literal null} otherwise
	 */
    private static <T> Expression<? extends T> matchExpressionInfo(String s, ExpressionInfo<?, ?> info, PatternType<T> expectedType) {
        List<PatternElement> patterns = info.getPatterns();
        PatternType<?> infoType = info.getReturnType();
        Class<?> infoTypeClass = infoType.getType().getTypeClass();
        Class<T> expectedTypeClass = expectedType.getType().getTypeClass();
        if (!expectedTypeClass.isAssignableFrom(infoTypeClass) && !Converters.converterExists(infoTypeClass, expectedTypeClass))
            return null;
        for (int i = 0; i < patterns.size(); i++) {
            PatternElement element = patterns.get(i);
            SkriptParser parser = new SkriptParser(element);
            if (element.match(s, 0, parser) != -1) {
                try {
                    Expression<? extends T> expression = (Expression<? extends T>) info.getSyntaxClass().newInstance();
                    expression.init(
                        parser.getParsedExpressions().toArray(new Expression[0]),
                        i,
                        parser.toParseResult()
                    );
                    Class<?> expressionReturnType = expression.getReturnType();
                    if (!expectedTypeClass.isAssignableFrom(expressionReturnType)) {
                        Expression<?> converted = expression.convertExpression(
                                (Class<?>) expectedTypeClass);
                        if (converted != null) {
                            return (Expression<? extends T>) converted;
                        } else {
                            error("Unmatching return types : expected " +
                                  infoType +
                                  " or a subclass, but found " +
                                  TypeManager.getByClass(expressionReturnType).getPluralForms()[expression.isSingle() ? 0 : 1]);
                            return null;
                        }
                    }
                    if (!expression.isSingle() &&
                        expectedType.isSingle()) {
                        error("Expected a single value, but multiple were given");
                        continue;
                    }
                    return expression;
                } catch (InstantiationException | IllegalAccessException e) {
                    error("Parsing of " + info.getSyntaxClass()
											  .getSimpleName() + " succeeded, but it couldn't be instantiated");
                }
            }
        }
        error("Can't understand the expression : '" + s + "'");
        return null;
    }

    private static Effect matchEffectInfo(String s, SyntaxInfo<? extends Effect> info) {
        List<PatternElement> patterns = info.getPatterns();
        for (int i = 0; i < patterns.size(); i++) {
            PatternElement element = patterns.get(i);
            SkriptParser parser = new SkriptParser(element);
            if (element.match(s, 0, parser) != -1) {
                try {
                    Effect effect = info.getSyntaxClass().newInstance();
                    effect.init(
                            parser.getParsedExpressions().toArray(new Expression[0]),
                            i,
                            parser.toParseResult()
                    );
                    return effect;
                } catch (InstantiationException | IllegalAccessException e) {
                    error("Parsing of " + info.getSyntaxClass()
                                              .getSimpleName() + " succeeded, but it couldn't be instantiated");
                }
            }
        }
        error("Can't understand the effect : '" + s + "'");
        return null;
    }

    public static Expression<Boolean> parseBooleanExpression(String s, boolean shouldNotBeConditional) {
        // I swear this is the cleanest way to do it
        if (s.equalsIgnoreCase("true")) {
            return new SimpleLiteral<>(Boolean.class, true);
        } else if (s.equalsIgnoreCase("false")) {
            return new SimpleLiteral<>(Boolean.class, false);
        }
        for (ExpressionInfo<?, ?> info : recentExpressions) {
            if (info.getReturnType().getType().getTypeClass() != Boolean.class)
                continue;
            Expression<Boolean> expression = (Expression<Boolean>) matchExpressionInfo(s, info, BOOLEAN_PATTERN_TYPE);
            if (expression != null) {
                if (shouldNotBeConditional && ConditionalExpression.class.isAssignableFrom(expression.getClass())) {
                    error("This expression can't be used outside of conditions and 'whether %boolean%'");
                    return null;
                }
                recentExpressions.removeFirstOccurrence(info);
                recentExpressions.addFirst(info);
                return expression;
            }
        }
        // Let's not loop over the same elements again
        Collection<ExpressionInfo<?, ?>> remainingExpressions = SyntaxManager.getAllExpressions();
        remainingExpressions.removeAll(recentExpressions);
        for (ExpressionInfo<?, ?> info : remainingExpressions) {
            if (info.getReturnType().getType().getTypeClass() != Boolean.class)
                continue;
            Expression<Boolean> expression = (Expression<Boolean>) matchExpressionInfo(s, info, BOOLEAN_PATTERN_TYPE);
            if (expression != null) {
                if (ConditionalExpression.class.isAssignableFrom(expression.getClass()) && shouldNotBeConditional) {
                    error("This expression can't be used outside of conditions and 'whether %boolean%'");
                    return null;
                }
                recentExpressions.removeFirstOccurrence(info);
                recentExpressions.addFirst(info);
                return expression;
            }
        }
        return null;
    }

    private static void error(String s) {
        // TODO
    }
}
