package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.registration.PatternType;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.registration.TypeManager;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.junit.Test;

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
			"number(?<plural>s)??",
			s -> {
				Number n;
				try {
					n = Long.parseLong(s);
				} catch (NumberFormatException e) {
					try {
						n = Double.parseDouble(s);
					} catch (NumberFormatException e1) {
						return null;
					}
				}
				return n;
			}
		);
		registration.addType(
            String.class,
            "string",
            "string(?<plural>s)??",
            s -> StringUtils.parseConstantString(s)
        );
		registration.register();
		registration.addExpression(
			ExprSquared.class,
			Number.class,
			"the number %number% squared"
		);
		registration.addExpression(
            ExprRandom.class,
            Number.class,
            "random (0\u00a6number|1\u00a6integer) between %number% and %number% [2\u00a6exclusively]"
        );
		registration.addExpression(
		    ExprSubstring.class,
            String.class,
            "substring %string% from %number% to %number%"
        );
		registration.register();
	}

    public static class ExprSquared implements Expression<Number> {
		private Expression<Number> number;

		@Override
		public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
			number = (Expression<Number>) expressions[0];
			return true;
		}

		@Override
		public Number[] getValues() {
			Number n = number.getSingle();
			if (n == null)
				return new Number[0];
			return new Number[]{n.doubleValue() * n.doubleValue()};
		}

		@Override
		public String toString(boolean debug) {
			return number.toString(debug) + " squared";
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
        public Number[] getValues() {
            Number lower = lowerBound.getSingle();
            Number upper = upperBound.getSingle();
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
        public String toString(boolean debug) {
            return "random " + (integer ? "integer" : "number") + " between " + lowerBound.toString(debug) + " and " + upperBound.toString(debug);
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
        public String[] getValues() {
            String str = string.getSingle();
            Number start = startIndex.getSingle();
            Number end = endIndex.getSingle();
            if (str == null || start == null || end == null)
                return new String[0];
            int s = start.intValue();
            int e = end.intValue();
            if (s < 0 && e >= str.length())
                return new String[0];
            return new String[]{str.substring(s, e)};
        }

        @Override
        public String toString(boolean debug) {
            return "substring " + string.toString(debug) + " from " + startIndex.toString(debug) + " to " + endIndex.toString(debug);
        }
    }

    public void assertExpressionEquals(Expression<?> expected, Expression<?> actual) {
        if (expected == actual)
            return;
	    assertArrayEquals(expected.getValues(), actual.getValues());
    }

	@Test
	public void parseExpression() {
	    PatternType<Number> numberType = new PatternType<>(TypeManager.getInstance().getByClass(Number.class), false);
		assertExpressionEquals(
			new Literal<>(Long.class, 2L),
			SyntaxParser.parseExpression("2", numberType)
		);
		assertExpressionEquals(
			new Literal<>(Double.class, 4.0d),
			SyntaxParser.parseExpression("the number 2 squared", numberType)
		);
		int expectedInt = SyntaxParser.parseExpression("random integer between 0 and 10", numberType).getSingle().intValue();
		assertTrue(0 <= expectedInt && expectedInt <= 10);
		double expectedDouble = SyntaxParser.parseExpression("random number between 9.9999 and 10 exclusively", numberType).getSingle().doubleValue();
		assertTrue(9.9999 + Double.MIN_VALUE <= expectedDouble && expectedDouble <= 10 - Double.MIN_VALUE);
		PatternType<String> stringType = new PatternType<>(TypeManager.getInstance().getByClass(String.class), false);
		assertExpressionEquals(
		    new Literal<>(String.class, "Hello"),
            SyntaxParser.parseExpression("substring \"Hello\" from 0 to 5", stringType)
        );
		assertExpressionEquals(
		    new Literal<>(String.class, "Hello, I am \"raw\""),
            SyntaxParser.parseExpression("substring R\"$(Hello, I am \"raw\")$\" from 0 to 17", stringType)
        );
		assertExpressionEquals(
		        new Literal<>(String.class, "I am \"raw\" too !"),
                SyntaxParser.parseExpression("substring 'I am \"raw\" too !' from 0 to 16", stringType)
        );
    }
}