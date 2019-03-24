package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.PatternParser;
import io.github.syst3ms.skriptparser.TestRegistration;
import io.github.syst3ms.skriptparser.pattern.*;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PatternParserTest {

    static {
        TestRegistration.register();
    }

    @Test
    public void testParsePattern() {
        PatternParser parser = new PatternParser();
        assertEquals(new TextElement("syntax"), parser.parsePattern("syntax"));
        assertEquals(new OptionalGroup(new TextElement("optional")), parser.parsePattern("[optional]"));
        assertEquals(
                new OptionalGroup(
                    new CompoundElement(
                            new TextElement("nested "),
                            new OptionalGroup(new TextElement("optional"))
                    )
                ),
                parser.parsePattern("[nested [optional]]")
        );
        assertEquals(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("single choice"), 0)
                ),
                parser.parsePattern("(single choice)")
        );
        assertEquals(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("parse mark"), 1)
                ),
                parser.parsePattern("(1\u00a6parse mark)")
        );
        assertEquals(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("first choice"), 0),
                        new ChoiceElement(new TextElement("second choice"), 0)
                ),
                parser.parsePattern("(first choice|second choice)")
        );
        assertEquals(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("first mark"), 0),
                        new ChoiceElement(new TextElement("second mark"), 1)
                ),
                parser.parsePattern("(first mark|1\u00a6second mark)")
        );
        assertEquals(
                new OptionalGroup(
                        new CompoundElement(
                                new TextElement("optional "),
                                new ChoiceGroup(
                                        new ChoiceElement(new TextElement("first choice"), 0),
                                        new ChoiceElement(new TextElement("second choice"), 1)
                                )
                        )
                ),
                parser.parsePattern("[optional (first choice|1\u00a6second choice)]")
        );
        assertEquals(
                new RegexGroup(Pattern.compile(".+")),
                parser.parsePattern("<.+>")
        );
        assertEquals(
                new ExpressionElement(
                        Collections.singletonList(TypeManager.getPatternType("number")),
                        ExpressionElement.Acceptance.ALL,
                        false,
                        false
                ),
                parser.parsePattern("%number%")
        );
        assertEquals(
                new ExpressionElement(
                        Arrays.asList(
                                TypeManager.getPatternType("number"),
                                TypeManager.getPatternType("strings")
                        ),
                        ExpressionElement.Acceptance.LITERALS_ONLY,
                        true,
                        false
                ),
                parser.parsePattern("%*number/strings%")
        );
        assertNull(parser.parsePattern("(unclosed"));
        assertNull(parser.parsePattern("%unfinished type"));
    }

}