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
        assertEquals(Collections.singletonList(new SimpleFileLine("test")), parser.parseFileLines(Collections.singletonList("test"), 0));
        assertEquals(
                Collections.singletonList(new FileSection("test section", new LinkedList<>())),
                parser.parseFileLines(Collections.singletonList("test section:"), 0)
        );
    }

}