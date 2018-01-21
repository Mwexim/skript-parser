package io.github.syst3ms.skriptparser

import io.github.syst3ms.skriptparser.classes.SkriptParser
import io.github.syst3ms.skriptparser.classes.SkriptRegistration
import io.github.syst3ms.skriptparser.classes.TypeManager
import io.github.syst3ms.skriptparser.pattern.*
import org.junit.Test
import java.util.regex.Pattern
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PatternParserTest {

    @Test
    @Throws(Exception::class)
    fun testParsePattern() {
        val parser = PatternParser()
        assertEquals(TextElement("syntax"), parser.parsePattern("syntax"))
        assertEquals(OptionalGroup(TextElement("optional")), parser.parsePattern("[optional]"))
        var expected: PatternElement = OptionalGroup(
                CompoundElement(
                        TextElement("something "),
                        OptionalGroup(TextElement("optional")),
                        TextElement(" again")
                )
        )
        assertEquals(expected, parser.parsePattern("[something [optional] again]"))
        assertEquals(ChoiceGroup(ChoiceElement(TextElement("single choice"), 0)), parser.parsePattern("(single choice)"))
        assertEquals(ChoiceGroup(ChoiceElement(TextElement("parse mark"), 1)), parser.parsePattern("(1\u00a6parse mark)"))
        expected = ChoiceGroup(
                ChoiceElement(TextElement("first choice"), 0),
                ChoiceElement(TextElement("second choice"), 0)
        )
        assertEquals(expected, parser.parsePattern("(first choice|second choice)"))
        expected = ChoiceGroup(
                ChoiceElement(TextElement("first mark"), 0),
                ChoiceElement(TextElement("second mark"), 1)
        )
        assertEquals(expected, parser.parsePattern("(first mark|1\u00a6second mark)"))
        expected = OptionalGroup(
                CompoundElement(
                        TextElement("lookie, "),
                        ChoiceGroup(
                                ChoiceElement(TextElement("another"), 0),
                                ChoiceElement(TextElement("choice"), 1)
                        ),
                        TextElement(" !")
                )
        )
        assertEquals(expected, parser.parsePattern("[lookie, (another|1\u00a6choice) !]"))
        assertEquals(RegexGroup(Pattern.compile(".+")), parser.parsePattern("<.+>"))
        assertEquals(
                ExpressionElement(
                        listOf(TypeManager.getPatternType("number")!!),
                        ExpressionElement.Acceptance.BOTH),
                parser.parsePattern("%number%")
        )
        assertEquals(
                ExpressionElement(
                        listOf(
                                TypeManager.getPatternType("number")!!,
                                TypeManager.getPatternType("strings")!!
                        ),
                        ExpressionElement.Acceptance.LITERALS_ONLY
                ),
                parser.parsePattern("%*number/strings%")
        )
        assertNull(parser.parsePattern("(unclosed"))
        assertNull(parser.parsePattern("%unfinished type"))
    }

    @Test
    @Throws(Exception::class)
    fun testMatch() {
        val patternParser = PatternParser()
        var pattern = patternParser.parsePattern("pattern")
        var parser = SkriptParser(pattern!!)
        assertEquals(7, pattern.match("pattern", 0, parser).toLong())
        pattern = patternParser.parsePattern("pattern [with optional]")
        parser = SkriptParser(pattern!!)
        assertEquals(8, pattern.match("pattern", 0, parser).toLong())
        assertEquals(21, pattern.match("pattern with optional", 0, parser).toLong())
        pattern = patternParser.parsePattern("pattern [with [another] optional]")
        parser = SkriptParser(pattern!!)
        assertEquals(22, pattern.match("pattern with optional", 0, parser).toLong())
        assertEquals(30, pattern.match("pattern with another optional", 0, parser).toLong())
        pattern = patternParser.parsePattern("you must (choose|this|or this)")
        parser = SkriptParser(pattern!!)
        assertEquals(15, pattern.match("you must choose", 0, parser).toLong())
        assertEquals(13, pattern.match("you must this", 0, parser).toLong())
        assertEquals(16, pattern.match("you must or this", 0, parser).toLong())
        pattern = patternParser.parsePattern("I choose (1\u00a6this|2\u00a6that)")
        parser = SkriptParser(pattern!!)
        assertTrue(pattern.match("I choose this", 0, parser) == 13 && parser.parseMark == 1)
        // The real stuff
        pattern = patternParser.parsePattern("say %number% [!]")
        parser = SkriptParser(pattern!!)
        pattern.match("say 2", 0, parser)
        assertEquals(1, parser.getParsedExpressions().size.toLong())
        parser = SkriptParser(pattern)
        pattern.match("say 2 !", 0, parser)
        assertEquals(1, parser.getParsedExpressions().size.toLong())
    }

    companion object {
        init {
            val reg = SkriptRegistration("unit-tests")
            reg.addType(Number::class.java, "number", "number(?<plural>s)?", { it.toDouble() })
            reg.addType(String::class.java, "string", "string(?<plural>s)?")
            reg.register()
        }
    }
}