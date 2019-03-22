package io.github.syst3ms.skriptparser.expressions;

import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.ExpressionList;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
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

/**
 * A very general condition, it simply compares two values. Usually you can only compare for equality (e.g. text is/isn't &lt;text&gt;),
 * but some values can also be compared using greater than/less than. In that case you can also test for whether an object is between two others.
 *
 * @name Comparison
 * @type CONDITION
 * @pattern [neither] %objects% ((is|are)[(n't| not| neither)] (greater|more|higher|bigger|larger|above) [than] or (equal to|the same as)|\\>=) %objects%
 * @pattern [neither] %objects% ((is|are)[(n't| not| neither)] (less|smaller|below) [than] or (equal to|the same as)|\\<=) %objects%
 * @pattern [neither] %objects% ((is|are)[(n't| not| neither)] ((greater|more|higher|bigger|larger) than|above)|\\>) %objects%
 * @pattern [neither] %objects% ((is|are)[(n't| not| neither)] ((less|smaller) than|below)|\\<) %objects%
 * @pattern [neither] %objects% (is not|are not|isn't|aren't) between %objects% and %objects%
 * @pattern [neither] %objects% (is|are) between %objects% and %objects%
 * @pattern [neither] %objects% ((is|are) (not|neither)|isn't|aren't|!=) [equal to] %objects%
 * @pattern [neither] %objects% (is|are|=) [(equal to|the same as)] %objects%
 * @since ALPHA
 * @author Syst3ms
 */
public class CondExprCompare extends ConditionalExpression {
    public static final PatternInfos<Relation> PATTERNS = new PatternInfos<>(new Object[][] {
            {"[1¦neither] %objects% ((is|are)[2¦(n't| not|4¦ neither)] (greater|more|higher|bigger|larger|above) [than] or (equal to|the same as)|\\>=) %objects%", Relation.GREATER_OR_EQUAL},
            {"[1¦neither] %objects% ((is|are)[2¦(n't| not|4¦ neither)] (less|smaller|below) [than] or (equal to|the same as)|\\<=) %objects%", Relation.SMALLER_OR_EQUAL},
            {"[1¦neither] %objects% ((is|are)[2¦(n't| not|4¦ neither)] ((greater|more|higher|bigger|larger) than|above)|\\>) %objects%", Relation.GREATER},
            {"[1¦neither] %objects% ((is|are)[2¦(n't| not|4¦ neither)] ((less|smaller) than|below)|\\<) %objects%", Relation.SMALLER},
            {"[1¦neither] %objects% [2¦](is not|are not|isn't|aren't) between %objects% and %objects%", Relation.EQUAL},
            {"[1¦neither] %objects% (is|are) between %objects% and %objects%", Relation.EQUAL},
            {"[1¦neither] %objects% [2¦]((is|are) (not|4¦neither)|isn't|aren't|!=) [equal to] %objects%", Relation.EQUAL},
            {"[1¦neither] %objects% (is|are|=) [(equal to|the same as)] %objects%", Relation.EQUAL}
        }
    );

    static {
        Main.getMainRegistration().addExpression(
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
    private Comparator<Object, Object> comp;

    @SuppressWarnings("null")
    @Override
    public boolean init(Expression<?>[] vars, int matchedPattern, ParseContext result) {
        first = vars[0];
        second = vars[1];
        if (vars.length == 3)
            third = vars[2];
        SkriptLogger logger = result.getLogger();
        relation = PATTERNS.getInfo(matchedPattern);
        if ((result.getParseMark() & 2) != 0) // "not" somewhere in the condition
            setNegated(true);

        if ((result.getParseMark() & 1) != 0) // "neither" on the left side
            setNegated(!isNegated());
        if ((result.getParseMark() & 4) != 0) {// "neither" on the right side
            if (second instanceof ExpressionList) {
                second.setAndList(!second.isAndList());
            }
            if (third instanceof ExpressionList) {
                third.setAndList(!second.isAndList());
            }
        }
        Expression<?> third = this.third;
        if (!initialize()) {
            if (third == null) {
                logger.error("'" + first.toString(null, false) + "' and '" + second.toString(null, false) + "' cannot be compared");
                return false;
            } else {
                logger.error("'" + first.toString(null, false) + "' cannot be compared with '" + second.toString(null, false) + "' and '" + third.toString(null, false) + "'");
                return false;
            }
        }
        @SuppressWarnings("rawtypes")
        Comparator comp = this.comp;
        if (comp != null) {
            if (third == null) {
                return relation.isEqualOrInverse() || comp.supportsOrdering();
            } else if (!comp.supportsOrdering()) {
                logger.error(errorString(first) + " cannot be ordered between " + errorString(second) + " and " + errorString(third));
                return false;
            }
        }
        return true;
    }

    @Nullable
    private String errorString(Expression<?> expr) {
        if (expr.getReturnType() == Object.class)
            return expr.toString(null, false);
        Type<?> exprType = TypeManager.getByClass(expr.getReturnType());
        assert exprType != null;
        return StringUtils.withIndefiniteArticle(exprType.getBaseName(), !expr.isSingle());
    }

    @SuppressWarnings({"unchecked"})
    private boolean initialize() {
        Expression<?> third = this.third;
        if (first.getReturnType() == Object.class) {
            Expression<?> e = first.convertExpression(Object.class);
            if (e == null) {
                return false;
            }
            first = e;
        }
        if (second.getReturnType() == Object.class) {
            Expression<?> e = second.convertExpression(Object.class);
            if (e == null) {
                return false;
            }
            second = e;
        }
        if (third != null && third.getReturnType() == Object.class) {
            Expression<?> e = third.convertExpression(Object.class);
            if (e == null) {
                return false;
            }
            this.third = third = e;
        }
        Class<?> f = first.getReturnType();
        Class<?> s = third == null
                     ? second.getReturnType()
                     : ClassUtils.getCommonSuperclass(second.getReturnType(), third.getReturnType());
        if (f == Object.class || s == Object.class)
            return true;
        comp = (Comparator<Object, Object>) Comparators.getComparator(f, s);
        return comp != null;
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
        Expression<?> third = this.third;
        return first.check(
                ctx,
            o1 -> second.check(
                    ctx,
                o2 -> {
                    if (third == null) {
                        return relation.is(comp != null ?
                                               comp.apply(o1, o2)
                                               : Comparators.compare(o1, o2));
                    }
                    return third.check(
                            ctx,
                        o3 -> relation == Relation.NOT_EQUAL ^
                              (Relation.GREATER_OR_EQUAL.is(comp != null
                                                                ? comp.apply(o1, o2)
                                                                : Comparators.compare(o1, o2)) &&
                               Relation.SMALLER_OR_EQUAL.is(comp != null
                                                                ? comp.apply(o1, o3)
                                                                : Comparators.compare(o1, o3))
                              )
                    );
                },
                isNegated()
            )
        );
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        String s;
        Expression<?> third = this.third;
        if (third == null) {
            s = first.toString(ctx, debug) + " is " + (isNegated() ? "not " : "") + relation + " " + second.toString(
                    ctx, debug);
        } else {
            s = first.toString(ctx, debug) + " is " + (isNegated() ? "not " : "") + "between " + second.toString(ctx, debug) + " and " + third.toString(
                    ctx, debug);
        }
        if (debug) {
            s += " (comparator: " + comp + ")";
        }
        return s;
    }
}
