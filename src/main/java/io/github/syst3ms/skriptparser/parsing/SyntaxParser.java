package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.expressions.ExprBooleanOperators;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.*;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.lang.base.ContextExpression;
import io.github.syst3ms.skriptparser.log.ErrorContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.pattern.PatternParser;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SkriptEventInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import io.github.syst3ms.skriptparser.registration.context.ContextValue;
import io.github.syst3ms.skriptparser.registration.context.ContextValue.State;
import io.github.syst3ms.skriptparser.registration.context.ContextValues;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.conversions.Converters;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.RecentElementList;
import io.github.syst3ms.skriptparser.util.StringUtils;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.intellij.lang.annotations.MagicConstant;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Contains the logic for parsing and interpreting single statements, sections and expressions inside of a script.
 */
@SuppressWarnings("unchecked")
public class SyntaxParser {
    /**
     * Tells {@link #parseBooleanExpression(String, int, ParserState, SkriptLogger)} to only return expressions that are not conditional
     * @see #parseBooleanExpression(String, int, ParserState, SkriptLogger)
     */
    public static final int NOT_CONDITIONAL = 0;
    /**
     * Tells {@link #parseBooleanExpression(String, int, ParserState, SkriptLogger)} to return any expressions, conditional or not
     * @see #parseBooleanExpression(String, int, ParserState, SkriptLogger)
     */
    public static final int MAYBE_CONDITIONAL = 1;
    /**
     * Tells {@link #parseBooleanExpression(String, int, ParserState, SkriptLogger)} to only return conditional expressions
     * @see #parseBooleanExpression(String, int, ParserState, SkriptLogger)
     */
    public static final int CONDITIONAL = 2;

    public static final Pattern LIST_SPLIT_PATTERN = Pattern.compile("\\s*(,)\\s*|\\s+(and|n?or)\\s+", Pattern.CASE_INSENSITIVE);

    /**
     * The pattern type representing {@link Boolean}
     */
    public static final PatternType<Boolean> BOOLEAN_PATTERN_TYPE = new PatternType<>((Type<Boolean>) TypeManager.getByClass(Boolean.class).orElseThrow(AssertionError::new), true);
    /**
     * The pattern type representing {@link Object}
     */
    // Gradle requires the cast, but IntelliJ considers it redundant
    public static final PatternType<Object> OBJECT_PATTERN_TYPE = new PatternType<>((Type<Object>) TypeManager.getByClass(Object.class).orElseThrow(AssertionError::new), true);

    // Gradle requires the cast, but IntelliJ considers it redundant
    public static final PatternType<Object> OBJECTS_PATTERN_TYPE = new PatternType<>((Type<Object>) TypeManager.getByClass(Object.class).orElseThrow(AssertionError::new), false);


    // We control input, so new SkriptLogger instance is fine
    public static final PatternElement CONTEXT_VALUE_PATTERN = PatternParser.parsePattern("[the] (0:(past|previous)|1:|2:(future|next)) [ctx:context-]<.+>", new SkriptLogger()).orElseThrow();

    public static final ExpressionInfo<ExprBooleanOperators, Boolean> EXPRESSION_BOOLEAN_OPERATORS = SyntaxManager.getAllExpressions().stream()
            .filter(i -> i.getSyntaxClass() == ExprBooleanOperators.class)
            .findFirst()
            .map(val -> (ExpressionInfo<ExprBooleanOperators, Boolean>) val)
            .orElseThrow();

