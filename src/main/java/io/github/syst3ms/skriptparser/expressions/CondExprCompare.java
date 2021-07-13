package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.ExpressionList;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.comparisons.Comparator;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;
import io.github.syst3ms.skriptparser.util.ClassUtils;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

/**
 * A very general condition, it simply compares two values. Usually you can only compare for equality (e.g. text is/isn't &lt;text&gt;),
 * but some values can also be compared using greater than/less than. In that case you can also test for whether an object is between two others.
 *
 * @author Syst3ms
 * @name Comparison
 * @type CONDITION
 * @pattern [neither] %objects% [each] ((is|are)((n't [each]|[ each] not|[ each] neither)| [each]) (greater|more|higher|bigger|larger|above) [than] or (equal to|the same as)|\>=) %objects% [each|respectively]
 * @pattern [neither] %objects% [each] ((is|are)((n't [each]|[ each] not|[ each] neither)| [each]) (less|smaller|below) [than] or (equal to|the same as)|\<=) %objects% [each|respectively]
 * @pattern [neither] %objects% [each] ((is|are)((n't [each]|[ each] not|[ each] neither)| [each]) ((greater|more|higher|bigger|larger) than|above)|\>) %objects% [each|respectively]
 * @pattern [neither] %objects% [each] ((is|are)((n't [each]|[ each] not|[ each] neither)| [each]) ((less|smaller) than|below)|\<) %objects% [each|respectively]
 * @pattern [neither] %objects% [each] (is|are)(n't [each]|[ each] not) between %objects% and %objects% [each|respectively]
 * @pattern [neither] %objects% [each] (is|are) [each] between %objects% and %objects% [each|respectively]
 * @pattern [neither] %objects% [each] ((is|are)((n't [each]|[ each] not|[ each] neither)) [equal to]|!=) %objects% [each|respectively]
 * @pattern [neither] %objects% [each] ((is|are) [each] [equal to|the same as]|[=]=) %objects% [each|respectively]
 * @since ALPHA
 */
public class CondExprCompare extends ConditionalExpression {
    public static final PatternInfos<Relation> PATTERNS = new PatternInfos<>(new Object[][]{
            {"[1:neither] %objects% [8:each] ((is|are)(2:(n't [8:each]|[8: each] not|4:[8: each] neither)| [8:each]) (greater|more|higher|bigger|larger|above) [than] or (equal to|the same as)|\\>=) %objects% [0x10:each|0x18:respectively]", Relation.GREATER_OR_EQUAL},
            {"[1:neither] %objects% [8:each] ((is|are)(2:(n't [8:each]|[8: each] not|4:[8: each] neither)| [8:each]) (less|smaller|below) [than] or (equal to|the same as)|\\<=) %objects% [0x10:each|0x18:respectively]", Relation.SMALLER_OR_EQUAL},
            {"[1:neither] %objects% [8:each] ((is|are)(2:(n't [8:each]|[8: each] not|4:[8: each] neither)| [8:each]) ((greater|more|higher|bigger|larger) than|above)|\\>) %objects% [0x10:each|0x18:respectively]", Relation.GREATER},
            {"[1:neither] %objects% [8:each] ((is|are)(2:(n't [8:each]|[8: each] not|4:[8: each] neither)| [8:each]) ((less|smaller) than|below)|\\<) %objects% [0x10:each|0x18:respectively]", Relation.SMALLER},
            {"[1:neither] %objects% [8:each] (is|are)(2:n't [8:each]|2:[8: each] not) between %objects% and %objects% [0x10:each|0x18:respectively]", Relation.EQUAL},
            {"[1:neither] %objects% [8:each] (is|are) [8:each] between %objects% and %objects% [0x10:each|0x18:respectively]", Relation.EQUAL},
            {"[1:neither] %objects% [8:each] ((is|are)(2:(n't [8:each]|[8: each] not|4:[8: each] neither)) [equal to]|2:!=) %objects% [0x10:each|0x18:respectively]", Relation.EQUAL},
            {"[1:neither] %objects% [8:each] ((is|are) [8:each] [equal to|the same as]|=[=]) %objects% [0x10:each|0x18:respectively]", Relation.EQUAL}
    });

