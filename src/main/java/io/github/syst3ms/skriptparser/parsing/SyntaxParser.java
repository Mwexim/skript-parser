package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.ExpressionList;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.LiteralList;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.lang.VariableString;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.RecentElementList;
import io.github.syst3ms.skriptparser.util.StringUtils;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class SyntaxParser {
    public static final Pattern LIST_SPLIT_PATTERN = Pattern.compile("\\s*(,)\\s*|\\s+(and|or)\\s+", Pattern.CASE_INSENSITIVE);
    @SuppressWarnings("ConstantConditions")
    public static final PatternType<Boolean> BOOLEAN_PATTERN_TYPE = new PatternType<>((Type<Boolean>) TypeManager.getByClass(Boolean.class), true);
    @SuppressWarnings("ConstantConditions")
    public static final PatternType<Object> OBJECT_PATTERN_TYPE = new PatternType<>((Type<Object>) TypeManager.getByClass(Object.class), true);

    private static final RecentElementList<SyntaxInfo<? extends Effect>> recentEffects = new RecentElementList<>();
    private static final RecentElementList<SyntaxInfo<? extends CodeSection>> recentSections = new RecentElementList<>();
    private static final RecentElementList<ExpressionInfo<?, ?>> recentExpressions = new RecentElementList<>();

    @Nullable
    public static <T> Expression<? extends T> parseExpression(String s, PatternType<T> expectedType) {
        if (s.isEmpty())
            return null;
        if (s.startsWith("(") && s.endsWith(")") && StringUtils.findClosingIndex(s, '(', ')', 0) == s.length() - 1) {
            s = s.substring(1, s.length() - 1);
        }
        Expression<? extends T> literal = parseLiteral(s, expectedType);
        if (literal != null) {
            return literal;
        }
        Variable<? extends T> variable = (Variable<? extends T>) Variables.parseVariable(s, expectedType.getType().getTypeClass());
        if (variable != null) {
            if (!variable.isSingle() && expectedType.isSingle()) {
                // REMIND error
                return null;
            }
            return variable;
        }
        if (!expectedType.isSingle()) {
            Expression<? extends T> listLiteral = parseListLiteral(s, expectedType);
            if (listLiteral != null) {
                return listLiteral;
            }
        }
        for (ExpressionInfo<?, ?> info : recentExpressions) {
            Expression<? extends T> expr = matchExpressionInfo(s, info, expectedType);
            if (expr != null) {
                recentExpressions.moveToFirst(info);
                return expr;
            }
        }
        // Let's not loop over the same elements again
        List<ExpressionInfo<?, ?>> remainingExpressions = SyntaxManager.getAllExpressions();
        recentExpressions.removeFrom(remainingExpressions);
        for (ExpressionInfo<?, ?> info : remainingExpressions) {
            Expression<? extends T> expr = matchExpressionInfo(s, info, expectedType);
            if (expr != null) {
                recentExpressions.moveToFirst(info);
                return expr;
            }
        }
        // REMIND error
        return null;
    }

    @Nullable
    public static <T> Expression<? extends T> parseListLiteral(String s, PatternType<T> expectedType) {
        assert !expectedType.isSingle();
        if (!s.contains(",") && !s.contains("and") && !s.contains("nor") && !s.contains("or"))
            return null;
        List<String> parts = new ArrayList<>();
        Matcher m = LIST_SPLIT_PATTERN.matcher(s);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i = StringUtils.nextSimpleCharacterIndex(s, i + 1)) {
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
                    continue;
                }
            } else if (c == '(') {
                String closing = StringUtils.getEnclosedText(s, '(', ')', i);
                if (closing != null) {
                    int endIndex = i + closing.length() + 1;
                    sb.append("(").append(s.substring(i + 1, endIndex)).append(")");
                    i = endIndex;
                    continue;
                }
            }
            sb.append(c);
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
                if (expression == null) {
                    return null;
                }
                isLiteralList &= expression instanceof Literal;
                expressions.add(expression);
            }
        }
        if (expressions.size() == 1)
            return expressions.get(0);
        if (isLiteralList) {
            //noinspection SuspiciousToArrayCall
            Literal[] literals = expressions.toArray(new Literal[expressions.size()]);
            Class<?> returnType = ClassUtils.getCommonSuperclass(Arrays.stream(literals).map(Literal::getReturnType).toArray(Class[]::new));
            return new LiteralList<>(
                literals,
                returnType,
                isAndList
            );
        } else {
            Expression[] exprs = expressions.toArray(new Expression[expressions.size()]);
            Class<?> returnType = ClassUtils.getCommonSuperclass(Arrays.stream(exprs).map(Expression::getReturnType).toArray(Class[]::new));
            return new ExpressionList<>(
                exprs,
                returnType,
                isAndList
            );
        }
    }

    @Nullable
    public static <T> Expression<? extends T> parseLiteral(String s, PatternType<T> expectedType) {
        Map<Class<?>, Type<?>> classToTypeMap = TypeManager.getClassToTypeMap();
        for (Class<?> c : classToTypeMap.keySet()) {
            Class<? extends T> expectedClass = expectedType.getType().getTypeClass();
            if (expectedClass.isAssignableFrom(c) || Converters.converterExists(c, expectedClass)) {
                Function<String, ?> literalParser = classToTypeMap.get(c).getLiteralParser();
                if (literalParser != null) {
                    T literal = (T) literalParser.apply(s);
                    if (literal != null && expectedClass.isAssignableFrom(c)) {
                        T[] one = (T[]) Array.newInstance(literal.getClass(), 1);
                        one[0] = literal;
                        return new SimpleLiteral<>(one);
                    } else if (literal != null) {
                        return new SimpleLiteral<>((Class<T>) c, literal).convertExpression(expectedType.getType().getTypeClass());
                    }
                } else if (expectedClass == String.class || c == String.class) {
                    VariableString vs = VariableString.newInstanceWithQuotes(s);
                    if (vs != null) {
                        return (Expression<? extends T>) vs;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public static Effect parseEffect(String s) {
        for (SyntaxInfo<? extends Effect> recentEffect : recentEffects) {
            Effect eff = matchEffectInfo(s, recentEffect);
            if (eff != null) {
                recentEffects.moveToFirst(recentEffect);
                return eff;
            }
        }
        // Let's not loop over the same elements again
        List<SyntaxInfo<? extends Effect>> remainingEffects = SyntaxManager.getEffects();
        recentEffects.removeFrom(remainingEffects);
        for (SyntaxInfo<? extends Effect> remainingEffect : remainingEffects) {
            Effect eff = matchEffectInfo(s, remainingEffect);
            if (eff != null) {
                recentEffects.moveToFirst(remainingEffect);
                return eff;
            }
        }
        // REMIND error
        return null;
    }

    /**
     * Tries to match an {@link ExpressionInfo} against the given {@link String} expression.
     * @param <T> The return type of the {@link Expression}
     * @return the Expression instance if matching, or {@literal null} otherwise
     */
    @Nullable
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
                    if (!expression.init(
                        parser.getParsedExpressions().toArray(new Expression[0]),
                        i,
                        parser.toParseResult()
                    )) {
                        continue;
                    }
                    Class<?> expressionReturnType = expression.getReturnType();
                    if (!expectedTypeClass.isAssignableFrom(expressionReturnType)) {
                        Expression<?> converted = expression.convertExpression(expectedTypeClass);
                        if (converted != null) {
                            return (Expression<? extends T>) converted;
                        } else {
                            Type<?> type = TypeManager.getByClass(expressionReturnType);
                            assert type != null;
                            // REMIND error
                            return null;
                        }
                    }
                    if (!expression.isSingle() &&
                        expectedType.isSingle()) {
                        // REMIND error
                        continue;
                    }
                    return expression;
                } catch (InstantiationException | IllegalAccessException e) {
                    // REMIND error
                }
            }
        }
        return null;
    }

    @Nullable
    private static Effect matchEffectInfo(String s, SyntaxInfo<? extends Effect> info) {
        List<PatternElement> patterns = info.getPatterns();
        for (int i = 0; i < patterns.size(); i++) {
            PatternElement element = patterns.get(i);
            SkriptParser parser = new SkriptParser(element);
            if (element.match(s, 0, parser) != -1) {
                try {
                    Effect effect = info.getSyntaxClass().newInstance();
                    if (!effect.init(
                        parser.getParsedExpressions().toArray(new Expression[0]),
                        i,
                        parser.toParseResult()
                    )) {
                        continue;
                    }
                    return effect;
                } catch (InstantiationException | IllegalAccessException e) {
                    // REMIND error
                }
            }
        }
        return null;
    }

    @Nullable
    public static Expression<Boolean> parseBooleanExpression(String s, boolean canBeConditional) {
        // I swear this is the cleanest way to do it
        if (s.equalsIgnoreCase("true")) {
            return new SimpleLiteral<>(Boolean.class, true);
        } else if (s.equalsIgnoreCase("false")) {
            return new SimpleLiteral<>(Boolean.class, false);
        }
        if (s.startsWith("(") && s.endsWith(")") && StringUtils.findClosingIndex(s, '(', ')', 0) == s.length() - 1) {
            s = s.substring(1, s.length() - 1);
        }
        for (ExpressionInfo<?, ?> info : recentExpressions) {
            if (info.getReturnType().getType().getTypeClass() != Boolean.class)
                continue;
            Expression<Boolean> expr = (Expression<Boolean>) matchExpressionInfo(s, info, BOOLEAN_PATTERN_TYPE);
            if (expr != null) {
                if (!canBeConditional && ConditionalExpression.class.isAssignableFrom(expr.getClass())) {
                    // REMIND error
                    return null;
                }
                recentExpressions.moveToFirst(info);
                return expr;
            }
        }
        // Let's not loop over the same elements again
        List<ExpressionInfo<?, ?>> remainingExpressions = SyntaxManager.getAllExpressions();
        recentExpressions.removeFrom(remainingExpressions);
        for (ExpressionInfo<?, ?> info : remainingExpressions) {
            if (info.getReturnType().getType().getTypeClass() != Boolean.class)
                continue;
            Expression<Boolean> expr = (Expression<Boolean>) matchExpressionInfo(s, info, BOOLEAN_PATTERN_TYPE);
            if (expr != null) {
                if (!canBeConditional && ConditionalExpression.class.isAssignableFrom(expr.getClass())) {
                    // REMIND error
                    return null;
                }
                recentExpressions.moveToFirst(info);
                return expr;
            }
        }
        // REMIND error
        return null;
    }

    @Nullable
    public static CodeSection parseSection(FileSection section) {
        for (SyntaxInfo<? extends CodeSection> recentSection : recentSections) {
            CodeSection sec = matchSectionInfo(section, recentSection);
            if (sec != null) {
                recentSections.moveToFirst(recentSection);
                return sec;
            }
        }
        List<SyntaxInfo<? extends CodeSection>> remainingSections = SyntaxManager.getSections();
        recentSections.removeFrom(remainingSections);
        for (SyntaxInfo<? extends CodeSection> remainingSection : remainingSections) {
            CodeSection sec = matchSectionInfo(section, remainingSection);
            if (sec != null) {
                recentSections.moveToFirst(remainingSection);
                return sec;
            }
        }
        // REMIND error
        return null;
    }

    @Nullable
    private static CodeSection matchSectionInfo(FileSection section, SyntaxInfo<? extends CodeSection> info) {
        List<PatternElement> patterns = info.getPatterns();
        for (int i = 0; i < patterns.size(); i++) {
            PatternElement element = patterns.get(i);
            SkriptParser parser = new SkriptParser(element);
            if (element.match(section.getLineContent(), 0, parser) != -1) {
                try {
                    CodeSection sec = info.getSyntaxClass().newInstance();
                    if (!sec.init(
                            parser.getParsedExpressions().toArray(new Expression[0]),
                            i,
                            parser.toParseResult()
                    )) {
                        continue;
                    }
                    sec.loadSection(section);
                    return sec;
                } catch (InstantiationException | IllegalAccessException e) {
                    // REMIND error
                }
            }
        }
        return null;
    }

}