    /**
     * All {@link Effect effects} that are successfully parsed during parsing, in order of last successful parsing
     */
    private static final RecentElementList<SyntaxInfo<? extends Effect>> recentEffects = new RecentElementList<>();
    /**
     * All {@link CodeSection sections} that are successfully parsed during parsing, in order of last successful parsing
     */
    private static final RecentElementList<SyntaxInfo<? extends CodeSection>> recentSections = new RecentElementList<>();
    /**
     * All {@link SkriptEvent events} that are successfully parsed during parsing, in order of last successful parsing
     */
    private static final RecentElementList<SkriptEventInfo<?>> recentEvents = new RecentElementList<>();
    /**
     * All {@link Expression expressions} that are successfully parsed during parsing, in order of last successful parsing
     */
    private static final RecentElementList<ExpressionInfo<?, ?>> recentExpressions = new RecentElementList<>();
    /**
     * All {@link ConditionalExpression conditions} that are successfully parsed during parsing, in order of last successful parsing
     */
    private static final RecentElementList<ExpressionInfo<? extends ConditionalExpression, ? extends Boolean>> recentConditions = new RecentElementList<>();
    /**
     * All {@link ContextValue context values} that are successfully parsed during parsing, in order of last successful parsing
     */
    private static final RecentElementList<ContextValue<?, ?>> recentContextValues = new RecentElementList<>();

    /**
     * Parses an {@link Expression} from the given {@linkplain String} and {@link PatternType expected return type}
     * @param <T> the type of the expression
     * @param s the string to be parsed as an expression
     * @param expectedType the expected return type
     * @param parserState the current parser state
     * @param logger the logger
     * @return an expression that was successfully parsed, or {@literal null} if the string is empty,
     * no match was found
     * or for another reason detailed in an error message.
     */
    public static <T> Optional<? extends Expression<? extends T>> parseExpression(String s, PatternType<T> expectedType, ParserState parserState, SkriptLogger logger) {
        if (s.isEmpty())
            return Optional.empty();
        if (s.startsWith("(") && s.endsWith(")") && StringUtils.findClosingIndex(s, '(', ')', 0) == s.length() - 1) {
            s = s.substring(1, s.length() - 1);
        }

        var literal = parseLiteral(s, expectedType, parserState, logger);
        if (literal.isPresent()) {
            logger.clearErrors();
            return literal;
        }

        var variable = (Optional<? extends Variable<? extends T>>) Variables.parseVariable(s, expectedType.getType().getTypeClass(), parserState, logger);
        if (variable.isPresent()) {
            if (variable.filter(v -> !v.isSingle() && expectedType.isSingle()).isPresent()) {
                logger.error(
                        "A single value was expected, but " +
                                s +
                                " represents multiple values.",
                        ErrorType.SEMANTIC_ERROR,
                        "Use a loop/map to divert each element of this list into single elements"
                );
                return Optional.empty();
            } else {
                return variable;
            }
        }

        // This is to prevent us from exchanging boolean operators with lists.
        if (s.toLowerCase().startsWith("list ")) {
            s = s.substring("list ".length());
        } else {
            // We parse boolean operators first to prevent clutter while parsing.
            var booleanOperator = matchExpressionInfo(s, EXPRESSION_BOOLEAN_OPERATORS, expectedType, parserState, logger);
            if (booleanOperator.isPresent()) {
                recentExpressions.acknowledge(EXPRESSION_BOOLEAN_OPERATORS);
                logger.clearErrors();
                return booleanOperator;
            }
        }
        if (!expectedType.isSingle()) {
            var listLiteral = parseListLiteral(s, expectedType, parserState, logger);
            if (listLiteral.isPresent()) {
                logger.clearErrors();
                return listLiteral;
            }
        }
        for (var info : recentExpressions.mergeWith(SyntaxManager.getAllExpressions())) {
            var expr = matchExpressionInfo(s, info, expectedType, parserState, logger);
            if (expr.isPresent()) {
                if (parserState.isRestrictingExpressions() && parserState.forbidsSyntax(expr.get().getClass())) {
                    logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                    logger.error("The enclosing section does not allow the use of this expression: " + expr.get().toString(TriggerContext.DUMMY, logger.isDebug()), ErrorType.SEMANTIC_ERROR);
                    return Optional.empty();
                }
                recentExpressions.acknowledge(info);
                logger.clearErrors();
                return expr;
            }
            logger.forgetError();
        }

        // Parsing of standalone context values
        var contextValue = parseContextValue(s, expectedType, parserState, logger);
        if (contextValue.isPresent()) {
            logger.clearErrors();
            return contextValue;
        }

        logger.setContext(ErrorContext.NO_MATCH);
        logger.error("No expression matching '" + s + "' was found", ErrorType.NO_MATCH);
        return Optional.empty();
    }


