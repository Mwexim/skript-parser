package io.github.syst3ms.skriptparser.log;

import io.github.syst3ms.skriptparser.TestRegistration;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class SkriptLoggerTest {
    static {
        TestRegistration.register();
    }

    @Test
    public void skriptLoggerTest() {
        SkriptLogger logger = new SkriptLogger();
        ParserState parserState = new ParserState();
        Optional<? extends Expression<?>> noMatchFound = SyntaxParser.parseExpression("an expression that doesn't match anything", SyntaxParser.OBJECT_PATTERN_TYPE, parserState, logger);
        logger.finalizeLogs();
        assertTrue(noMatchFound.isEmpty() && logger.close().get(0).getMessage().startsWith("No expression"));
        logger = new SkriptLogger();
        Optional<? extends Expression<?>> wrongNumber = SyntaxParser.parseExpression("range from 1 to 3", SyntaxParser.OBJECT_PATTERN_TYPE, parserState, logger);
        logger.finalizeLogs();
        assertTrue(wrongNumber.isEmpty() && logger.close().get(0).getMessage().startsWith("A single"));
        logger = new SkriptLogger();
        Optional<? extends Expression<Boolean>> wrongRange = SyntaxParser.parseBooleanExpression("1 is between \"a\" and \"b\"", SyntaxParser.MAYBE_CONDITIONAL, parserState, logger);
        logger.finalizeLogs();
        assertTrue(wrongRange.isEmpty() && logger.close().get(0).getMessage().startsWith("'1' cannot"));
    }
}
