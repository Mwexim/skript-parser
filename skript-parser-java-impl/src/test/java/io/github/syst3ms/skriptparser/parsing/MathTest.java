package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.junit.*;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static io.github.syst3ms.skriptparser.event.Event.DUMMY;
import static org.junit.Assert.*;

public class MathTest {
    public static final double DOUBLE_EPSILON = 1e-10;
    public static final BigDecimal BIG_DECIMAL_EPSILON = new BigDecimal("1e-20");

    static {
        TestRegistration.register();
    }

    private void assertNumberEquals(Number expected, @Nullable Expression<? extends Number> actual) {
        if (actual == null)
            fail("Null expression");
        Number value = actual.getSingle(DUMMY);
        if (value == null)
            fail("Null value");
        if (value instanceof Double) {
            assertEquals(expected.doubleValue(), value.doubleValue(), DOUBLE_EPSILON);
        } else if (value instanceof BigDecimal) {
            BigDecimal diff = ((BigDecimal) value).subtract((BigDecimal) expected).abs();
            if (diff.compareTo(BIG_DECIMAL_EPSILON) > 0)
                fail("Difference was " + diff);
        } else {
            assertEquals(expected, value);
        }
    }

    @Test
    public void mathTest() {
        PatternType<Number> numberType = new PatternType<>(TypeManager.getByClassExact(Number.class), true);
        assertNumberEquals(
                BigDecimal.ONE.add(BigDecimalFunctions.sqrt(new BigDecimal("5"), 64)).divide(new BigDecimal("2"), RoundingMode.HALF_UP),
                SyntaxParser.parseExpression("phi", numberType)
        );
    }
}
