package io.github.syst3ms.skriptparser.file;

import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.util.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for parsing a plaintext file into a list of {@link FileElement}s representing every line.
 */
public class FileParser {
    public static final Pattern LINE_PATTERN = Pattern.compile("^((?:[^#]|##)*)(\\s*#(?!#).*)$"); // Might as well take that from Skript

    /**
     * Parses a {@linkplain List} of strings into a list of {@link FileElement}s. This creates {@link FileElement} and
     * {@link FileSection} objects from the lines, effectively structuring the lines into a tree.
     * This removes comments from each line, and discards any blank lines afterwards.
     * @param fileName the name of the file the lines belong to
     * @param lines the list of lines to parse
     * @param expectedIndentation the indentation level the first line is expected to be at
     * @param lastLine a parameter that keeps track of the line count throughout recursive calls of this method when
     *                 parsing sections
     * @param logger
     * @return a list of {@link FileElement}s
     */
    public List<FileElement> parseFileLines(String fileName, List<String> lines, int expectedIndentation, int lastLine, SkriptLogger logger) {
        List<FileElement> elements = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String content;
            Matcher m = LINE_PATTERN.matcher(line);
            if (m.matches()) {
                content = m.group(1).replace("##", "#").trim();
            } else {
                content = line.replace("##", "#").trim();
            }
            if (content.matches("\\s*")) {
                elements.add(new VoidElement(fileName, lastLine + i, expectedIndentation));
                continue;
            }
            int lineIndentation = FileUtils.getIndentationLevel(line);
            if (lineIndentation > expectedIndentation) { // The line is indented too much
                logger.error("The line is indented too much (line " + (lastLine + i) + ": \"" + content + "\")");
                continue;
            } else if (lineIndentation < expectedIndentation) { // One indentation behind marks the end of a section
                return elements;
            }
            if (content.endsWith(":")) {
                if (i + 1 == lines.size()) {
                    elements.add(new FileSection(fileName, lastLine + i, content.substring(0, content.length() - 1),
                            new ArrayList<>(), expectedIndentation
                    ));
                } else {
                    List<FileElement> sectionElements = parseFileLines(fileName, lines.subList(i + 1, lines.size()),
                            expectedIndentation + 1, lastLine + i + 1,
                            logger);
                    elements.add(new FileSection(fileName, lastLine + i, content.substring(0, content.length() - 1),
                            sectionElements, expectedIndentation
                    ));
                    i += sectionElements.size();
                }
            } else {
                elements.add(new FileElement(fileName, lastLine + i, content, expectedIndentation));
            }
        }
        return elements;
    }

}
