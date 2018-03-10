package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.SkriptLogger;
import io.github.syst3ms.skriptparser.file.FileElement;
import io.github.syst3ms.skriptparser.file.FileParser;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.util.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.*;

import java.io.File;
import java.util.List;

import static io.github.syst3ms.skriptparser.parsing.TestRegistration.DUMMY;
import static org.junit.Assert.*;

@SuppressWarnings({"unchecked", "ConstantConditions"})
public class SyntaxParserTest {

    static {
        TestRegistration.register();
    }

    private void assertExpressionEquals(@Nullable Expression<?> expected, @Nullable Expression<?> actual) {
        if (expected == actual)
            return;
        if (expected == null || actual == null)
            fail();
        assertArrayEquals(expected.getValues(DUMMY), actual.getValues(DUMMY));
    }


    private void assertExpressionTrue(@Nullable Expression<?> actual) {
        assertExpressionEquals(new SimpleLiteral<>(Boolean.class, true), actual);
    }

    @Test
    public void parseExpression() {
        PatternType<Number> numberType = new PatternType<>(TypeManager.getByClassExact(Number.class), true);
        assertExpressionEquals(new SimpleLiteral<>(Long.class, 2L), SyntaxParser.parseExpression("2L", numberType));
        int expectedInt = SyntaxParser.parseExpression("random integer between 0 and 10", numberType)
                                      .getSingle(DUMMY)
                                      .intValue();
        assertTrue(0 <= expectedInt && expectedInt <= 10);
        double expectedDouble = SyntaxParser.parseExpression("random number between 9.9999 and 10 exclusively", numberType)
                                            .getSingle(DUMMY)
                                            .doubleValue();
        assertTrue(9.9999 + Double.MIN_VALUE <= expectedDouble && expectedDouble <= 10 - Double.MIN_VALUE);
        PatternType<String> stringType = new PatternType<>(TypeManager.getByClassExact(String.class), true);
        assertExpressionEquals(
                new SimpleLiteral<>(String.class, "Hello"),
                SyntaxParser.parseExpression("substring \"Hello\" from 0 to 5", stringType)
        );
        assertExpressionEquals(
                new SimpleLiteral<>(String.class, "Hello, I am \"raw\""),
                SyntaxParser.parseExpression("substring R\"$(Hello, I am \"raw\")$\" from 0 to 17", stringType)
        );
        assertExpressionEquals(
                new SimpleLiteral<>(String.class, "I am \"raw\" too !"),
                SyntaxParser.parseExpression("substring 'I am \"raw\" too !' from 0 to 16", stringType)
        );
        assertExpressionEquals(
            new SimpleLiteral<>(Number.class, 1L, 2L, 3L),
            SyntaxParser.parseExpression("1L, 2L and 3L", new PatternType<>(TypeManager.getByClass(Number.class), false))
        );
        assertExpressionTrue(
                SyntaxParser.parseBooleanExpression("whether 5 is greater than 0", false)
        );
        assertExpressionTrue(
                SyntaxParser.parseBooleanExpression("whether 5 is between 1 and 10", false)
        );
        assertExpressionTrue(
                SyntaxParser.parseExpression("1", SyntaxParser.BOOLEAN_PATTERN_TYPE)
        );
        assertExpressionTrue(
                SyntaxParser.parseBooleanExpression("whether 2 != 5", false)
        );
        assertExpressionTrue(
                SyntaxParser.parseBooleanExpression("  \r   -3   iS    \t   eQuAl TO\t\t\t\t  -3     ", true)
        );
        // These tests try to push the parser to its limits more than anything else
        /*
        assertExpressionEquals(
                new SimpleLiteral<>(Boolean.class, true, false, true),
                SyntaxParser.parseExpression("whether 2 <= 4, (whether 5 is greater than or equal to 6) and true", new PatternType<>(TypeManager.getByClass(Boolean.class), false))
        );
        assertExpressionTrue(
                SyntaxParser.parseBooleanExpression("whether whether 2 <= 4, (whether 10 is greater than or equal to 6) and true are true", false)
        );
        */
        assertNull(SyntaxParser.parseExpression("2 + \"test\"", numberType));
        SkriptLogger.printError();
        assertNull(SyntaxParser.parseEffect("set \"test\" to 2"));
        SkriptLogger.printError();
    }

    @Test
    public void parseSection() throws Exception {
        FileParser fileParser = new FileParser();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("section-test.txt").getFile());
        List<String> lines = FileUtils.readAllLines(file);
        List<FileElement> elements = fileParser.parseFileLines("section-test", lines, 0, 1);
        FileSection sec = (FileSection) elements.get(0);
        SyntaxParser.parseSection(sec);
    }
}