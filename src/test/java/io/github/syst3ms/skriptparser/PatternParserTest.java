package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.classes.SkriptParser;
import io.github.syst3ms.skriptparser.classes.SkriptRegistration;
import io.github.syst3ms.skriptparser.classes.TypeManager;
import io.github.syst3ms.skriptparser.pattern.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class PatternParserTest {

	static {
		SkriptRegistration reg = new SkriptRegistration("unit-tests");
		reg.addType(Number.class, "number", "number(?<plural>s)?", Double::parseDouble);
		reg.addType(String.class, "string", "string(?<plural>s)?");
		reg.register();
	}

	@Test
	public void testParsePattern() throws Exception {
		PatternParser parser = new PatternParser();
		assertEquals(new TextElement("syntax"), parser.parsePattern("syntax"));
		assertEquals(new OptionalGroup(new TextElement("optional")), parser.parsePattern("[optional]"));
		PatternElement expected = new OptionalGroup(
			new CompoundElement(
				new TextElement("something "),
				new OptionalGroup(new TextElement("optional")),
				new TextElement(" again")
			)
		);
		assertEquals(expected, parser.parsePattern("[something [optional] again]"));
		assertEquals(new ChoiceGroup(new ChoiceElement(new TextElement("single choice"), 0)), parser.parsePattern("(single choice)"));
		assertEquals(new ChoiceGroup(new ChoiceElement(new TextElement("parse mark"), 1)), parser.parsePattern("(1¦parse mark)"));
		expected = new ChoiceGroup(
			new ChoiceElement(new TextElement("first choice"), 0),
			new ChoiceElement(new TextElement("second choice"), 0)
		);
		assertEquals(expected, parser.parsePattern("(first choice|second choice)"));
		expected = new ChoiceGroup(
			new ChoiceElement(new TextElement("first mark"), 0),
			new ChoiceElement(new TextElement("second mark"), 1)
		);
		assertEquals(expected, parser.parsePattern("(first mark|1¦second mark)"));
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
		assertEquals(expected, parser.parsePattern("[lookie, (another|1¦choice) !]"));
		assertEquals(new RegexGroup(Pattern.compile(".+")), parser.parsePattern("<.+>"));
		assertEquals(new ExpressionElement(Collections.singletonList(TypeManager.getInstance()
																					   .getPatternType("number")), false, 0, ExpressionElement.Acceptance.BOTH), parser
			.parsePattern("%number%"));
		assertEquals(new ExpressionElement(Arrays.asList(TypeManager.getInstance()
																		   .getPatternType("number"), TypeManager.getInstance()
																												 .getPatternType("strings")), true, 1, ExpressionElement.Acceptance.LITERALS_ONLY), parser
			.parsePattern("%-*number/strings@1%"));
		assertNull(parser.parsePattern("(unclosed"));
		assertNull(parser.parsePattern("%unfinished type"));
	}

	@Test
	public void testMatch() throws Exception {
		PatternParser patternParser = new PatternParser();
		SkriptParser parser = new SkriptParser();
		PatternElement pattern = patternParser.parsePattern("pattern");
		assertEquals(7, pattern.match("pattern", 0, parser));
		pattern = patternParser.parsePattern("pattern [with optional]");
		assertEquals(7, pattern.match("pattern", 0, parser));
		assertEquals(21, pattern.match("pattern with optional", 0, parser));
	}
}