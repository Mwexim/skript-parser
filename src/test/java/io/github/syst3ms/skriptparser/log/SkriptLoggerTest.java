package io.github.syst3ms.skriptparser.log;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.parsing.TestRegistration;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import org.junit.*;

import static org.junit.Assert.*;

public class SkriptLoggerTest {

    static {
        TestRegistration.register();
    }

    @Test
    public void skriptLoggerTest() throws Exception {
        SkriptLogger logger = new SkriptLogger();
        Expression<?> noMatchFound = SyntaxParser.parseExpression("an expression that doesn't match anything", SyntaxParser.OBJECT_PATTERN_TYPE, logger);
        logger.logOutput();
        assertTrue(noMatchFound == null && logger.close().get(0).getMessage().startsWith("No expression"));
        logger = new SkriptLogger();
        Expression<?> wrongNumber = SyntaxParser.parseExpression("range from 1 to 3", SyntaxParser.OBJECT_PATTERN_TYPE, logger);
        logger.logOutput();
        assertTrue(wrongNumber == null && logger.close().get(0).getMessage().startsWith("A single"));
    }
}
