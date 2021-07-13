package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.TestRegistration;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static io.github.syst3ms.skriptparser.lang.TriggerContext.DUMMY;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class VariablesTest {
	static {
		TestRegistration.register();
	}

	public void assertExpressionEquals(Expression<?> expected, Optional<? extends Expression<?>> actual) {
		if (actual.filter(expected::equals).isPresent())
			return;
		if (actual.isEmpty())
			fail();
		assertArrayEquals(expected.getValues(DUMMY), actual.get().getValues(DUMMY));
	}

	private void assertExpressionTypeEquals(Class<?> expected, Optional<? extends Expression<?>> expr) {
		if (expr.isEmpty())
			fail("Null expression");
		if (expr.get().getReturnType() == expected)
			return;
		Optional<?> value = expr.get().getSingle(DUMMY);
		if (value.isEmpty() || value.get().getClass() != expected)
			fail("Different return types : expected " + expected + ", got " + value.map(Object::getClass).orElse(null));
	}

	private void run(Optional<? extends Effect> effect) {
		effect.ifPresent(eff -> Statement.runAll(eff, DUMMY));
	}

	@Test
	public void testVariables() {
		SkriptLogger logger = new SkriptLogger();
		ParserState parserState = new ParserState();
		run(SyntaxParser.parseEffect("set {variable} to \"test\"", parserState, logger));
		assertExpressionEquals(
				new SimpleLiteral<>(String.class, "test"),
				SyntaxParser.parseExpression("{variable}", new PatternType<>(TypeManager.getByClassExact(String.class).orElseThrow(AssertionError::new), true), parserState, logger)
		);
		run(SyntaxParser.parseEffect("delete {variable}", parserState, logger));
		assertExpressionEquals(
				new SimpleLiteral<>(Object.class),
				SyntaxParser.parseExpression("{variable}", SyntaxParser.OBJECT_PATTERN_TYPE, parserState, logger)
		);
		// Numbers
		PatternType<Number> numberType = new PatternType<>(TypeManager.getByClassExact(Number.class).orElseThrow(AssertionError::new), true);
		run(SyntaxParser.parseEffect("set {number} to 5", parserState, logger));
		assertExpressionTypeEquals(
				BigInteger.class,
				SyntaxParser.parseExpression("{number}", numberType, parserState, logger)
		);
		run(SyntaxParser.parseEffect("set {number} to 5.2", parserState, logger));
		assertExpressionTypeEquals(
				BigDecimal.class,
				SyntaxParser.parseExpression("{number}", numberType, parserState, logger)
		);
	}
}