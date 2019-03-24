package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.TestRegistration;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

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
    
    private void run(Effect eff) {
        Statement.runAll(eff, DUMMY);
    }

    @Test
    public void testVariables() {
        SkriptLogger logger = new SkriptLogger();
        run(SyntaxParser.parseEffect("set {variable} to \"test\"", logger));
        assertExpressionEquals(
                new SimpleLiteral<>(String.class, "test"),
                SyntaxParser.parseExpression("{variable}", new PatternType<>(TypeManager.getByClassExact(String.class), true),
                        logger)
        );
        run(SyntaxParser.parseEffect("delete {variable}", logger));
        assertExpressionEquals(
                new SimpleLiteral<>(Object.class),
                SyntaxParser.parseExpression("{variable}", SyntaxParser.OBJECT_PATTERN_TYPE, logger)
        );
        // Numbers
        PatternType<Number> numberType = new PatternType<>(TypeManager.getByClassExact(Number.class), true);
        run(SyntaxParser.parseEffect("set {number} to 5", logger));
        assertEquals(
                BigInteger.class,
                SyntaxParser.parseExpression("{number}", numberType, logger)
                            .getSingle(DUMMY)
                            .getClass()
        );
        run(SyntaxParser.parseEffect("set {number} to 5.2", logger));
        assertEquals(
                BigDecimal.class,
                SyntaxParser.parseExpression("{number}", numberType, logger)
                            .getSingle(DUMMY)
                            .getClass()
        );
    }
}
