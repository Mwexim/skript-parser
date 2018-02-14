package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.*;
import io.github.syst3ms.skriptparser.lang.interfaces.ConditionalExpression;
import io.github.syst3ms.skriptparser.lang.interfaces.DynamicNumberExpression;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.registration.SyntaxInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.lang.VariableString;
import io.github.syst3ms.skriptparser.util.Expressions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class SyntaxParser {
    public static final PatternType<Boolean> BOOLEAN_PATTERN_TYPE = new PatternType<>((Type<Boolean>) TypeManager.getByClass(Boolean.class), true);
    public static final PatternType<Object> OBJECT_PATTERN_TYPE = new PatternType<>((Type<Object>) TypeManager.getByClass(Object.class), true);
    private static final LinkedList<SyntaxInfo<? extends Effect>> recentEffects = new LinkedList<>();
    private static final LinkedList<SyntaxInfo<? extends CodeSection>> recentSections = new LinkedList<>();
    private static final LinkedList<ExpressionInfo<?, ?>> recentExpressions = new LinkedList<>();

    public static <T> Expression<? extends T> parseExpression(String s, PatternType<T> expectedType) {
	    if (expectedType.equals(BOOLEAN_PATTERN_TYPE)) {
	        throw new IllegalStateException("Parsing of boolean expressions should be delegated to parseBooleanExpression");
        }
        Expression<? extends T> typeClass = parseLiteral(s, expectedType);
        if (typeClass != null) return typeClass;
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
        return null;
    }

    public static <T> Expression<? extends T> parseLiteral(String s, PatternType<T> expectedType) {
        Map<Class<?>, Type<?>> classToTypeMap = TypeManager.getClassToTypeMap();
        for (Class<?> c : classToTypeMap.keySet()) {
            Class<T> typeClass = expectedType.getType().getTypeClass();
            if (typeClass.isAssignableFrom(c)) {
                Function<String, ?> literalParser = classToTypeMap.get(c).getLiteralParser();
                if (literalParser != null) {
                    T literal = (T) literalParser.apply(s);
                    if (literal != null) {
                        return new Literal<>(typeClass, literal);
                    }
                } else if (expectedType.getType().getTypeClass() == String.class) {
                    VariableString vs = VariableString.newInstanceWithQuotes(s);
                    if (vs != null) {
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
        for (int i = 0; i < patterns.size(); i++) {
            PatternType<?> returnType = info.getReturnType();
            PatternElement element = patterns.get(i);
            SkriptParser parser = new SkriptParser(element);
            if (element.match(s, 0, parser) != -1) {
                if (!DynamicNumberExpression.class.isAssignableFrom(info.getSyntaxClass()) &&
                    !returnType.isSingle() &&
                    expectedType.isSingle()) {
                    continue;
                }
                try {
                    Expression<? extends T> expression = (Expression<? extends T>) info.getSyntaxClass().newInstance();
                    expression.init(
                        parser.getParsedExpressions().toArray(new Expression[0]),
                        i,
                        parser.toParseResult()
                    );
                    Class<?> returnTypeClass = returnType.getType().getTypeClass();
                    Class<?> exprReturnType = Expressions.getReturnType(expression);
                    if (!returnTypeClass.isAssignableFrom(exprReturnType)) {
                        Expression<?> converted = Expressions.convertExpression(expression, returnTypeClass);
                        if (converted != null) {
                            return (Expression<? extends T>) converted;
                        } else {
                            error("Unmatching return types : expected " + returnTypeClass.getName() + " or subclass, but only found " + exprReturnType.getName());
                        }
                    }
                    if (DynamicNumberExpression.class.isAssignableFrom(expression.getClass()) &&
                        !((DynamicNumberExpression) expression).isSingle() &&
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
            return new Literal<>(Boolean.class, true);
        } else if (s.equalsIgnoreCase("false")) {
            return new Literal<>(Boolean.class, false);
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
