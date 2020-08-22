package io.github.syst3ms.skriptparser.log;

import io.github.syst3ms.skriptparser.TestRegistration;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SkriptLoggerTest {

    static {
        TestRegistration.register();
    }

    @Test
    public void skriptLoggerTest() throws Exception {
        SkriptLogger logger = new SkriptLogger();
        ParserState parserState = new ParserState();
        Expression<?> noMatchFound = SyntaxParser.parseExpression("an expression that doesn't match anything", SyntaxParser.OBJECT_PATTERN_TYPE, parserState, logger);
        logger.logOutput();
        assertTrue(noMatchFound == null && logger.close().get(0).getMessage().startsWith("No expression"));
        logger = new SkriptLogger();
        Expression<?> wrongNumber = SyntaxParser.parseExpression("range from 1 to 3", SyntaxParser.OBJECT_PATTERN_TYPE, parserState, logger);
        logger.logOutput();
        assertTrue(wrongNumber == null && logger.close().get(0).getMessage().startsWith("A single"));
        logger = new SkriptLogger();
        Expression<?> wrongRange = SyntaxParser.parseBooleanExpression("1 is between 'a' and 'b'", SyntaxParser.MAYBE_CONDITIONAL, parserState, logger);
        logger.logOutput();
        assertTrue(wrongRange == null && logger.close().get(0).getMessage().startsWith("'1' cannot"));
    }
}
