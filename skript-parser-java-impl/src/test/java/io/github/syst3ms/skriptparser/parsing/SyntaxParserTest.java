package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.event.Event;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class SyntaxParserTest {

    static {
        SkriptRegistration registration = new SkriptRegistration("unit-tests");
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
        registration.register();
        registration.addExpression(
                ExprSquared.class,
                Number.class,
                true,
                "the number %number% squared"
        );
        registration.addExpression(
                ExprRandom.class,
                Number.class,
                true,
                "random (0¦number|1¦integer) between %number% and %number% [2¦exclusively]"
        );
        registration.addExpression(
                ExprSubstring.class,
                String.class,
                true,
                "substring %string% from %number% to %number%"
        );
        registration.register();
    }

    public void assertExpressionEquals(Expression<?> expected, Expression<?> actual) {
        if (expected == actual) return;
        assertArrayEquals(expected.getValues(null), actual.getValues(null));
    }

    @Test
    public void parseExpression() {
        PatternType<Number> numberType = new PatternType<>(TypeManager.getByClassExact(Number.class), false);
        assertExpressionEquals(new SimpleLiteral<>(Long.class, 2L), SyntaxParser.parseExpression("2", numberType));
        assertExpressionEquals(new SimpleLiteral<>(Double.class, 4.0d),
                SyntaxParser.parseExpression("the number 2 squared", numberType)
        );
        int expectedInt = SyntaxParser.parseExpression("random integer between 0 and 10", numberType)
                                      .getSingle(null)
                                      .intValue();
        assertTrue(0 <= expectedInt && expectedInt <= 10);
        double expectedDouble = SyntaxParser.parseExpression("random number between 9.9999 and 10 exclusively", numberType)
                                            .getSingle(null)
                                            .doubleValue();
        assertTrue(9.9999 + Double.MIN_VALUE <= expectedDouble && expectedDouble <= 10 - Double.MIN_VALUE);
        PatternType<String> stringType = new PatternType<>(TypeManager.getByClassExact(String.class), false);
        assertExpressionEquals(
                new SimpleLiteral<>(String.class, "Hello"),
                SyntaxParser.parseExpression("substring \"Hello\" from 0 to 5", stringType)
        );
        assertExpressionEquals(
                new SimpleLiteral<>(String.class, "Hello, I am \"raw\""),
                SyntaxParser.parseExpression("substring R\"$(Hello, I am \"raw\")$\" from 0 to 17", stringType)
        );
        assertExpressionEquals(
                new SimpleLiteral<>(String.class, "I am \"raw\" too !"),
                SyntaxParser.parseExpression("substring 'I am \"raw\" too !' from 0 to 16", stringType)
        );
        assertExpressionEquals(
            new SimpleLiteral<>(Number.class, 1L, 2L, 3L),
            SyntaxParser.parseExpression("1, 2 and 3", new PatternType<>(TypeManager.getByClass(Number.class), false))
        );
    }

    public static class ExprSquared implements Expression<Number> {
        private Expression<Number> number;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
            number = (Expression<Number>) expressions[0];
            return true;
        }

        @Override
        public Number[] getValues(Event e) {
            Number n = number.getSingle(e);
            if (n == null)
                return new Number[0];
            return new Number[]{n.doubleValue() * n.doubleValue()};
        }

        @Override
        public String toString(Event e, boolean debug) {
            return number.toString(e, debug) + " squared";
        }
    }

    public static class ExprRandom implements Expression<Number> {
        private static final Random rnd = new Random();
        private Expression<Number> lowerBound, upperBound;
        private boolean integer, exclusive;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
            lowerBound = (Expression<Number>) expressions[0];
            upperBound = (Expression<Number>) expressions[1];
            integer = (parseResult.getParseMark() & 1) == 1;
            exclusive = (parseResult.getParseMark() & 2) == 2;
            return true;
        }

        @Override
        public Number[] getValues(Event e) {
            Number lower = lowerBound.getSingle(e);
            Number upper = upperBound.getSingle(e);
            if (lower == null || upper == null)
                return new Number[0];
            if (integer) {
                int l = lower.intValue();
                int u = upper.intValue();
                if (l > u) { // Just swap the variables
                    int temp = l;
                    l = u;
                    u = temp;
                }
                if (exclusive) {
                    l++;
                    u--;
                }
                return new Number[]{l + rnd.nextInt(u - l)};
            } else {
                double l = lower.doubleValue();
                double u = upper.doubleValue();
                if (l > u) { // Just swap the variables
                    double temp = l;
                    l = u;
                    u = temp;
                }
                double r = l + rnd.nextDouble() * (u - l);
                while (r == l || r == u)
                    r = l + rnd.nextDouble() * (u - l);
                return new Number[]{r};
            }
        }

        @Override
        public String toString(Event e, boolean debug) {
            return "random " +
                   (integer ? "integer" : "number") +
                   " between " +
                   lowerBound.toString(e, debug) +
                   " and " +
                   upperBound.toString(e, debug);
        }
    }

    public static class ExprSubstring implements Expression<String> {
        private Expression<String> string;
        private Expression<Number> startIndex, endIndex;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
            string = (Expression<String>) expressions[0];
            startIndex = (Expression<Number>) expressions[1];
            endIndex = (Expression<Number>) expressions[2];
            return true;
        }

        @Override
        public String[] getValues(Event event) {
            String str = string.getSingle(event);
            Number start = startIndex.getSingle(event);
            Number end = endIndex.getSingle(event);
            if (str == null || start == null || end == null)
                return new String[0];
            int s = start.intValue();
            int e = end.intValue();
            if (s < 0 && e >= str.length())
                return new String[0];
            return new String[]{str.substring(s, e)};
        }

        @Override
        public String toString(Event e, boolean debug) {
            return "substring " +
                   string.toString(e, debug) +
                   " from " +
                   startIndex.toString(e, debug) +
                   " to " +
                   endIndex.toString(e, debug);
        }
    }
}