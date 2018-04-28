package io.github.syst3ms.skriptparser.file;

import io.github.syst3ms.skriptparser.util.FileUtils;
import org.junit.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class FileParserTest {
    private FileElement simpleFileLine(String content, int indentation, int line) {
        return new FileElement("unit-tests", line, content, indentation);
    }

    private FileSection fileSection(String content, int indentation, int line, FileElement... elements) {
        return new FileSection("unit-tests", line, content, Arrays.asList(elements), indentation);
    }

    private List<FileElement> parseLines(FileParser parser, List<String> lines) {
        return parser.parseFileLines("unit-tests", lines, 0, 1);
    }

    @Test
    public void parseFileLines() throws Exception {
        FileParser parser = new FileParser();
        assertEquals(Collections.singletonList(simpleFileLine("check", 0, 1)), parseLines(parser, Collections.singletonList("check")));
        assertEquals(
                Collections.singletonList(fileSection("check section", 0, 1)),
                parseLines(parser, Collections.singletonList("check section:"))
        );
        assertEquals(
            Collections.singletonList(
                fileSection(
                    "section with an element",
                    0,
                    1,
                    simpleFileLine("i am an element", 1, 2)
                )
            ),
            parseLines(parser, Arrays.asList("section with an element:", "    i am an element"))
        );
        List<FileElement> expected = Arrays.asList(
            simpleFileLine("let's see nested sections", 0, 1),
            fileSection(
                "this is a section",
                0,
                2,
                fileSection(
                    "this is another one",
                    1,
                    3,
                    simpleFileLine("with an element inside", 2, 4)
                )
            )
        );
        assertEquals(
            expected,
            parseLines(parser,
                Arrays.asList(
                    "let's see nested sections",
                    "this is a section:",
                    "\tthis is another one:",
                    "\t\twith an element inside"
                ))
        );
        expected = Arrays.asList(
            simpleFileLine("Let's check simple elements after a section closes", 0, 1),
            fileSection(
                "The section",
                0,
                2,
                simpleFileLine("the element inside", 1, 3)
            ),
            simpleFileLine("the element after the section", 0, 4)
        );
        assertEquals(
            expected,
            parseLines(
                parser,
                Arrays.asList(
                    "Let's check simple elements after a section closes",
                    "The section:",
                    "\tthe element inside",
                    "the element after the section"
                )
            )
        );
        assertEquals(
            Collections.singletonList(simpleFileLine("Hello there", 0, 1)),
            parseLines(parser, Collections.singletonList("Hello there # ignore this !!!"))
        );
        assertEquals(
            Collections.singletonList(simpleFileLine("However # do not ignore this !!", 0, 1)),
            parseLines(parser, Collections.singletonList("However ## do not ignore this !!"))
        );
    }

    @Test
    public void readLines() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("test-file.txt").getFile());
        assertEquals(
            Arrays.asList(
                "# The purpose of this file is to test multiline syntax",
                "First with no indents",
                "Opening a section:",
                "    now with some indents"
            ),
            FileUtils.readAllLines(file)
        );
    }
}