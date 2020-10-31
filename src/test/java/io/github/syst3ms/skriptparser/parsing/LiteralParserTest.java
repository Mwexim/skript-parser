package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.TestRegistration;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Literal;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static io.github.syst3ms.skriptparser.lang.TriggerContext.DUMMY;
import static io.github.syst3ms.skriptparser.parsing.SyntaxParser.parseLiteral;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

@SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType"})
public class LiteralParserTest {
    static {
        TestRegistration.register();
    }

    private void assertExpressionEquals(Expression<?> expected, Optional< ?extends Expression<?>> actual) {
        if (actual.filter(expected::equals).isPresent())
            return;
        if (actual.isEmpty())
            fail("Null expression");
        assertArrayEquals(expected.getValues(DUMMY), actual.get().getValues(DUMMY));
    }


    private void assertExpressionTrue(Optional<? extends Expression<?>> actual) {
        assertExpressionEquals(new SimpleLiteral<>(Boolean.class, true), actual);
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

    private <T> PatternType<T> getType(Class<T> c) {
        return new PatternType<>(TypeManager.getByClassExact(c).orElseThrow(AssertionError::new), true);
    }

    private <T> Literal<T> literal(T... values) {
        return new SimpleLiteral<>(values);
    }

    @Test
    public void literalTest() throws Exception {
        SkriptLogger logger = new SkriptLogger();
        ParserState parserState = new ParserState();
        PatternType<Number> numberType = getType(Number.class);
        assertExpressionTypeEquals(
            BigInteger.class,
            parseLiteral("1", numberType, parserState, logger)
        );
        assertExpressionTypeEquals(
            BigDecimal.class,
            parseLiteral("1.0", numberType, parserState, logger)
        );
        PatternType<String> stringType = getType(String.class);
        assertExpressionEquals(
            new SimpleLiteral<>(String.class, "\\\" ()\"\"'"),
            parseLiteral("R\"µ(\\\" ()\"\"')µ\"", stringType, parserState, logger)
        );
    }

    /*
    @Test
    public void sectionTest() throws Exception {
        FileParser fileParser = new FileParser();
        ClassLoader classLoader = getClass().getClassLoader();
        // While test
        File file = new File(classLoader.getResource("while-test.txt").getJarFile());
        List<String> lines = FileUtils.readAllLines(file);
        List<FileElement> elements = fileParser.parseFileLines("while-test", lines, 0, 1);
        FileSection sec = (FileSection) elements.get(0);
        CodeSection whileLoop = parseSection(sec);
        assertTrue("The while loop failed", Statement.runAll(whileLoop, DUMMY));
        // Loop test
        file = new File(classLoader.getResource("general/loop-test.txt").getJarFile());
        lines = FileUtils.readAllLines(file);
        elements = fileParser.parseFileLines("loop-test", lines, 0, 1);
        sec = (FileSection) elements.get(0);
        CodeSection loop = parseSection(sec);
        assertTrue("The loop failed while running", Statement.runAll(loop, DUMMY));
        // Condition test
        file = new File(classLoader.getResource("conditions.txt").getJarFile());
        lines = FileUtils.readAllLines(file);
        elements = fileParser.parseFileLines("conditions", lines, 0, 1);
        sec = (FileSection) elements.get(0);
        Statement first = ScriptLoader.loadItems(sec).get(0);
        assertTrue(Statement.runAll(first, DUMMY));
    }
    */
}