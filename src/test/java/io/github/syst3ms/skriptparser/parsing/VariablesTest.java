package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.jetbrains.annotations.Nullable;
import org.junit.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import static io.github.syst3ms.skriptparser.event.TriggerContext.DUMMY;
import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class VariablesTest {

    static {
        TestRegistration.register();
    }

    public void assertExpressionEquals(@Nullable Expression<?> expected, @Nullable Expression<?> actual) {
        if (expected == actual)
            return;
        if (expected == null || actual == null)
            fail();
        assertArrayEquals(expected.getValues(DUMMY), actual.getValues(DUMMY));
    }

    @Test
    public void testVariables() {
        SyntaxParser.parseEffect("set {variable} to \"test\"").execute(DUMMY);
        assertExpressionEquals(
                new SimpleLiteral<>(String.class, "test"),
                SyntaxParser.parseExpression("{variable}", new PatternType<>(TypeManager.getByClassExact(String.class), true))
        );
        SyntaxParser.parseEffect("delete {variable}").execute(DUMMY);
        assertExpressionEquals(
                new SimpleLiteral<>(Object.class),
                SyntaxParser.parseExpression("{variable}", SyntaxParser.OBJECT_PATTERN_TYPE)
        );
        // Numbers
        PatternType<Number> numberType = new PatternType<>(TypeManager.getByClassExact(Number.class), true);
        SyntaxParser.parseEffect("set {number} to 5").execute(DUMMY);
        assertEquals(
                BigInteger.class,
                SyntaxParser.parseExpression("{number}", numberType)
                            .getSingle(DUMMY)
                            .getClass()
        );
        SyntaxParser.parseEffect("set {number} to 5.2").execute(DUMMY);
        assertEquals(
                BigDecimal.class,
                SyntaxParser.parseExpression("{number}", numberType)
                            .getSingle(DUMMY)
                            .getClass()
        );
    }
}
