package io.github.syst3ms.skriptparser.expressions;

import com.sun.istack.internal.Nullable;
import io.github.syst3ms.skriptparser.Main;
import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.ExpressionList;
import io.github.syst3ms.skriptparser.lang.interfaces.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import io.github.syst3ms.skriptparser.registration.PatternInfos;
import io.github.syst3ms.skriptparser.types.ClassUtils;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.comparisons.Comparator;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;

public class CondExprComparisons extends ConditionalExpression {

    private static final PatternInfos<Relation> patterns = new PatternInfos<>(new Object[][] {
            {"(1¦neither|) %objects% ((is|are)(|2¦(n't| not|4¦ neither)) ((greater|more|higher|bigger|larger) than|above)|\\>) %objects%", Relation.GREATER},
            {"(1¦neither|) %objects% ((is|are)(|2¦(n't| not|4¦ neither)) (greater|more|higher|bigger|larger|above) [than] or (equal to|the same as)|\\>=) %objects%", Relation.GREATER_OR_EQUAL},
            {"(1¦neither|) %objects% ((is|are)(|2¦(n't| not|4¦ neither)) ((less|smaller) than|below)|\\<) %objects%", Relation.SMALLER},
            {"(1¦neither|) %objects% ((is|are)(|2¦(n't| not|4¦ neither)) (less|smaller|below) [than] or (equal to|the same as)|\\<=) %objects%", Relation.SMALLER_OR_EQUAL},
            {"(1¦neither|) %objects% (2¦)((is|are) (not|4¦neither)|isn't|aren't|!=) [equal to] %objects%", Relation.EQUAL},
            {"(1¦neither|) %objects% (is|are|=) [(equal to|the same as)] %objects%", Relation.EQUAL},
            {"(1¦neither|) %objects% (is|are) between %objects% and %objects%", Relation.EQUAL},
            {"(1¦neither|) %objects% (2¦)(is not|are not|isn't|aren't) between %objects% and %objects%", Relation.EQUAL}
        }
    );

    static {
        Main.getMainRegistration().addExpression(
                CondExprComparisons.class,
                Boolean.class,
                true,
                patterns.getPatterns()
        );
    }

    private Expression<?> first;
    private Expression<?> second;
    private Expression<?> third;
    private Relation relation;
    private Comparator<Object, Object> comp;

    @SuppressWarnings("null")
    @Override
    public boolean init(final Expression<?>[] vars, final int matchedPattern, final ParseResult result) {
        first = vars[0];
        second = vars[1];
        if (vars.length == 3)
            third = vars[2];
        relation = patterns.getInfo(matchedPattern);
        if ((result.getParseMark() & 2) != 0) // "not" somewhere in the condition
            setNegated(true);
        if ((result.getParseMark() & 1) != 0) // "neither" on the left side
            setNegated(!isNegated());
        if ((result.getParseMark() & 4) != 0) {// "neither" on the right side
            if (second instanceof ExpressionList)
                second.setAndList(!second.isAndList());
            if (third instanceof ExpressionList)
                third.setAndList(!second.isAndList());
        }
        final boolean b = initialize();
        final Expression<?> third = this.third;
        if (!b) {
            if (third == null && first.getReturnType() == Object.class && second.getReturnType() == Object.class) {
                return false;
            } else {
                //Skript.error("Can't compare " + toString(first) + " with " + toString(second) + (third == null ? "" : " and " + toString(third)), ErrorQuality.NOT_AN_EXPRESSION);
                return false;
            }
        }
        @SuppressWarnings("rawtypes")
        final Comparator comp = this.comp;
        if (comp != null) {
            if (third == null) {
                if (!relation.isEqualOrInverse() && !comp.supportsOrdering()) {
                    //Skript.error("Can't check " + toString(first) + " for being '" + relation + "' " + toString(second), ErrorQuality.NOT_AN_EXPRESSION);
                    return false;
                }
            } else {
                if (!comp.supportsOrdering()) {
                    //Skript.error("Can't check " + toString(first) + " for being 'between' " + toString(second) + " and " + toString(third), ErrorQuality.NOT_AN_EXPRESSION);
                    return false;
                }
            }
        }

        return true;
    }

    public static String toString(final Expression<?> e) {
        if (e.getReturnType() == Object.class)
            return e.toString(null, false);
        return TypeManager.getByClass(e.getReturnType()).getBaseName();
    }

    @SuppressWarnings({"unchecked"})
    private boolean initialize() {
        Expression<?> third = this.third;
        if (first.getReturnType() == Object.class) {
            final Expression<?> e = first.convertExpression(Object.class);
            if (e == null) {
                return false;
            }
            first = e;
        }
        if (second.getReturnType() == Object.class) {
            final Expression<?> e = second.convertExpression(Object.class);
            if (e == null) {
                return false;
            }
            second = e;
        }
        if (third != null && third.getReturnType() == Object.class) {
            final Expression<?> e = third.convertExpression(Object.class);
            if (e == null) {
                return false;
            }
            this.third = third = e;
        }
        final Class<?> f = first.getReturnType(), s = third == null ? second.getReturnType() : ClassUtils.getCommonSuperclass(second.getReturnType(), third.getReturnType());
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
    public boolean check(final Event e) {
        final Expression<?> third = this.third;
        return first.check(e, o1 ->
                second.check(e, o2 -> {
                    if (third == null)
                        return relation.is(
                                comp != null ? comp.apply(o1, o2) : Comparators.compare(o1, o2)
                        );
                    return third.check(e, o3 ->
                            relation == Relation.NOT_EQUAL ^
                            (
                                    Relation.GREATER_OR_EQUAL
                                            .is(
                                                    comp != null ? comp.apply(o1, o2) : Comparators.compare(o1, o2)
                                            )
                                    &&
                                    Relation.SMALLER_OR_EQUAL
                                            .is(
                                                    comp != null ? comp.apply(o1, o3) : Comparators.compare(o1, o3)
                                            )
                            )
                    );
                },
                isNegated()
                )
        );
    }

    @Override
    public String toString(final Event e, final boolean debug) {
        String s;
        final Expression<?> third = this.third;
        if (third == null)
            s = first.toString(e, debug) + " is " + (isNegated() ? "not " : "") + relation + " " + second.toString(e, debug);
        else
            s = first.toString(e, debug) + " is " + (isNegated() ? "not " : "") + "between " + second.toString(e, debug) + " and " + third.toString(e, debug);
        if (debug)
            s += " (comparator: " + comp + ")";
        return s;
    }
}
