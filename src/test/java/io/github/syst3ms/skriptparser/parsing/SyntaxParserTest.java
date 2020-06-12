package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.TestRegistration;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.util.CollectionUtils;
import io.github.syst3ms.skriptparser.util.math.BigDecimalMath;
import io.github.syst3ms.skriptparser.util.math.NumberMath;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static io.github.syst3ms.skriptparser.event.TriggerContext.DUMMY;
import static io.github.syst3ms.skriptparser.parsing.SyntaxParser.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

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

    private <T> Literal<T> literal(T... values) {
        return new SimpleLiteral<>(values);
    }

    @Test
    public void literalTest() throws Exception {
        SkriptLogger logger = new SkriptLogger();
        ParserState parserState = new ParserState();
        PatternType<Number> numberType = getType(Number.class, true);
        assertExpressionTypeEquals(
            BigInteger.class,
            parseLiteral("1", numberType, parserState, logger)
        );
        assertExpressionTypeEquals(
            BigDecimal.class,
            parseLiteral("1.0", numberType, parserState, logger)
        );
        PatternType<String> stringType = getType(String.class, true);
        assertExpressionEquals(
            new SimpleLiteral<>(String.class, "\\\" ()\"\"'"),
            parseLiteral("R\"µ(\\\" ()\"\"')µ\"", stringType, parserState, logger)
        );
    }

    @Test
    public void standardExpressionsTest() throws Exception {
        SkriptLogger logger = new SkriptLogger();
        ParserState parserState = new ParserState();
        // CondExprCompare
        assertExpressionTrue(
            parseBooleanExpression("2 > 1", SyntaxParser.MAYBE_CONDITIONAL, parserState, logger)
        );
        assertExpressionTrue(
            parseBooleanExpression("(3^2) - (2^3) = 1", SyntaxParser.MAYBE_CONDITIONAL, parserState, logger)
        );
        assertExpressionTrue(
            parseBooleanExpression("1 is between 0 and 10", SyntaxParser.MAYBE_CONDITIONAL, parserState, logger)
        );
        // ExprBinaryMathFunctions
        PatternType<Number> numberType = getType(Number.class, true);
        assertExpressionEquals(
            literal((BigDecimal) NumberMath.log(new BigDecimal("2.0"), BigDecimal.TEN)),
            parseExpression("log base 2 of 10", numberType, parserState, logger)
        );
        // ExprBooleanOperators
        assertExpressionTrue(
            parseBooleanExpression("not (false and (false or true))", SyntaxParser.NOT_CONDITIONAL, parserState, logger)
        );
        // ExprNumberArithmetic
        assertExpressionEquals(
            literal(new BigDecimal("251")),
            parseExpression("6*(6+6*6)-6/6", numberType, parserState, logger)
        );
        assertExpressionEquals(
            literal(BigInteger.valueOf(3435)),
            parseExpression("3^3+4^4+3^3+5^5", numberType, parserState, logger)
        );
        // ExprRange
        PatternType<Object> objectsType = getType(Object.class, false);
        BigInteger[] oneThroughTen = new BigInteger[10];
        for (int i = 1; i <= 10; i++) {
            oneThroughTen[i - 1] = BigInteger.valueOf(i);
        }
        assertExpressionEquals(
            literal(oneThroughTen),
            parseExpression("range from 1 to 10", objectsType, parserState, logger)
        );
        assertExpressionEquals(
            literal(CollectionUtils.reverseArray(oneThroughTen)),
            parseExpression("range from 10 to 1", objectsType, parserState, logger)
        );
        // ExprUnaryMathFunctions
        assertExpressionEquals(
            literal(new BigDecimal("3628800")),
            parseExpression("(round acos cos 10)!", numberType, parserState, logger)
        );
        assertExpressionEquals(
            literal(new BigDecimal("4.0")),
            parseExpression("sqrt 16", numberType, parserState, logger)
        );
        // ExprWhether is a wrapper, no need to test it
        // LitMathConstants
        assertExpressionEquals(
            literal(BigDecimalMath.e(BigDecimalMath.DEFAULT_CONTEXT)),
            parseExpression("e", numberType, parserState, logger)
        );
    }

    /*
    @Test
    public void sectionTest() throws Exception {
        FileParser fileParser = new FileParser();
        ClassLoader classLoader = getClass().getClassLoader();
        // While test
        File file = new File(classLoader.getResource("while-test.txt").getFile());
        List<String> lines = FileUtils.readAllLines(file);
        List<FileElement> elements = fileParser.parseFileLines("while-test", lines, 0, 1);
        FileSection sec = (FileSection) elements.get(0);
        CodeSection whileLoop = parseSection(sec);
        assertTrue("The while loop failed", Statement.runAll(whileLoop, DUMMY));
        // Loop test
        file = new File(classLoader.getResource("loop-test.txt").getFile());
        lines = FileUtils.readAllLines(file);
        elements = fileParser.parseFileLines("loop-test", lines, 0, 1);
        sec = (FileSection) elements.get(0);
        CodeSection loop = parseSection(sec);
        assertTrue("The loop failed while running", Statement.runAll(loop, DUMMY));
        // Condition test
        file = new File(classLoader.getResource("conditions.txt").getFile());
        lines = FileUtils.readAllLines(file);
        elements = fileParser.parseFileLines("conditions", lines, 0, 1);
        sec = (FileSection) elements.get(0);
        Statement first = ScriptLoader.loadItems(sec).get(0);
        assertTrue(Statement.runAll(first, DUMMY));
    }
    */
}