    /**
     * Parses a {@link Expression boolean expression} from the given {@linkplain String}
     * @param s the string to be parsed as an expression
     * @param conditional a constant describing whether the result can be a {@link ConditionalExpression condition}
     * @param parserState the current parser state
     * @param logger the logger
     * @see SyntaxParser#NOT_CONDITIONAL
     * @see SyntaxParser#MAYBE_CONDITIONAL
     * @see SyntaxParser#CONDITIONAL
     * @return a boolean expression that was successfully parsed, or {@literal null} if the string is empty,
     * no match was found
     * or for another reason detailed in an error message.
     */
    public static Optional<? extends Expression<Boolean>> parseBooleanExpression(String s, @MagicConstant(intValues = {NOT_CONDITIONAL, MAYBE_CONDITIONAL, CONDITIONAL}) int conditional, ParserState parserState, SkriptLogger logger) {
        // I swear this is the cleanest way to do it
        if (s.startsWith("(") && s.endsWith(")") && StringUtils.findClosingIndex(s, '(', ')', 0) == s.length() - 1) {
            s = s.substring(1, s.length() - 1);
        }
        if (s.equalsIgnoreCase("true")) {
            return Optional.of(new SimpleLiteral<>(Boolean.class, true));
        } else if (s.equalsIgnoreCase("false")) {
            return Optional.of(new SimpleLiteral<>(Boolean.class, false));
        }
        var variable = (Optional<? extends Variable<Boolean>>) Variables.parseVariable(s, Boolean.class, parserState, logger);
        if (variable.isPresent()) {
            if (variable.filter(v -> !v.isSingle()).isPresent()) {
                logger.error(
                        "A single value was expected, but " +
                                s +
                                " represents multiple values.",
                        ErrorType.SEMANTIC_ERROR,
                        "Use a loop/map to divert each element of this list into single elements"
                );
                return Optional.empty();
            } else {
                return variable;
            }
        }
        for (var info : recentExpressions.mergeWith(SyntaxManager.getAllExpressions())) {
            if (info.getReturnType().getType().getTypeClass() != Boolean.class)
                continue;
            var expr = (Optional<? extends Expression<Boolean>>) matchExpressionInfo(s, info, BOOLEAN_PATTERN_TYPE, parserState, logger);
            if (expr.isPresent()) {
                switch (conditional) {
                    case NOT_CONDITIONAL: // Can't be conditional
                        if (ConditionalExpression.class.isAssignableFrom(expr.get().getClass())) {
                            logger.error(
                                    "The boolean expression must not be conditional",
                                    ErrorType.SEMANTIC_ERROR,
                                    "Rather than a condition, use a simple boolean here. Use 'whether %=boolean%' to convert a condition into a simple boolean"
                            );
                            return Optional.empty();
                        }
                        break;
                    case MAYBE_CONDITIONAL: // Can be conditional
                        if (ConditionalExpression.class.isAssignableFrom(expr.get().getClass())) {
                            recentConditions.acknowledge((ExpressionInfo<? extends ConditionalExpression, ? extends Boolean>) info);
                        }
                    case CONDITIONAL: // Has to be conditional
                        if (!ConditionalExpression.class.isAssignableFrom(expr.get().getClass())) {
                            logger.error(
                                    "The boolean expression must be conditional",
                                    ErrorType.SEMANTIC_ERROR,
                                    "Rather than a simple boolean, use a condition here, like '{x} is more than 10'"
                            );
                            return Optional.empty();
                        }
                    default: // You just want me dead, don't you ?
                        break;
                }
                recentExpressions.acknowledge(info);
                logger.clearErrors();
                return expr;
            }
            logger.forgetError();
        }

        logger.setContext(ErrorContext.NO_MATCH);
        logger.error("No expression matching '" + s + "' was found", ErrorType.NO_MATCH);
        return Optional.empty();
    }

