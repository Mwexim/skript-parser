package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.junit.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class VariablesTest {

    static {
        TestRegistration.register();
    }

    public void assertExpressionEquals(Expression<?> expected, Expression<?> actual) {
        if (expected == actual)
            return;
        if (expected == null || actual == null)
            fail();
        assertArrayEquals(expected.getValues(null), actual.getValues(null));
    }

    @Test
    public void testVariables() throws Exception {
        SyntaxParser.parseEffect("set {variable} to \"test\"").execute(null);
        assertExpressionEquals(
                new SimpleLiteral<>(String.class, "test"),
                SyntaxParser.parseExpression("{variable}", new PatternType<>(TypeManager.getByClassExact(String.class), true))
        );
        SyntaxParser.parseEffect("delete {variable}").execute(null);
        assertExpressionEquals(
                new SimpleLiteral<>(Object.class),
                SyntaxParser.parseExpression("{variable}", SyntaxParser.OBJECT_PATTERN_TYPE)
        );
        // Numbers
        PatternType<Number> numberType = new PatternType<>(TypeManager.getByClassExact(Number.class), true);
        SyntaxParser.parseEffect("set {number} to 5").execute(null);
        assertEquals(
                BigInteger.class,
                SyntaxParser.parseExpression("{number}", numberType)
                            .getSingle(null)
                            .getClass()
        );
        SyntaxParser.parseEffect("set {number} to 5.2").execute(null);
        assertEquals(
                BigDecimal.class,
                SyntaxParser.parseExpression("{number}", numberType)
                            .getSingle(null)
                            .getClass()
        );
        assertExpressionEquals(
                new SimpleLiteral<>(BigDecimal.class, new BigDecimal("10.2")),
                SyntaxParser.parseExpression("5 + {number}", numberType)
        );
    }
}
