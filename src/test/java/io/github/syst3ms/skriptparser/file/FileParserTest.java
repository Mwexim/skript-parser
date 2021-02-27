package io.github.syst3ms.skriptparser.file;

import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.util.FileUtils;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FileParserTest {
    private FileElement simpleFileLine(String content, int indentation, int line) {
        return new FileElement("unit-tests", line, content, indentation);
    }

    private FileSection fileSection(String content, int indentation, int line, FileElement... elements) {
        return new FileSection("unit-tests", line, content, Arrays.asList(elements), indentation);
    }

    private List<FileElement> parseLines(List<String> lines) {
        return FileParser.parseFileLines("unit-tests", lines, 0, 1, new SkriptLogger());
    }

    @Test
    public void parseFileLines() {
        assertEquals(
            Collections.singletonList(simpleFileLine("line", 0, 1)),
            parseLines(Collections.singletonList("line"))
        );
        assertEquals(
            Collections.singletonList(fileSection("section", 0, 1)),
            parseLines(Collections.singletonList("section:"))
        );
        assertEquals(
            Collections.singletonList(
                fileSection(
                    "section with element",
                    0,
                    1,
                    simpleFileLine("element", 1, 2)
                )
            ),
            parseLines(Arrays.asList("section with element:", "\telement"))
        );
        List<FileElement> expected = Arrays.asList(
            simpleFileLine("nested sections", 0, 1),
            fileSection(
                "section",
                0,
                2,
                fileSection(
                    "other section",
                    1,
                    3,
                    simpleFileLine("element", 2, 4)
                )
            )
        );
        assertEquals(
            expected,
            parseLines(Arrays.asList(
                    "nested sections",
                    "section:",
                    "\tother section:",
                    "\t\telement"
                ))
        );
        expected = Arrays.asList(
            simpleFileLine("elements after section", 0, 1),
            fileSection(
                "section",
                0,
                2,
                simpleFileLine("element", 1, 3)
            ),
            simpleFileLine("element after", 0, 4)
        );
        assertEquals(
            expected,
            parseLines(
                Arrays.asList(
                    "elements after section",
                    "section:",
                    "\telement",
                    "element after"
                )
            )
        );
        assertEquals(
            Collections.singletonList(simpleFileLine("code", 0, 1)),
            parseLines(Collections.singletonList("code # comment"))
        );
        assertEquals(
            Collections.singletonList(simpleFileLine("code # not comment", 0, 1)),
            parseLines(Collections.singletonList("code ## not comment"))
        );
    }

    @Test
    public void readLines() throws Exception {
        Path filePath = Paths.get(ClassLoader.getSystemResource("misc/multiline.txt").toURI());
        assertEquals(
            Arrays.asList(
                "# Testing multiline syntax",
                "no indent",
                "section:",
                "    with indent"
            ),
            FileUtils.readAllLines(filePath)
        );
    }
}