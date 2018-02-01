package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.registration.PatternType;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.registration.TypeManager;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.*;

public class SyntaxParserTest {

	static {
		SkriptRegistration registration = new SkriptRegistration("unit-tests");
		registration.addType(
			Number.class,
			"number",
			"number(?<plural>s)?",
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
		registration.register();
		registration.addExpression(
			ExprSquared.class,
			Number.class,
			"%number% squared"
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
			Number[] n = number.getValues();
			if (n.length == 0)
				return new Number[0];
			return new Number[]{n[0].doubleValue() * n[0].doubleValue()};
		}

		@Override
		public String toString(boolean debug) {
			return number.toString(debug) + " squared";
		}
	}

	@Test
	public void parseExpression() {
		assertArrayEquals(
			Expression.fromLambda(() -> (long) 2).getValues(),
			SyntaxParser.parseExpression("2", new PatternType<>(TypeManager.getInstance().getByClass(Number.class), false)).getValues()
		);
		assertArrayEquals(
			Expression.fromLambda(() -> (long) 4).getValues(),
			SyntaxParser.parseExpression("2 squared", new PatternType<>(TypeManager.getInstance().getByClass(Number.class), false)).getValues()
		);
	}
}