    static {
        Parser.getMainRegistration().addExpression(
                CondExprCompare.class,
                Boolean.class,
                true,
                1,
                PATTERNS.getPatterns()
        );
    }

    private Expression<?> first;
    private Expression<?> second;
    @Nullable
    private Expression<?> third;
    private Relation relation;
    @Nullable
    private Comparator<Object, Object> comparator;

    private boolean contentComparison;
    private boolean firstEach;
    private boolean secondEach;

    @SuppressWarnings("null")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext result) {
        first = expressions[0];
        second = expressions[1];
        if (expressions.length == 3)
            third = expressions[2];
        SkriptLogger logger = result.getLogger();
        relation = PATTERNS.getInfo(matchedPattern);
        int parseMark = result.getNumericMark();
        if ((parseMark & 2) != 0) // "not" somewhere in the condition
            setNegated(true);
        if ((parseMark & 1) != 0) // "neither" on the left side
            setNegated(!isNegated());
        if ((parseMark & 4) != 0) {// "neither" on the right side
            if (second instanceof ExpressionList) {
                second.setAndList(!second.isAndList());
            }
        }
        if ((parseMark & 0x18) != 0) {
            firstEach = (parseMark & 0x8) != 0;
            secondEach = (parseMark & 0x10) != 0;
            if (firstEach && secondEach && first.isSingle() && second.isSingle() && (third == null || third.isSingle())) {
                logger.warn("Using \"respectively\" or two \"each\" options on single values is redundant");
            } else if (firstEach
                    && secondEach
                    && (first.isAndList() != second.isAndList() ||
                        first.isSingle() != second.isSingle())) {
                /*
                 * Here we want to rule out some obviously impossible combinations:
                 *  - Respectively comparing a single value and a list
                 *  - Respectively comparing lists that aren't both "and" or "or"
                 */
                logger.error(
                        "'" +
                                first.toString(TriggerContext.DUMMY, logger.isDebug()) +
                                "' and '" +
                                second.toString(TriggerContext.DUMMY, logger.isDebug()) +
                                "' cannot be compared respectively",
                        ErrorType.SEMANTIC_ERROR
                );
                return false;
            } else if (third != null
                    && secondEach
                    && (firstEach && first.isAndList() != second.isAndList()
                        || second.isAndList() != third.isAndList()
                        || firstEach && first.isSingle() != second.isSingle()
                        || second.isSingle() != third.isSingle())) {
                /*
                 * What is ruled out here is using "each" when "second" and "third" don't match in number or in and/or type.
                 * Otherwise these are fundamentally the same checks as above but when "third" is present.
                 */
                logger.error(
                        "'" +
                                first.toString(TriggerContext.DUMMY, logger.isDebug()) +
                                "' cannot be compared respectively with '" +
                                second.toString(TriggerContext.DUMMY, logger.isDebug()) +
                                "' and '" +
                                third.toString(TriggerContext.DUMMY, logger.isDebug()) +
                                "'",
                        ErrorType.SEMANTIC_ERROR
                );
                return false;
            } else if (firstEach && first.isSingle()) {
                logger.warn("Using \"each\" on '" + first.toString(TriggerContext.DUMMY, logger.isDebug()) + "' is redundant, as it is a single value");
            } else if (secondEach && second.isSingle() && (third == null || third.isSingle())) {
                logger.warn("Using \"each\" on '" + second.toString(TriggerContext.DUMMY, logger.isDebug()) + "' is redundant, as it is a single value");
            }
        }
        /*
         * When writing something like "if {a::*} is equal to {b::*}", we would expect Skript to compare the content of
         * each list. However, it doesn't exactly do that. Using some literal values,
         * "if (1 and 2) is equal to (2 and 1)" is actually interpreted as :
         *  1 == 2 && 1 == 1 && 2 == 2 && 2 == 1
         * which isn't at all what we want.
         * Writing "if (1 and 2) is equal to (2 or 1)" is instead interpreted as :
         *  (1 == 2 || 1 == 1) && (2 == 2 || 2 == 1)
         * Sure, it *is* true, but so is "if (1, 2, 2, 2 and 2) is equal to (1 or 2)", still not what we want.
         *
         * With the new syntax that includes "each" and "respectively", writing "if {a::*} is {b::*} respectively" does
         * actually compare the contents, but it's order-sensitive. This makes sense, but there is still no nice way
         * to check if a list is equal to another independent of ordering. For numbers, a semi-clean solution would be to
         * sort both lists before comparing them, but not everything can be ordered.
         *
         * It is more reasonable to ensure that omitting "respectively" (and "each" along with it) will compare the
         * two lists order-insensitively.
         */
        contentComparison = third == null
                && relation == Relation.EQUAL
                && !(firstEach || secondEach)
                && !(first.isSingle() || second.isSingle());

        if (!initComparator()) {
            if (third == null) {
                logger.error(
                        "'" +
                        first.toString(TriggerContext.DUMMY, logger.isDebug()) +
                        "' and '" +
                        second.toString(TriggerContext.DUMMY, logger.isDebug()) +
                        "' cannot be compared",
                        ErrorType.SEMANTIC_ERROR
                );
            } else {
                logger.error(
                        "'" +
                        first.toString(TriggerContext.DUMMY, logger.isDebug()) +
                        "' cannot be compared with '" +
                        second.toString(TriggerContext.DUMMY, logger.isDebug()) +
                        "' and '" +
                        third.toString(TriggerContext.DUMMY, logger.isDebug()) +
                        "'",
                        ErrorType.SEMANTIC_ERROR
                );
            }
            return false;
        }
        if (comparator != null) {
            if (third == null) {
                return relation.isEqualOrInverse() || comparator.supportsOrdering();
            } else if (!comparator.supportsOrdering()) {
                logger.error(
                        errorString(first, logger.isDebug()) +
                        " cannot be ordered between " +
                        errorString(second, logger.isDebug()) +
                        " and " +
                        errorString(third, logger.isDebug()), ErrorType.SEMANTIC_ERROR);
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings({"unchecked"})
    private boolean initComparator() {
        Class<?> f = first.getReturnType();
        Class<?> s = third == null
                ? second.getReturnType()
                : ClassUtils.getCommonSuperclass(second.getReturnType(), third.getReturnType());

        if (f == Object.class || s == Object.class) {
            return true;
        } else if (f != s) {
            // Tries to convert the instances to each other.
            // Basically takes expression conversions into account.
            var converted = Expression.convertPair(first, second);
            if (!first.equals(converted.getFirst()) || !second.equals(converted.getSecond())) {
                first = converted.getFirst();
                second = converted.getSecond();
                comparator = (Comparator<Object, Object>) Comparators.getComparator(first.getReturnType(), second.getReturnType()).orElseThrow(AssertionError::new);
                return true;
            }
        }
        return (comparator = (Comparator<Object, Object>) Comparators.getComparator(f, s).orElse(null)) != null;
    }

    /*
     * # := condition (e.g. is, is less than, contains, is enchanted with, has permission, etc.)
     * !# := not #
     *
     * a and b # x === a # x && b # x
     * a or b # x === a # x || b # x
     * a # x and y === a # x && a # y
     * a # x or y === a # x || a # y
     * a and b # x and y === a # x and y && b # x and y === a # x && a # y && b # x && b # y
     * a and b # x or y === a # x or y && b # x or y
     * a or b # x and y === a # x and y || b # x and y
     * a or b # x or y === a # x or y || b # x or y
     *
     *
     * a and b !# x === a !# x && b !# x
     * neither a nor b # x === a !# x && b !# x		// nor = and
     * a or b !# x === a !# x || b !# x
     *
     * a !# x and y === a !# x || a !# y							// e.g. "player doesn't have 2 emeralds and 5 gold ingots" == "NOT(player has 2 emeralds and 5 gold ingots)" == "player doesn't have 2 emeralds OR player doesn't have 5 gold ingots"
     * a # neither x nor y === a !# x && a !# y		// nor = or 	// e.g. "player has neither 2 emeralds nor 5 gold ingots" == "player doesn't have 2 emeralds AND player doesn't have 5 gold ingots"
     * a # neither x nor y === a !# x && a !# y		// nor = or 	// e.g. "player is neither the attacker nor the victim" == "player is not the attacker AND player is not the victim"
     * a !# x or y === a !# x && a !# y								// e.g. "player doesn't have 2 emeralds or 5 gold ingots" == "NOT(player has 2 emeralds or 5 gold ingots)" == "player doesn't have 2 emeralds AND player doesn't have 5 gold ingots"
     *
     * a and b !# x and y === a !# x and y && b !# x and y === (a !# x || a !# y) && (b !# x || b !# y)
     * a and b !# x or y === a !# x or y && b !# x or y
     * a and b # neither x nor y === a # neither x nor y && b # neither x nor y
     *
     * a or b !# x and y === a !# x and y || b !# x and y
     * a or b !# x or y === a !# x or y || b !# x or y
     * a or b # neither x nor y === a # neither x nor y || b # neither x nor y
     *
     * neither a nor b # x and y === a !# x and y && b !# x and y		// nor = and
     * neither a nor b # x or y === a !# x or y && b !# x or y			// nor = and
     */
    @Override
    public boolean check(TriggerContext ctx) {
        Object[] firstValues = first.getValues(ctx);
        Object[] secondValues = second.getValues(ctx);
        Object[] thirdValues = third != null ? third.getValues(ctx) : null;
        if (thirdValues == null) {
            if (firstEach && secondEach) {
                if (firstValues.length != secondValues.length)
                    return false;
                assert first.isAndList() == second.isAndList();
                boolean and = first.isAndList();
                boolean hasElement = false;
                for (int i = 0; i < firstValues.length; i++) {
                    hasElement = true;
                    boolean b = fullCompare(
                            Arrays.copyOfRange(firstValues, i, i+1),
                            Arrays.copyOfRange(secondValues, i, i+1)
                    );
                    if (and && !b)
                        return false;
                    if (!and && b)
                        return true;
                }
                /*
                 * Note that we do not worry about isNegated() here, since that's already accounted for inside
                 * fullCompare and fullCompareWithThird, so instead we just use its default value of false.
                 */
                return hasElement && and;
            } else if (firstEach) {
                return Expression.check(
                        firstValues,
                        f -> fullCompare(new Object[]{f}, secondValues),
                        false,
                        first.isAndList()
                );
            } else if (secondEach) {
                return Expression.check(
                        secondValues,
                        s -> fullCompare(firstValues, new Object[]{s}),
                        false,
                        second.isAndList()
                );
            } else {
                return fullCompare(firstValues, secondValues);
            }
        } else if (firstEach && secondEach) {
            if (firstValues.length != secondValues.length || firstValues.length != thirdValues.length)
                return false;
            assert first.isAndList() == second.isAndList() && first.isAndList() == third.isAndList();
            boolean and = first.isAndList();
            boolean hasElement = false;
            for (int i = 0; i < firstValues.length; i++) {
                hasElement = true;
                boolean b = fullCompareWithThird(
                        Arrays.copyOfRange(firstValues, i, i+1),
                        Arrays.copyOfRange(secondValues, i, i+1),
                        Arrays.copyOfRange(thirdValues, i, i+1)
                );
                if (and && !b)
                    return false;
                if (!and && b)
                    return true;
            }
            return hasElement && and;
        } else if (firstEach) {
            return Expression.check(
                    firstValues,
                    f -> fullCompareWithThird(new Object[]{f}, secondValues, thirdValues),
                    false,
                    first.isAndList()
            );
        } else if (secondEach) {
            if (secondValues.length != thirdValues.length)
                return false;
            assert second.isAndList() == third.isAndList();
            boolean and = second.isAndList();
            boolean hasElement = false;
            for (int i = 0; i < secondValues.length; i++) {
                hasElement = true;
                boolean b = fullCompareWithThird(
                        firstValues,
                        Arrays.copyOfRange(secondValues, i, i+1),
                        Arrays.copyOfRange(thirdValues, i, i+1)
                );
                if (and && !b)
                    return false;
                if (!and && b)
                    return true;
            }
            return hasElement && and;
        } else {
            return fullCompareWithThird(firstValues, secondValues, thirdValues);
        }
    }

    private boolean fullCompare(Object[] firstValues, Object[] secondValues) {
        if (!contentComparison) {
            return Expression.check(
                    firstValues,
                    o1 -> Expression.check(
                            secondValues,
                            o2 -> relation.is(
                                    comparator != null ?
                                            comparator.apply(o1, o2)
                                            : Comparators.compare(o1, o2)
                            ),
                            false,
                            second.isAndList()
                    ),
                    isNegated(),
                    first.isAndList()
            );
        } else {
            if (firstValues.length != secondValues.length)
                return isNegated();
            for (Object f : firstValues) {
                boolean isContained = false;
                for (Object s : secondValues) {
                    if (comparator == null) {
                        if (Comparators.compare(f, s).is(Relation.EQUAL)) {
                            isContained = true;
                            break;
                        }
                    } else if (comparator.apply(f, s).is(Relation.EQUAL)) {
                        isContained = true;
                        break;
                    }
                }
                if (!isContained)
                    return isNegated();
            }
            return !isNegated();
        }
    }

    private boolean fullCompareWithThird(Object[] firstValues, Object[] secondValues, Object[] thirdValues) {
        assert third != null;
        return Expression.check(
                firstValues,
                o1 -> Expression.check(
                        secondValues,
                        o2 -> Expression.check(
                                thirdValues,
                                o3 -> {
                                    boolean isBetween;
                                    if (comparator != null) {
                                        isBetween =
                                                (Relation.GREATER_OR_EQUAL.is(comparator.apply(o1, o2)) &&
                                                        Relation.SMALLER_OR_EQUAL.is(comparator.apply(o1, o3)))
                                                        || // Check OPPOSITE (switching o2 / o3)
                                                        (Relation.GREATER_OR_EQUAL.is(comparator.apply(o1, o3)) &&
                                                                Relation.SMALLER_OR_EQUAL.is(comparator.apply(o1, o2)));
                                    } else {
                                        isBetween =
                                                (Relation.GREATER_OR_EQUAL.is(Comparators.compare(o1, o2)) &&
                                                        Relation.SMALLER_OR_EQUAL.is(Comparators.compare(o1, o3)))
                                                        || // Check OPPOSITE (switching o2 / o3)
                                                        (Relation.GREATER_OR_EQUAL.is(Comparators.compare(o1, o3)) &&
                                                                Relation.SMALLER_OR_EQUAL.is(Comparators.compare(o1, o2)));
                                    }
                                    return relation == Relation.NOT_EQUAL != isBetween;
                                },
                                false,
                                third.isAndList()
                        ),
                        false,
                        second.isAndList()
                ),
                isNegated(),
                first.isAndList()
        );
    }

    @Override
    public String toString(TriggerContext ctx, boolean debug) {
        String s;
        Expression<?> third = this.third;
        if (third == null) {
            s = first.toString(ctx, debug) + " is " + (isNegated() ? "not " : "") + relation + " " + second.toString(
                    ctx, debug);
        } else {
            s = first.toString(ctx, debug) +
                    " is " +
                    (isNegated() ? "not " : "") +
                    "between " +
                    second.toString(ctx, debug) +
                    " and " +
                    third.toString(ctx, debug);
        }
        if (debug) {
            s += " (comparator: " + comparator + ")";
        }
        return s;
    }

    private static String errorString(Expression<?> expr, boolean debug) {
        if (expr.getReturnType() == Object.class)
            return expr.toString(TriggerContext.DUMMY, debug);
        Optional<? extends Type<?>> exprType = TypeManager.getByClass(expr.getReturnType());
        assert exprType.isPresent();
        return StringUtils.withIndefiniteArticle(exprType.get().getBaseName(), !expr.isSingle());
    }
}
