package io.github.syst3ms.skriptparser.classes

import io.github.syst3ms.skriptparser.PatternParser
import io.github.syst3ms.skriptparser.pattern.TextElement
import org.junit.Test
import java.util.*
import kotlin.test.*

class SkriptParserTest {
    @Test
    @Throws(Exception::class)
    fun getPossibleInputs() {
        val patternParser = PatternParser()
        var e = patternParser.parsePattern("text [and optional]")
        val parser = SkriptParser(e!!)
        assertEquals(listOf(TextElement("text ")), parser.getPossibleInputs(parser.flatten(e)))
        e = patternParser.parsePattern("[optional] and text")
        assertEquals(
                Arrays.asList(TextElement("optional"), TextElement(" and text")),
                parser.getPossibleInputs(parser.flatten(e!!))
        )
        e = patternParser.parsePattern("[(this|that)] [(may be|is)] good")
        assertEquals(
                Arrays.asList(TextElement("this"), TextElement("that"), TextElement("may be"), TextElement("is"), TextElement(" good")),
                parser.getPossibleInputs(parser.flatten(e!!))
        )
    }

}