    /**
     * Parses a {@link ContextValue} from the given {@linkplain String} and {@link PatternType expected return type}
     * @param <T> the type of the context value
     * @param toParse the string to be parsed as a context value
     * @param expectedType the expected return type
     * @param parserState the current parser state
     * @param logger the logger
     * @return an expression that was successfully parsed, or {@literal null} if the string is empty,
     * no match was found
     * or for another reason detailed in an error message.
     */
    public static <T> Optional<? extends ContextExpression<?, ? extends T>> parseContextValue(String toParse, PatternType<T> expectedType, ParserState parserState, SkriptLogger logger) {
        var matchContext = new MatchContext(CONTEXT_VALUE_PATTERN, parserState, logger);
        CONTEXT_VALUE_PATTERN.match(toParse, 0, matchContext);

        var parseContext = matchContext.toParseResult();
        var state = State.values()[parseContext.getNumericMark()];
        boolean alone = !parseContext.hasMark("ctx");
        var value = parseContext.getMatches().get(0).group();

        for (Class<? extends TriggerContext> ctx : parseContext.getParserState().getCurrentContexts()) {
            for (var info : recentContextValues.mergeWith(ContextValues.getContextValues(ctx))) {
                matchContext = new MatchContext(info.getPattern(), parserState, logger);

                // Checking all conditions, so no false results slip through.
                if (info.getPattern().match(value, 0, matchContext) == -1 || !expectedType.getType().getTypeClass().isAssignableFrom(info.getReturnType().getType().getTypeClass())) {
                    continue;
                } else if (!info.getUsage().isCorrect(alone)) {
                    if (alone) {
                        logger.error(
                                "The context value matching '" + toParse + "' cannot be used alone",
                                ErrorType.SEMANTIC_ERROR,
                                "Use 'context-something' instead of using this context value alone"
                        );
                    } else {
                        logger.error(
                                "The context value matching '" + toParse + "' must be used alone",
                                ErrorType.SEMANTIC_ERROR,
                                "Instead of 'context-something', use this context value as 'something'"
                        );
                    }
                    return Optional.empty();
                } else if (state != info.getState()) {
                    logger.error("The time state of this context value (" + state.toString().toLowerCase() + ") is incorrect", ErrorType.SEMANTIC_ERROR);
                    continue;
                } else if (expectedType.isSingle() && !info.getReturnType().isSingle()) {
                    logger.error(
                            "A single value was expected, but " + toParse + " represents multiple values.",
                            ErrorType.SEMANTIC_ERROR,
                            "Use a loop/map to divert each element of this list into single elements"
                    );
                    return Optional.empty();
                } else if (parserState.isRestrictingExpressions() && parserState.forbidsSyntax(ContextExpression.class)) {
                    logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                    logger.error("The enclosing section does not allow the use of context expressions.", ErrorType.SEMANTIC_ERROR);
                    return Optional.empty();
                }

                recentContextValues.acknowledge(info);
                return Optional.of(new ContextExpression<>((ContextValue<?, T>) info, value, alone));
            }
        }
        return Optional.empty();
    }

