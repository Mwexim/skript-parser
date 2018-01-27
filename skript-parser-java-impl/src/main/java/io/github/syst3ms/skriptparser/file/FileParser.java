package io.github.syst3ms.skriptparser.file;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileParser {
    public static final Pattern LEADING_WHITESPACE_PATTERN = Pattern.compile("(\\s+)\\S.+");
    public static final Pattern LINE_PATTERN = Pattern.compile("^((?:[^#]|##)*)(\\s*#(?!#).*)$"); // Might as well take that from Skript

    public List<FileElement> parseFileLines(String fileName, List<String> lines, int expectedIndentation, int lastLine) {
		List<FileElement> elements = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String content;
            final Matcher m = LINE_PATTERN.matcher(line);
            if (m.matches())
                content = m.group(1).replace("##", "#").trim();
            else
            	content = line.replace("##", "#").trim();
            if (content.matches("\\s*"))
                continue;
            int lineIndentation = getIndentationLevel(line);
            if (expectedIndentation > lineIndentation) { // One indentation behind marks the end of a section
                error("Invalid indentation, expected " + expectedIndentation +
                      " indents, but found " +
                      lineIndentation);
                continue; // Let's ignore it *for now*
            } else if (expectedIndentation < lineIndentation) { // End of section
                return elements;
            }
            if (content.endsWith(":")) {
                if (i + 1 == lines.size()) {
                    elements.add(new FileSection(fileName, lastLine + i, content.substring(0, content.length() - 1), new ArrayList<>(), expectedIndentation));
                } else {
                    List<FileElement> sectionElements = parseFileLines(fileName, lines.subList(i + 1, lines.size()), expectedIndentation + 1, lastLine + i + 1);
                    elements.add(new FileSection(fileName, lastLine + i, content.substring(0, content.length() - 1), sectionElements, expectedIndentation));
                }
            } else {
                elements.add(new SimpleFileLine(fileName, lastLine + i, content, expectedIndentation));
            }
        }
        return elements;
    }

    /*
    // Remember, CodeSection extends Effect
    public List<Effect> parseFileSyntaxes(List<FileElement> fileElements) {
        List<Effect> effects = new ArrayList<>();
        for (FileElement fileElement : fileElements) {
            if (fileElement instanceof SimpleFileLine) {
                Effect eff = Effect.parse((SimpleFileLine) fileElement);
                if (eff == null) {
                    error("Can't understand this effect : '" + eff);
                }
                effects.add(eff);
            } else {
                effects.add();
            }
        }
        return effects;
    }
    */

    private void error(String error) {
        // TODO
    }

    private int getIndentationLevel(String line) {
        Matcher m = LEADING_WHITESPACE_PATTERN.matcher(line);
        if (m.matches()) {
            return StringUtils.count(m.group(1), "\t", "    ");
        } else {
            return 0;
        }
    }
}
