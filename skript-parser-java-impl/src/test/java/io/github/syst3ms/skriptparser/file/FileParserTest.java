package io.github.syst3ms.skriptparser.file;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class FileParserTest {
    @Test
    public void parseFileLines() throws Exception {
        FileParser parser = new FileParser();
        assertEquals(Collections.singletonList(new SimpleFileLine("test", 0)), parser.parseFileLines(Collections.singletonList("test"), 0));
        assertEquals(
                Collections.singletonList(new FileSection("test section", new LinkedList<>(), 0)),
                parser.parseFileLines(Collections.singletonList("test section:"), 0)
        );
		assertEquals(
            Collections.singletonList(
                new FileSection("section with an element", Collections.singletonList(new SimpleFileLine("i am an element", 1)), 0)
            ),
            parser.parseFileLines(Arrays.asList("section with an element:", "    i am an element"), 0)
        );
    	List<FileElement> expected = Arrays.asList(
    		new SimpleFileLine("let's see nested sections", 0),
    		new FileSection(
    			"this is a section",
				Collections.singletonList(
					new FileSection(
						"this is another one",
						Collections.singletonList(new SimpleFileLine("with an element inside", 2)),
						1
					)
				),
			0)
		);
    	assertEquals(
    		expected,
			parser.parseFileLines(
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
    		Collections.singletonList(new SimpleFileLine("Hello there", 0)),
			parser.parseFileLines(Collections.singletonList("Hello there # ignore this !!!"), 0)
		);
    	assertEquals(
    		Collections.singletonList(new SimpleFileLine("However # do not ignore this !!", 0)),
			parser.parseFileLines(Collections.singletonList("However ## do not ignore this !!"), 0)
		);
    }
}