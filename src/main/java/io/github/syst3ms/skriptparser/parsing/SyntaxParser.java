package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Conditional;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.ExpressionList;
import io.github.syst3ms.skriptparser.lang.InlineCondition;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.LiteralList;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.Variable;
import io.github.syst3ms.skriptparser.lang.VariableString;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SkriptEventInfo;
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
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;

import javax.swing.plaf.nimbus.State;
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
    private static final RecentElementList<SkriptEventInfo<?>> recentEvents = new RecentElementList<>();
    private static final RecentElementList<ExpressionInfo<?, ?>> recentExpressions = new RecentElementList<>();
    private static final RecentElementList<ExpressionInfo<? extends ConditionalExpression, ? extends Boolean>> recentConditions = new RecentElementList<>();

    private static Class<? extends TriggerContext>[] currentContexts = new Class[]{};

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
            Expression<? extends T> expr = matchExpressionInfo(s, info, expectedType, currentContexts);
            if (expr != null) {
                recentExpressions.moveToFirst(info);
                return expr;
            }
        }
        // Let's not loop over the same elements again
        List<ExpressionInfo<?, ?>> remainingExpressions = SyntaxManager.getAllExpressions();
        recentExpressions.removeFrom(remainingExpressions);
        for (ExpressionInfo<?, ?> info : remainingExpressions) {
            Expression<? extends T> expr = matchExpressionInfo(s, info, expectedType, currentContexts);
            if (expr != null) {
                recentExpressions.moveToFirst(info);
                return expr;
            }
        }
        // REMIND error
        return null;
    }

    @Nullable
    public static Expression<Boolean> parseBooleanExpression(String s, @MagicConstant(intValues = {0, 1, 2}) int conditional) {
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
            Expression<Boolean> expr = (Expression<Boolean>) matchExpressionInfo(s, info, BOOLEAN_PATTERN_TYPE, currentContexts);
            if (expr != null) {
                switch (conditional) {
                    case 0: // Can't be conditional
                        if (ConditionalExpression.class.isAssignableFrom(expr.getClass())) {
                            // REMIND error
                            return null;
                        }
                        break;
                    case 2: // Has to be conditional
                        if (!ConditionalExpression.class.isAssignableFrom(expr.getClass())) {
                            // REMIND error
                            return null;
                        }
                    case 1: // Can be conditional
                        if (ConditionalExpression.class.isAssignableFrom(expr.getClass())) {
                            recentConditions.moveToFirst((ExpressionInfo<? extends ConditionalExpression, ? extends Boolean>) info);
                        }
                    default: // You just want me dead, don't you ?
                        break;
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
            Expression<Boolean> expr = (Expression<Boolean>) matchExpressionInfo(s, info, BOOLEAN_PATTERN_TYPE, currentContexts);
            if (expr != null) {
                switch (conditional) {
                    case 0: // Can't be conditional
                        if (ConditionalExpression.class.isAssignableFrom(expr.getClass())) {
                            // REMIND error
                            return null;
                        }
                        break;
                    case 2: // Has to be conditional
                        if (!ConditionalExpression.class.isAssignableFrom(expr.getClass())) {
                            // REMIND error
                            return null;
                        }
                    case 1: // Can be conditional
                        if (ConditionalExpression.class.isAssignableFrom(expr.getClass())) {
                            recentConditions.moveToFirst((ExpressionInfo<? extends ConditionalExpression, ? extends Boolean>) info);
                        }
                    default: // You just want me dead, don't you ?
                        break;
                }
                recentExpressions.moveToFirst(info);
                return expr;
            }
        }
        // REMIND error
        return null;
    }

    @Nullable
    public static InlineCondition parseInlineCondition(String s) {
        if (s.isEmpty())
            return null;
        Expression<Boolean> cond = parseBooleanExpression(s, 2);
        return cond != null ? new InlineCondition(cond) : null;
    }

    /**
     * Tries to match an {@link ExpressionInfo} against the given {@link String} expression.
     * @param <T> The return type of the {@link Expression}
     * @param currentContextss the current
     * @return the Expression instance if matching, or {@literal null} otherwise
     */
    @Nullable
    private static <T> Expression<? extends T> matchExpressionInfo(String s, ExpressionInfo<?, ?> info, PatternType<T> expectedType, Class<? extends TriggerContext>[] currentContextss) {
        List<PatternElement> patterns = info.getPatterns();
        PatternType<?> infoType = info.getReturnType();
        Class<?> infoTypeClass = infoType.getType().getTypeClass();
        Class<T> expectedTypeClass = expectedType.getType().getTypeClass();
        if (!expectedTypeClass.isAssignableFrom(infoTypeClass) && !Converters.converterExists(infoTypeClass, expectedTypeClass))
            return null;
        for (int i = 0; i < patterns.size(); i++) {
            PatternElement element = patterns.get(i);
            SkriptParser parser = new SkriptParser(element, currentContextss);
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
                    sb.append("(").append(s, i + 1, endIndex).append(")");
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
            Literal[] literals = expressions.toArray(new Literal[0]);
            Class<?> returnType = ClassUtils.getCommonSuperclass(Arrays.stream(literals).map(Literal::getReturnType).toArray(Class[]::new));
            return new LiteralList<>(
                literals,
                returnType,
                isAndList
            );
        } else {
            Expression[] exprs = expressions.toArray(new Expression[0]);
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
        if (s.isEmpty())
            return null;
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

    @Nullable
    private static Effect matchEffectInfo(String s, SyntaxInfo<? extends Effect> info) {
        List<PatternElement> patterns = info.getPatterns();
        for (int i = 0; i < patterns.size(); i++) {
            PatternElement element = patterns.get(i);
            SkriptParser parser = new SkriptParser(element, currentContexts);
            if (element.match(s, 0, parser) != -1) {
                try {
                    Effect eff = info.getSyntaxClass().newInstance();
                    if (!eff.init(
                        parser.getParsedExpressions().toArray(new Expression[0]),
                        i,
                        parser.toParseResult()
                    )) {
                        continue;
                    }
                    return eff;
                } catch (InstantiationException | IllegalAccessException e) {
                    // REMIND error
                }
            }
        }
        return null;
    }

    @Nullable
    public static Statement parseStatement(String s) {
        if (s.isEmpty())
            return null;
        if (s.regionMatches(true, 0, "continue if ", 0, "continue if ".length())) { // startsWithIgnoreCase
            InlineCondition cond = parseInlineCondition(s.substring("continue if ".length(), s.length()));
            if (cond != null)
                return cond;
        }
        return parseEffect(s); // If that's null, we wanted to return null anyway
    }

    @Nullable
    public static CodeSection parseSection(FileSection section) {
        if (section.getLineContent().isEmpty())
            return null;
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
            SkriptParser parser = new SkriptParser(element, currentContexts);
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

    public static Trigger parseTrigger(FileSection s) {
        if (s.getLineContent().isEmpty())
            return null;
        for (SkriptEventInfo<?> recentEvent : recentEvents) {
            Trigger trigger = matchEventInfo(s, recentEvent);
            if (trigger != null) {
                recentEvents.moveToFirst(recentEvent);
                recentEvent.getRegisterer().handleTrigger(trigger);
                return trigger;
            }
        }
        // Let's not loop over the same elements again
        List<SkriptEventInfo<?>> remainingEvents = SyntaxManager.getTriggers();
        recentEvents.removeFrom(remainingEvents);
        for (SkriptEventInfo<?> remainingEvent : remainingEvents) {
            Trigger trigger = matchEventInfo(s, remainingEvent);
            if (trigger != null) {
                recentEvents.moveToFirst(remainingEvent);
                remainingEvent.getRegisterer().handleTrigger(trigger);
                return trigger;
            }
        }
        // REMIND error
        return null;
    }

    @Nullable
    private static Trigger matchEventInfo(FileSection section, SkriptEventInfo<?> info) {
        List<PatternElement> patterns = info.getPatterns();
        for (int i = 0; i < patterns.size(); i++) {
            PatternElement element = patterns.get(i);
            SkriptParser parser = new SkriptParser(element, currentContexts);
            if (element.match(section.getLineContent(), 0, parser) != -1) {
                try {
                    SkriptEvent event = info.getSyntaxClass().newInstance();
                    if (!event.init(
                            parser.getParsedExpressions().toArray(new Expression[0]),
                            i,
                            parser.toParseResult()
                    )) {
                        continue;
                    }
                    Trigger trig = new Trigger(event);
                    trig.loadSection(section);
                    setCurrentContexts(info.getContexts());
                    return trig;
                } catch (InstantiationException | IllegalAccessException e) {
                    // REMIND error
                }
            }
        }
        return null;
    }

    static void setCurrentContexts(Class<? extends TriggerContext>[] currentContexts) {
        SyntaxParser.currentContexts = currentContexts;
    }
}
