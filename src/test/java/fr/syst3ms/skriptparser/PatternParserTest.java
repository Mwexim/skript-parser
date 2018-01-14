package fr.syst3ms.skriptparser;

import fr.syst3ms.skriptparser.classes.TypeManager;
import fr.syst3ms.skriptparser.pattern.*;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

import static fr.syst3ms.skriptparser.PatternParser.parsePattern;

public class PatternParserTest extends TestCase {

	static {
		TypeManager.registerType(Number.class, "number", "number(?<plural>s)?");
		TypeManager.registerType(String.class, "string", "string(?<plural>s)?");
	}

	@Test
	public void testParsePattern() throws Exception {
		assertEquals(new TextElement("syntax"), parsePattern("syntax"));
		assertEquals(new OptionalGroup(new TextElement("optional")), parsePattern("[optional]"));
		PatternElement expected = new OptionalGroup(
			new CompoundElement(
				new TextElement("something "),
				new OptionalGroup(new TextElement("optional")),
				new TextElement(" again")
			)
		);
		assertEquals(expected, parsePattern("[something [optional] again]"));
		assertEquals(new ChoiceGroup(new ChoiceElement(new TextElement("single choice"), 0)), parsePattern("(single choice)"));
		assertEquals(new ChoiceGroup(new ChoiceElement(new TextElement("parse mark"), 1)), parsePattern("(1¦parse mark)"));
		expected = new ChoiceGroup(
			new ChoiceElement(new TextElement("first choice"), 0),
			new ChoiceElement(new TextElement("second choice"), 0)
		);
		assertEquals(expected, parsePattern("(first choice|second choice)"));
		expected = new ChoiceGroup(
			new ChoiceElement(new TextElement("first mark"), 0),
			new ChoiceElement(new TextElement("second mark"), 1)
		);
		assertEquals(expected, parsePattern("(first mark|1¦second mark)"));
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
		assertEquals(expected, parsePattern("[lookie, (another|1¦choice) !]"));
		assertEquals(new RegexGroup(Pattern.compile(".+")), parsePattern("<.+>"));
		assertEquals(
			new ExpressionElement(
				Collections.singletonList(TypeManager.getPatternType("number")),
				false,
				0,
				ExpressionElement.Acceptance.BOTH
			),
			parsePattern("%number%")
		);
		assertEquals(
			new ExpressionElement(
				Arrays.asList(TypeManager.getPatternType("number"), TypeManager.getPatternType("strings")),
				true,
				1,
				ExpressionElement.Acceptance.LITERALS_ONLY
			),
			parsePattern("%-*number/strings@1%")
		);
		assertNull(parsePattern("(unclosed"));
		assertNull(parsePattern("%unfinished type"));
	}
}