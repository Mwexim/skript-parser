package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.junit.*;
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
    }
}
