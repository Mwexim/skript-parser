package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileParser;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.util.FileUtils;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;
import io.github.syst3ms.skriptparser.util.math.NumberMath;
import org.jetbrains.annotations.Nullable;
import org.junit.*;

import javax.print.attribute.standard.NumberUp;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static io.github.syst3ms.skriptparser.event.Event.DUMMY;
import static io.github.syst3ms.skriptparser.parsing.SyntaxParser.*;
import static org.junit.Assert.*;

@SuppressWarnings({"unchecked", "ConstantConditions"})
public class SyntaxParserTest {

    static {
        TestRegistration.register();
    }

    private void assertExpressionEquals(@Nullable Expression<?> expected, @Nullable Expression<?> actual) {
        if (expected == actual)
            return;
        if (actual == null)
            fail("Null expression");
        assertArrayEquals(expected.getValues(DUMMY), actual.getValues(DUMMY));
    }


    private void assertExpressionTrue(@Nullable Expression<?> actual) {
        assertExpressionEquals(new SimpleLiteral<>(Boolean.class, true), actual);
    }

    private void assertExpressionTypeEquals(Class<?> expected, @Nullable Expression<?> expr) throws Exception {
        if (expr == null)
            fail("Null expression");
        if (expr.getReturnType() == expected)
            return;
        Object value = expr.getSingle(DUMMY);
        if (value == null || value.getClass() != expected)
            fail("Different return types : expected " + expected + ", got " + (value == null ? "null" : value.getClass()));
    }

    private <T> PatternType<T> getType(Class<T> c, boolean single) {
        return new PatternType<>(TypeManager.getByClassExact(c), single);
    }

    @Test
    public void literalTest() throws Exception {
        PatternType<Number> numberType = getType(Number.class, true);
        assertExpressionTypeEquals(
            BigInteger.class,
            parseLiteral("1", numberType)
        );
        assertExpressionTypeEquals(
            BigDecimal.class,
            parseLiteral("1.0", numberType)
        );
        PatternType<String> stringType = getType(String.class, true);
        assertExpressionEquals(
            new SimpleLiteral<>(String.class, "\\\" ()\"\"'"),
            parseLiteral("R\"µ(\\\" ()\"\"')µ\"", stringType)
        );
    }

    @Test
    public void standardExpressionsTest() throws Exception {
        // CondExprCompare
        assertExpressionTrue(
            parseBooleanExpression("2 > 1", true)
        );
        assertExpressionTrue(
            parseBooleanExpression("(3^2) - (2^3) = 1", true)
        );
        assertExpressionTrue(
            parseBooleanExpression("1 is between 0 and 10", true)
        );
        // ExprBinaryMathFunctions
        PatternType<Number> numberType = getType(Number.class, true);
        assertExpressionEquals(
            new SimpleLiteral<>(BigDecimal.class, (BigDecimal) NumberMath.log(new BigDecimal("2.0"), BigDecimal.TEN)),
            parseExpression("log base 2 of 10", numberType)
        );
        // ExprBooleanOperators
        assertExpressionTrue(
            parseBooleanExpression("not (false and (false or true))", false)
        );
        // ExprNumberArithmetic
        assertExpressionEquals(
            new SimpleLiteral<>(Number.class, new BigDecimal("251")),
            parseExpression("6*(6+6*6)-6/6", numberType)
        );
        assertExpressionEquals(
            new SimpleLiteral<>(Number.class, BigInteger.valueOf(3435)),
            parseExpression("3^3+4^4+3^3+5^5", numberType)
        );
        // ExprRange
        PatternType<Object> objectsType = getType(Object.class, false);
        BigInteger[] zeroThroughTen = new BigInteger[10];
        for (int i = 1; i <= 10; i++) {
            zeroThroughTen[i - 1] = BigInteger.valueOf(i);
        }
        assertExpressionEquals(
            new SimpleLiteral<>(Number.class, zeroThroughTen),
            parseExpression("range from 1 to 10", objectsType)
        );
        // ExprUnaryMathFunctions
        assertExpressionEquals(
            new SimpleLiteral<>(Number.class, new BigInteger("3628800")),
            parseExpression("(round acos cos 10)!", numberType)
        );
    }

    @Test
    public void sectionTest() throws Exception {
        FileParser fileParser = new FileParser();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("while-test.txt").getFile());
        List<String> lines = FileUtils.readAllLines(file);
        List<FileElement> elements = fileParser.parseFileLines("while-test", lines, 0, 1);
        FileSection sec = (FileSection) elements.get(0);
        parseSection(sec);
        file = new File(classLoader.getResource("loop-test.txt").getFile());
        lines = FileUtils.readAllLines(file);
        elements = fileParser.parseFileLines("loop-test", lines, 0, 1);
        sec = (FileSection) elements.get(0);
        CodeSection loop = parseSection(sec);
        assertTrue("The loop failed while running", Effect.runAll(loop, DUMMY));
    }
}