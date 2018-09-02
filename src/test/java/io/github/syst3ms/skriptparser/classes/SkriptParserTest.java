package io.github.syst3ms.skriptparser.classes;

import io.github.syst3ms.skriptparser.PatternParser;
import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.SkriptParser;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.pattern.TextElement;
import org.junit.*;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class SkriptParserTest {
    @Test
    public void getPossibleInputs() throws Exception {
        PatternParser patternParser = new PatternParser();
        PatternElement e = patternParser.parsePattern("text [and optional]");
        SkriptParser parser = new SkriptParser(e, new Class[0]);
        assertEquals(Collections.singletonList(new TextElement("text ")), parser.getPossibleInputs(parser.flatten(e)));
        e = patternParser.parsePattern("[optional] and text");
        assertEquals(
                Arrays.asList(new TextElement("optional"), new TextElement(" and text")),
                parser.getPossibleInputs(parser.flatten(e))
        );
        e = patternParser.parsePattern("[(this|that)] [(may be|is)] good");
        assertEquals(
                Arrays.asList(new TextElement("this"), new TextElement("that"), new TextElement("may be"), new TextElement("is"), new TextElement(" good")),
                parser.getPossibleInputs(parser.flatten(e))
        );
    }

}