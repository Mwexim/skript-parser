package fr.syst3ms.skriptparser;

import fr.syst3ms.skriptparser.pattern.*;
import junit.framework.TestCase;
import org.junit.Test;

import static fr.syst3ms.skriptparser.PatternParser.*;

public class PatternParserTest extends TestCase {
	@Test
	public void testParsePattern() throws Exception {
		/*
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
		*/
		assertEquals(new ChoiceGroup(new ChoiceElement(new TextElement("parse mark"), 1)), parsePattern("(1¦parse mark)"));
	}
}