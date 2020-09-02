package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.TestRegistration;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.pattern.*;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PatternParserTest {

    static {
        TestRegistration.register();
    }
    
    private <T> void assertEqualsOptional(T expected, Optional<? extends T> actual) {
        assertTrue(actual.filter(expected::equals).isPresent());
    }

    private void assertOptionalEmpty(Optional<?> optional) {
        assertTrue(optional.isEmpty());
    }

    @Test
    public void testParsePattern() {
        SkriptLogger logger = new SkriptLogger();
        PatternParser parser = new PatternParser();
        assertEqualsOptional(new TextElement("syntax"), parser.parsePattern("syntax", logger));
        assertEqualsOptional(new OptionalGroup(new TextElement("optional")), parser.parsePattern("[optional]", logger));
        assertEqualsOptional(
                new OptionalGroup(
                    new CompoundElement(
                            new TextElement("nested "),
                            new OptionalGroup(new TextElement("optional"))
                    )
                ),
                parser.parsePattern("[nested [optional]]", logger)
        );
        assertEqualsOptional(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("single choice"), 0)
                ),
                parser.parsePattern("(single choice)", logger)
        );
        assertEqualsOptional(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("parse mark"), 1)
                ),
                parser.parsePattern("(1:parse mark)", logger)
        );
        assertEqualsOptional(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("first choice"), 0),
                        new ChoiceElement(new TextElement("second choice"), 0)
                ),
                parser.parsePattern("(first choice|second choice)", logger)
        );
        assertEqualsOptional(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("first mark"), 0),
                        new ChoiceElement(new TextElement("second mark"), 1)
                ),
                parser.parsePattern("(first mark|1:second mark)", logger)
        );
        assertEqualsOptional(
                new OptionalGroup(
                        new CompoundElement(
                                new TextElement("optional "),
                                new ChoiceGroup(
                                        new ChoiceElement(new TextElement("first choice"), 0),
                                        new ChoiceElement(new TextElement("second choice"), 1)
                                )
                        )
                ),
                parser.parsePattern("[optional (first choice|1:second choice)]", logger)
        );
        assertEqualsOptional(
                new RegexGroup(Pattern.compile(".+")),
                parser.parsePattern("<.+>", logger)
        );
        assertEqualsOptional(
                new ExpressionElement(
                        Collections.singletonList(TypeManager.getPatternType("number").orElseThrow(AssertionError::new)),
                        ExpressionElement.Acceptance.ALL,
                        false,
                        false
                ),
                parser.parsePattern("%number%", logger)
        );
        assertEqualsOptional(
                new ExpressionElement(
                        Arrays.asList(
                                TypeManager.getPatternType("number").orElseThrow(AssertionError::new),
                                TypeManager.getPatternType("strings").orElseThrow(AssertionError::new)
                        ),
                        ExpressionElement.Acceptance.LITERALS_ONLY,
                        true,
                        false
                ),
                parser.parsePattern("%*number/strings%", logger)
        );
        assertOptionalEmpty(parser.parsePattern("(unclosed", logger));
        assertOptionalEmpty(parser.parsePattern("%unfinished type", logger));
    }

}