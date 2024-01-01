package io.github.syst3ms.skriptparser.parsing;

import io.github.syst3ms.skriptparser.TestRegistration;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.pattern.ChoiceElement;
import io.github.syst3ms.skriptparser.pattern.ChoiceGroup;
import io.github.syst3ms.skriptparser.pattern.CompoundElement;
import io.github.syst3ms.skriptparser.pattern.ExpressionElement;
import io.github.syst3ms.skriptparser.pattern.OptionalGroup;
import io.github.syst3ms.skriptparser.pattern.RegexGroup;
import io.github.syst3ms.skriptparser.pattern.TextElement;
import io.github.syst3ms.skriptparser.types.TypeManager;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.github.syst3ms.skriptparser.pattern.PatternParser.parsePattern;
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

        // Simple patterns with one element
        assertEqualsOptional(new TextElement("syntax"), parsePattern("syntax", logger));
        assertEqualsOptional(new OptionalGroup(new TextElement("optional")), parsePattern("[optional]", logger));

        // Nested optionals
        assertEqualsOptional(
                new OptionalGroup(
                    new CompoundElement(
                            new TextElement("nested "),
                            new OptionalGroup(new TextElement("optional"))
                    )
                ),
                parsePattern("[nested [optional]]", logger)
        );

        // Choice groups with parse marks
        assertEqualsOptional(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("single choice"), null)
                ),
                parsePattern("(single choice)", logger)
        );
        assertEqualsOptional(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("parse mark"), "1")
                ),
                parsePattern("(1:parse mark)", logger)
        );
        assertEqualsOptional(
                new ChoiceGroup(
                        new ChoiceElement(
                                new CompoundElement(
                                        new TextElement("simplified "),
                                        new OptionalGroup(
                                                new TextElement("mark")
                                        )
                                ),
                                "simplified"
                        )
                ),
                parsePattern("(:simplified [mark])", logger)
        );
        assertEqualsOptional(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("first choice"), null),
                        new ChoiceElement(new TextElement("second choice"), null)
                ),
                parsePattern("(first choice|second choice)", logger)
        );
        assertEqualsOptional(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("first mark"), null),
                        new ChoiceElement(new TextElement("second mark"), "1")
                ),
                parsePattern("(first mark|1:second mark)", logger)
        );
        assertEqualsOptional(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("first choice "), "first choice"),
                        new ChoiceElement(new TextElement("second choice"), "second choice")
                ),
                parsePattern(":(first choice |second choice)", logger)
        );
        assertEqualsOptional(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("first mark"), null),
                        new ChoiceElement(new TextElement("second mark"), "second mark")
                ),
                parsePattern("(first mark|:second mark)", logger)
        );
        assertEqualsOptional(
                new ChoiceGroup(
                        new ChoiceElement(new TextElement("first mark"), null),
                        new ChoiceElement(new TextElement("second mark"), "second mark"),
                        new ChoiceElement(new TextElement("third custom mark"), "custom"),
                        new ChoiceElement(new TextElement("fourth mark"), null)
                ),
                parsePattern("(first mark|:second mark|custom:third custom mark|fourth mark)", logger)
        );

        // Optional choice group (syntax sugar)
        assertEqualsOptional(
                new OptionalGroup(
                        new ChoiceGroup(
                                new ChoiceElement(new TextElement("one"), "one"),
                                new ChoiceElement(new TextElement(" two"), "two"),
                                new ChoiceElement(new TextElement("three"), "four")
                        )
                ),
                parsePattern(":[one| two|four:three]", logger)
        );

        // Optional with nested group with parse marks
        assertEqualsOptional(
                new OptionalGroup(
                        new ChoiceGroup(
                                new ChoiceElement(
                                        new CompoundElement(
                                                new TextElement("optional "),
                                                new ChoiceGroup(
                                                        new ChoiceElement(new TextElement("first choice"), null),
                                                        new ChoiceElement(new TextElement("second choice"), "second")
                                                )
                                        ),
                                        "optional"
                                )
                        )
                ),
                parsePattern("[:optional (first choice|second:second choice)]", logger)
        );

        // Regex group
        assertEqualsOptional(
                new RegexGroup(Pattern.compile(".+")),
                parsePattern("<.+>", logger)
        );
        assertEqualsOptional(
                new RegexGroup(Pattern.compile("(\\d*1)st|(\\d*2)nd|(\\d*3)rd|(\\d*[4-90])th")),
                parsePattern("<(\\d*1)st|(\\d*2)nd|(\\d*3)rd|(\\d*[4-90])th>", logger)
        );

        // Expression elements
        assertEqualsOptional(
                new ExpressionElement(
                        Collections.singletonList(TypeManager.getPatternType("number").orElseThrow(AssertionError::new)),
                        ExpressionElement.Acceptance.ALL,
                        false,
                        false
                ),
                parsePattern("%number%", logger)
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
                parsePattern("%*number/strings%", logger)
        );

        // Failing patterns
        assertOptionalEmpty(parsePattern("(unclosed", logger));
        assertOptionalEmpty(parsePattern("%unfinished type", logger));
    }

}