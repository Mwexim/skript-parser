package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.classes.TypeManager;
import io.github.syst3ms.skriptparser.pattern.*;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

public class PatternParserTest extends TestCase {

	static {
		TypeManager.registerType(Number.class, "number", "number(?<plural>s)?");
		TypeManager.registerType(String.class, "string", "string(?<plural>s)?");
	}

	@Test
	public void testParsePattern() throws Exception {
		assertEquals(new TextElement("syntax"), PatternParser.parsePattern("syntax"));
		assertEquals(new OptionalGroup(new TextElement("optional")), PatternParser.parsePattern("[optional]"));
		PatternElement expected = new OptionalGroup(
			new CompoundElement(
				new TextElement("something "),
				new OptionalGroup(new TextElement("optional")),
				new TextElement(" again")
			)
		);
		assertEquals(expected, PatternParser.parsePattern("[something [optional] again]"));
		assertEquals(new ChoiceGroup(new ChoiceElement(new TextElement("single choice"), 0)), PatternParser.parsePattern("(single choice)"));
		assertEquals(new ChoiceGroup(new ChoiceElement(new TextElement("parse mark"), 1)), PatternParser.parsePattern("(1¦parse mark)"));
		expected = new ChoiceGroup(
			new ChoiceElement(new TextElement("first choice"), 0),
			new ChoiceElement(new TextElement("second choice"), 0)
		);
		assertEquals(expected, PatternParser.parsePattern("(first choice|second choice)"));
		expected = new ChoiceGroup(
			new ChoiceElement(new TextElement("first mark"), 0),
			new ChoiceElement(new TextElement("second mark"), 1)
		);
		assertEquals(expected, PatternParser.parsePattern("(first mark|1¦second mark)"));
		expected = new OptionalGroup(
			new CompoundElement(
				new TextElement("lookie, "),
				new ChoiceGroup(
					new ChoiceElement(new TextElement("another"), 0),
					new ChoiceElement(new TextElement("choice"), 1)
				),
				new TextElement(" !")
			)
		);
		assertEquals(expected, PatternParser.parsePattern("[lookie, (another|1¦choice) !]"));
		assertEquals(new RegexGroup(Pattern.compile(".+")), PatternParser.parsePattern("<.+>"));
		assertEquals(
			new ExpressionElement(
				Collections.singletonList(TypeManager.getPatternType("number")),
				false,
				0,
				ExpressionElement.Acceptance.BOTH
			),
			PatternParser.parsePattern("%number%")
		);
		assertEquals(
			new ExpressionElement(
				Arrays.asList(TypeManager.getPatternType("number"), TypeManager.getPatternType("strings")),
				true,
				1,
				ExpressionElement.Acceptance.LITERALS_ONLY
			),
			PatternParser.parsePattern("%-*number/strings@1%")
		);
		assertNull(PatternParser.parsePattern("(unclosed"));
		assertNull(PatternParser.parsePattern("%unfinished type"));
	}
}