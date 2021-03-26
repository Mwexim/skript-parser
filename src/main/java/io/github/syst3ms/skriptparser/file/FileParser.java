package io.github.syst3ms.skriptparser.file;

import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.util.FileUtils;
import io.github.syst3ms.skriptparser.util.color.Color;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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
     * @param logger the logger
     * @return a list of {@link FileElement}s
     */
    public static List<FileElement> parseFileLines(String fileName, List<String> lines, int expectedIndentation, int lastLine, SkriptLogger logger) {
        List<FileElement> elements = new ArrayList<>();
        for (var i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            String content = removeComments(line);

            if (content == null) {
                content = line.replace("##", "#").strip();
            } else if (content.isEmpty()) {
                elements.add(new VoidElement(fileName, lastLine + i, expectedIndentation));
                continue;
            }

            var lineIndentation = FileUtils.getIndentationLevel(line, false);
            if (lineIndentation > expectedIndentation) { // The line is indented too much
                logger.error(
                        "The line is indented too much (line " + (lastLine + i) + ": \"" + content + "\")",
                        ErrorType.STRUCTURE_ERROR,
                        "You only need to indent once (using tabs) after each section (a line that ends with a ':'). Try to omit some tabs so the line suffices this rule"
                );
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
                    var sectionElements = parseFileLines(fileName, lines.subList(i + 1, lines.size()),
                            expectedIndentation + 1, lastLine + i + 1,
                            logger);
                    elements.add(new FileSection(fileName, lastLine + i, content.substring(0, content.length() - 1),
                            sectionElements, expectedIndentation
                    ));
                    i += count(sectionElements);
                }
            } else {
                elements.add(new FileElement(fileName, lastLine + i, content, expectedIndentation));
            }
        }
        return elements;
    }

    private static int count(List<FileElement> elements) {
        var count = 0;
        for (var element : elements) {
            if (element instanceof FileSection) {
                count += count(((FileSection) element).getElements()) + 1;
            } else {
                count++;
            }
        }
        return count;
    }

    /**
     * Removes all comments from a given String
     * @param string the String
     * @return the String with the comments removed; {@literal null} if no comments were found; an empty string if the whole String was a comment
     */
    @Nullable
    private static String removeComments(String string) {
        if (string.matches("^[\\s\\t]*#[^#]+") || string.startsWith("#") || string.isBlank()) {
            return ""; // Whole string is a comment
        }

        var builder = new StringBuilder();
        outer:
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            if (c == '#') {
                if (string.charAt(i + 1) == '#') {
                    builder.append(c).append(string.charAt(++i));
                } else {
                    var checked = string.substring(i + 1);
                    for (int j : new int[] {3, 6, 8}) {
                        if (checked.length() >= j
                                && Color.COLOR_PATTERN.matcher(checked.substring(0, j)).matches()) {
                            builder.append(c).append(checked, 0, j);
                            i+=j;
                            continue outer;
                        }
                    }

                    // Comment was found. Erase it from the string
                    assert !builder.toString().isEmpty();
                    return builder.toString().strip();
                }
            } else {
                builder.append(c);
            }
        }
        if (builder.toString().equals(string))
            return null;
        return builder.toString().strip();
    }
}
