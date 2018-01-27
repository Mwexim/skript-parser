package io.github.syst3ms.skriptparser.file;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class FileParserTest {
	private SimpleFileLine simpleFileLine(String content, int indentation, int line) {
		return new SimpleFileLine("unit-tests", line, content, indentation);
	}
	
	private FileSection fileSection(String content, int indentation, int line, FileElement... elements) {
		return new FileSection("unit-tests", line, content, Arrays.asList(elements), indentation);
	}
	
	private List<FileElement> parseLines(FileParser parser, List<String> lines, int indentation) {
		return parser.parseFileLines("unit-tests", lines, indentation, 1);
	}
	
    @Test
    public void parseFileLines() throws Exception {
        FileParser parser = new FileParser();
        assertEquals(Collections.singletonList(simpleFileLine("test", 0, 1)), parseLines(parser, Collections.singletonList("test"), 0));
        assertEquals(
                Collections.singletonList(fileSection("test section", 0, 1)),
                parseLines(parser, Collections.singletonList("test section:"), 0)
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
            parseLines(parser, Arrays.asList("section with an element:", "    i am an element"), 0)
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
				),
				0
			)
		);
    	assertEquals(
    		Collections.singletonList(simpleFileLine("Hello there", 0, 1)),
			parseLines(parser, Collections.singletonList("Hello there # ignore this !!!"), 0)
		);
    	assertEquals(
    		Collections.singletonList(simpleFileLine("However # do not ignore this !!", 0, 1)),
			parseLines(parser, Collections.singletonList("However ## do not ignore this !!"), 0)
		);
    }
}