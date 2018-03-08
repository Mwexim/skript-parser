package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.effects.EffChange;
import io.github.syst3ms.skriptparser.expressions.CondExprCompare;
import io.github.syst3ms.skriptparser.expressions.ExprWhether;
import io.github.syst3ms.skriptparser.lang.While;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.comparisons.Comparator;
import io.github.syst3ms.skriptparser.types.comparisons.Comparators;
import io.github.syst3ms.skriptparser.types.comparisons.Relation;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TestRegistration {
    public static void register() {
        SkriptRegistration registration = new SkriptRegistration("unit-tests");
        registration.addType(
                Object.class,
                "object",
                "object¦s"
        );
        registration.addType(
                Number.class,
                "number",
                "number¦s",
                s -> {
                    Number n;
                    try {
                        n = Long.parseLong(s);
                    } catch (NumberFormatException e) {
                        try {
                            n = Double.parseDouble(s);
                        } catch (NumberFormatException e1) {
                            try {
                                n = new BigInteger(s);
                            } catch (NumberFormatException e2) {
                                try {
                                    n = new BigDecimal(s);
                                } catch (NumberFormatException e3) {
                                    return null;
                                }
                            }
                        }
                    }
                    return n;
                },
                o -> {
                    if (o == null)
                        return TypeManager.NULL_REPRESENTATION;
                    if (o instanceof Long) {
                        return o.toString();
                    } else if (o instanceof Double) {
                        return Double.toString(o.doubleValue());
                    } else if (o instanceof BigInteger || o instanceof BigDecimal) {
                        return o.toString(); // Both BigInteger and BigDecimal override toString
                    }
                    assert false;
                    return null; // Can't happen, so we don't really have to worry about that
                }
        );
        registration.addType(
                String.class,
                "string",
                "string¦s"
        );
        registration.addType(
                Boolean.class,
                "boolean",
                "boolean¦s",
                s -> {
                    if (s.equalsIgnoreCase("true")) {
                        return true;
                    } else if (s.equalsIgnoreCase("false")) {
                        return false;
                    } else {
                        return null;
                    }
                }
        );
        registration.register();
        registration.addExpression(
                TestExpressions.ExprSquared.class,
                Number.class,
                true,
                "the number %number% squared"
        );
        registration.addExpression(
                TestExpressions.ExprRandom.class,
                Number.class,
                true,
                "random (0¦number|1¦integer) between %number% and %number% [2¦exclusively]"
        );
        registration.addExpression(
                TestExpressions.ExprSubstring.class,
                String.class,
                true,
                "substring %string% from %number% to %number%"
        );
        registration.addExpression(
                ExprWhether.class,
                Boolean.class,
                true,
                "whether %~=boolean%"
        );
        registration.addExpression(
                CondExprCompare.class,
                Boolean.class,
                true,
                1,
                CondExprCompare.PATTERNS.getPatterns()
        );
        registration.addEffect(
                EffChange.class,
                2,
                EffChange.PATTERNS.getPatterns()
        );
        registration.addSection(
                While.class,
                "while %=boolean%"
        );
        registration.addEffect(
                TestEffects.EffPrintln.class,
                "println %string%"
        );
        Comparators.registerComparator(Number.class, Number.class, new Comparator<Number, Number>() {
            @Override
            public Relation apply(Number number, Number number2) {
                if (number instanceof BigDecimal || number2 instanceof BigDecimal) {
                    if (number instanceof BigDecimal && number2 instanceof BigDecimal)
                        return Relation.get(((BigDecimal) number).compareTo((BigDecimal) number2));
                    else if (number instanceof BigDecimal)
                        return Relation.get(((BigDecimal) number).compareTo(new BigDecimal(number2.toString())));
                    else
                        return Relation.get(-((BigDecimal) number2).compareTo(new BigDecimal(number.toString())));
                } else if (number instanceof Double || number2 instanceof Double) {
                    return Relation.get(number.doubleValue() - number2.doubleValue());
                } else {
                    return Relation.get(number.longValue() - number2.longValue());
                }
            }

            @Override
            public boolean supportsOrdering() {
                return true;
            }
        });
        registration.addConverter(
                Number.class,
                Boolean.class,
                n -> {
                    long l = n.longValue();
                    return l != 0;
                }
        );
        registration.register();
    }
}