    private static <T> Optional<? extends Expression<? extends T>> matchExpressionInfo(String s, ExpressionInfo<?, ?> info, PatternType<T> expectedType, ParserState parserState, SkriptLogger logger) {
        var patterns = info.getPatterns();
        var infoType = info.getReturnType();
        var infoTypeClass = infoType.getType().getTypeClass();
        var expectedTypeClass = expectedType.getType().getTypeClass();
        if (!expectedTypeClass.isAssignableFrom(infoTypeClass) && !Converters.converterExists(infoTypeClass, expectedTypeClass))
            return Optional.empty();
        for (var i = 0; i < patterns.size(); i++) {
            var element = patterns.get(i);
            logger.setContext(ErrorContext.MATCHING);
            var parser = new MatchContext(element, parserState, logger);
            if (element.match(s, 0, parser) == s.length()) {
                try {
                    var expression = (Expression<? extends T>) info.getSyntaxClass()
                            .getDeclaredConstructor()
                            .newInstance();
                    logger.setContext(ErrorContext.INITIALIZATION);
                    if (!expression.init(
                            parser.getParsedExpressions().toArray(new Expression[0]),
                            i,
                            parser.toParseResult()
                    )) {
                        continue;
                    }
                    logger.setContext(ErrorContext.CONSTRAINT_CHECKING);
                    Class<?> expressionReturnType = expression.getReturnType();
                    if (!expectedTypeClass.isAssignableFrom(expressionReturnType)) { // Would only screw up in case of bad dynamic type usage
                        var converted = expression.convertExpression(expectedTypeClass);
                        if (converted.isPresent()) {
                            return converted;
                        } else {
                            var type = TypeManager.getByClass(expressionReturnType);
                            assert type.isPresent();
                            logger.error(StringUtils.withIndefiniteArticle(expectedType.toString(), false) +
                                    " was expected, but " +
                                    StringUtils.withIndefiniteArticle(type.get().toString(), false) +
                                    " was found", ErrorType.SEMANTIC_ERROR);
                            return Optional.empty();
                        }
                    }
                    if (!expression.isSingle() &&
                            expectedType.isSingle()) {
                        logger.error(
                                "A single value was expected, but '" + s + "' represents multiple values.",
                                ErrorType.SEMANTIC_ERROR,
                                "Use a loop/map to divert each element of this list into single elements"
                        );
                        continue;
                    }
                    if (parserState.isRestrictingExpressions() && parserState.forbidsSyntax(expression.getClass())) {
                        logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                        logger.error(
                                "The enclosing section does not allow the use of this expression: "
                                        + expression.toString(TriggerContext.DUMMY, logger.isDebug()),
                                ErrorType.SEMANTIC_ERROR,
                                "The current section limits the usage of syntax. This means that certain syntax cannot be used here, which was the case. Remove this expression entirely and refer to the documentation for the correct usage of this section"
                        );
                        continue;
                    }
                    return Optional.of(expression);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    logger.error("Couldn't instantiate class '" + info.getSyntaxClass().getName() + "'", ErrorType.EXCEPTION);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Parses a list literal expression (of the form {@code ..., ... and ...}) from the given {@linkplain String}  and {@link PatternType expected return type}
     * @param <T> the type of the list literal
     * @param s the string to be parsed as a list literal
     * @param expectedType the expected return type (must be plural)
     * @param parserState the current parser state
     * @param logger the logger
     * @return a list literal that was successfully parsed, or {@literal null} if the string is empty,
     * no match was found
     * or for another reason detailed in an error message.
     */
    public static <T> Optional<? extends Expression<? extends T>> parseListLiteral(String s, PatternType<T> expectedType, ParserState parserState, SkriptLogger logger) {
        assert !expectedType.isSingle();
        if (!s.contains(",") && !s.contains("and") && !s.contains("nor") && !s.contains("or"))
            return Optional.empty();
        List<String> parts = new ArrayList<>();
        var m = LIST_SPLIT_PATTERN.matcher(s);
        var lastIndex = 0;
        for (var i = 0; i < s.length(); i = StringUtils.nextSimpleCharacterIndex(s, i + 1)) {
            if (i == -1) {
                return Optional.empty();
            } else if (StringUtils.nextSimpleCharacterIndex(s, i) > i) { // We are currently at the start of something we need to skip over
                i = StringUtils.nextSimpleCharacterIndex(s, i) - 1;
                continue;
            }
            var c = s.charAt(i);
            if (c == ' ' || c == ',') {
                m.region(i, s.length());
                if (m.lookingAt()) {
                    if (i == lastIndex)
                        return Optional.empty();
                    parts.add(s.substring(lastIndex, i));
                    parts.add(m.group());
                    i = m.end() - 1;
                    lastIndex = i;
                }
            } else if (c == '(') {
                var closing = StringUtils.getEnclosedText(s, '(', ')', i);
                if (closing.isPresent()) {
                    var finalI = i; // Lambdas require it
                    i = closing.map(cl -> finalI + cl.length() + 1).orElse(i);
                }
            }
        }
        if (lastIndex < s.length() - 1)
            parts.add(s.substring(lastIndex));
        if (parts.size() == 1)
            return Optional.empty();
        Boolean isAndList = null; // Hello nullable booleans, it had been a pleasure NOT using you
        for (var i = 0; i < parts.size(); i++) {
            if ((i & 1) == 1) { // Odd index == separator
                var separator = parts.get(i).strip();
                if (separator.equalsIgnoreCase("and") || separator.equalsIgnoreCase("nor")) {
                    isAndList = true;
                } else if (separator.equalsIgnoreCase("or")) {
                    isAndList = isAndList != null && isAndList;
                }
            }
        }
        isAndList = isAndList == null || isAndList; // Defaults to true
        List<Expression<? extends T>> expressions = new ArrayList<>();
        var isLiteralList = true;
        for (var i = 0; i < parts.size(); i++) {
            if ((i & 1) == 0) { // Even index == element
                var part = parts.get(i).strip();
                logger.recurse();
                var expression = parseExpression(part, expectedType, parserState, logger);
                logger.callback();
                if (expression.isEmpty())
                    return Optional.empty();
                isLiteralList &= Literal.isLiteral(expression.get());
                expressions.add(expression.get());
            }
        }
        if (expressions.size() == 1)
            return Optional.of(expressions.get(0));
        if (isLiteralList) {
            Literal<?>[] literals = new Literal[expressions.size()];
            for (var i = 0; i < expressions.size(); i++) {
                var exp = expressions.get(i);
                if (exp instanceof Literal) {
                    literals[i] = (Literal<?>) exp;
                } else {
                    assert exp instanceof VariableString;
                    literals[i] = new SimpleLiteral<>(String.class, (String) exp.getSingle(TriggerContext.DUMMY).orElseThrow(AssertionError::new));
                }
            }
            var returnType = ClassUtils.getCommonSuperclass(false, Arrays.stream(literals).map(Literal::getReturnType).toArray(Class[]::new));
            return Optional.of(new LiteralList<>(
                    (Literal<? extends T>[]) literals,
                    (Class<T>) returnType,
                    isAndList
            ));
        } else {
            Expression<?>[] exprs = expressions.toArray(new Expression[0]);
            var returnType = ClassUtils.getCommonSuperclass(false, Arrays.stream(exprs).map(Expression::getReturnType).toArray(Class[]::new));
            return Optional.of(new ExpressionList<>(
                    (Expression<? extends T>[]) exprs,
                    (Class<T>) returnType,
                    isAndList
            ));
        }
    }

    /**
     * Parses a literal of a given {@link PatternType type} from the given {@linkplain String}
     * @param <T> the type of the literal
     * @param s the string to be parsed as a literal
     * @param expectedType the expected return type
     * @param parserState the current parser state
     * @param logger the logger
     * @return a literal that was successfully parsed, or {@literal null} if the string is empty,
     * no match was found
     * or for another reason detailed in an error message.
     */
    public static <T> Optional<? extends Expression<? extends T>> parseLiteral(String s, PatternType<T> expectedType, ParserState parserState, SkriptLogger logger) {
        var classToTypeMap = TypeManager.getClassToTypeMap();
        for (var c : classToTypeMap.keySet()) {
            Class<? extends T> expectedClass = expectedType.getType().getTypeClass();
            if (expectedClass.isAssignableFrom(c) || Converters.converterExists(c, expectedClass)) {
                Optional<? extends Function<String, ?>> literalParser = classToTypeMap.get(c).getLiteralParser();
                if (literalParser.isPresent()) {
                    var literal = literalParser.map(l -> (T) l.apply(s));
                    if (literal.isPresent() && expectedClass.isAssignableFrom(c)) {
                        return Optional.of(new SimpleLiteral<>((Class<T>) literal.get().getClass(), literal.get()));
                    } else if (literal.isPresent()) {
                        return new SimpleLiteral<>((Class<T>) c, literal.get()).convertExpression(expectedType.getType().getTypeClass());
                    }
                } else if (expectedClass == String.class || c == String.class) {
                    var vs = VariableString.newInstanceWithQuotes(s, parserState, logger)
                            .map(v -> (Expression<? extends T>) v);
                    if (vs.isPresent()) {
                        return vs;
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Parses a line of code as an {@link Effect}
     * @param s the line to be parsed
     * @param parserState the current parser state
     * @param logger the logger
     * @return an effect that was successfully parsed, or {@literal null} if the string is empty,
     * no match was found
     * or for another reason detailed in an error message
     */
    public static Optional<? extends Effect> parseEffect(String s, ParserState parserState, SkriptLogger logger) {
        if (s.isEmpty())
            return Optional.empty();

        for (var recentEffect : recentEffects.mergeWith(SyntaxManager.getEffects())) {
            var eff = matchEffectInfo(s, recentEffect, parserState, logger);
            if (eff.isPresent()) {
                if (parserState.forbidsSyntax(eff.get().getClass())) {
                    logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                    logger.error("The enclosing section does not allow the use of this effect: " + eff.get().toString(TriggerContext.DUMMY, logger.isDebug()), ErrorType.SEMANTIC_ERROR);
                    return Optional.empty();
                }
                recentEffects.acknowledge(recentEffect);
                logger.clearErrors();
                return eff;
            }
            logger.forgetError();
        }

        logger.setContext(ErrorContext.NO_MATCH);
        logger.error("No effect matching '" + s + "' was found", ErrorType.NO_MATCH);
        return Optional.empty();
    }

    private static Optional<? extends Effect> matchEffectInfo(String s, SyntaxInfo<? extends Effect> info, ParserState parserState, SkriptLogger logger) {
        var patterns = info.getPatterns();
        for (var i = 0; i < patterns.size(); i++) {
            var element = patterns.get(i);
            logger.setContext(ErrorContext.MATCHING);
            var parser = new MatchContext(element, parserState, logger);
            if (element.match(s, 0, parser) == s.length()) {
                try {
                    var eff = info.getSyntaxClass()
                            .getDeclaredConstructor()
                            .newInstance();
                    logger.setContext(ErrorContext.INITIALIZATION);
                    if (!eff.init(
                            parser.getParsedExpressions().toArray(new Expression[0]),
                            i,
                            parser.toParseResult()
                    )) {
                        continue;
                    }
                    return Optional.of(eff);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    logger.error("Couldn't instantiate class " + info.getSyntaxClass(), ErrorType.EXCEPTION);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Parses a section of a file as a {@link CodeSection}
     * @param section the section to be parsed
     * @param parserState the current parser state
     * @param logger the logger
     * @return a section that was successfully parsed, or {@literal null} if the section is empty,
     * no match was found or for another reason detailed in an error message
     */
    public static Optional<? extends CodeSection> parseSection(FileSection section, ParserState parserState, SkriptLogger logger) {
        var content = section.getLineContent();
        if (content.isEmpty())
            return Optional.empty();

        for (var toParse : recentSections.mergeWith(SyntaxManager.getSections())) {
            var sec = matchSectionInfo(section, toParse, parserState, logger);
            if (sec.isPresent()) {
                if (parserState.forbidsSyntax(sec.get().getClass())) {
                    logger.setContext(ErrorContext.RESTRICTED_SYNTAXES);
                    logger.error("The enclosing section does not allow the use of this section: " + sec.get().toString(TriggerContext.DUMMY, logger.isDebug()), ErrorType.SEMANTIC_ERROR);
                    return Optional.empty();
                }
                recentSections.acknowledge(toParse);
                logger.clearErrors();
                return sec;
            }
            logger.forgetError();
        }

        logger.setContext(ErrorContext.NO_MATCH);
        logger.error("No section matching '" + content + "' was found", ErrorType.NO_MATCH);
        return Optional.empty();
    }

    private static Optional<? extends CodeSection> matchSectionInfo(FileSection section, SyntaxInfo<? extends CodeSection> info, ParserState parserState, SkriptLogger logger) {
        var patterns = info.getPatterns();
        for (var i = 0; i < patterns.size(); i++) {
            var element = patterns.get(i);
            logger.setContext(ErrorContext.MATCHING);
            var parser = new MatchContext(element, parserState, logger);
            if (element.match(section.getLineContent(), 0, parser) != -1) {
                try {
                    var sec = info.getSyntaxClass()
                            .getDeclaredConstructor()
                            .newInstance();
                    logger.setContext(ErrorContext.INITIALIZATION);
                    if (!sec.init(
                            parser.getParsedExpressions().toArray(Expression[]::new),
                            i,
                            parser.toParseResult())) {
                        continue;
                    }
                    if (!sec.loadSection(section, parserState, logger)) {
                        continue;
                    }
                    return Optional.of(sec);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    logger.error("Couldn't instantiate class " + info.getSyntaxClass(), ErrorType.EXCEPTION);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Parses a section of a file as a {@link Trigger}
     * @param section the section to be parsed
     * @param logger the logger
     * @return a trigger that was successfully parsed, or {@literal null} if the section is empty,
     * no match was found
     * or for another reason detailed in an error message
     */
    public static Optional<? extends UnloadedTrigger> parseTrigger(FileSection section, SkriptLogger logger) {
        if (section.getLineContent().isEmpty())
            return Optional.empty();
        for (var info : recentEvents.mergeWith(SyntaxManager.getEvents())) {
            var trigger = matchEventInfo(section, info, logger);
            if (trigger.isPresent()) {
                recentEvents.acknowledge(info);
                logger.clearErrors();
                return trigger;
            }
            logger.forgetError();
        }

        logger.setContext(ErrorContext.NO_MATCH);
        logger.error("No trigger matching '" + section.getLineContent() + "' was found", ErrorType.NO_MATCH);
        return Optional.empty();
    }

    private static Optional<? extends UnloadedTrigger> matchEventInfo(FileSection section, SkriptEventInfo<?> info, SkriptLogger logger) {
        var patterns = info.getPatterns();
        for (var i = 0; i < patterns.size(); i++) {
            var element = patterns.get(i);
            var parserState = new ParserState();
            logger.setContext(ErrorContext.MATCHING);
            var parser = new MatchContext(element, parserState, logger);
            if (element.match(section.getLineContent(), 0, parser) != -1) {
                try {
                    var event = info.getSyntaxClass()
                            .getDeclaredConstructor()
                            .newInstance();
                    logger.setContext(ErrorContext.INITIALIZATION);
                    if (!event.init(
                            parser.getParsedExpressions().toArray(new Expression[0]),
                            i,
                            parser.toParseResult()
                    )) {
                        continue;
                    }
                    var trig = new Trigger(event);
                    parserState.setCurrentContexts(info.getContexts());
                    /*
                     * We don't actually load the trigger here, that will be left to the loading priority system
                     */
                    return Optional.of(new UnloadedTrigger(trig, section, logger.getLine(), info, parserState));
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    logger.error("Couldn't instantiate class " + info.getSyntaxClass(), ErrorType.EXCEPTION);
                }
            }
        }
        return Optional.empty();
    }
}
