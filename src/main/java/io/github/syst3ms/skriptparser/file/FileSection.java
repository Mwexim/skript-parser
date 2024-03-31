package io.github.syst3ms.skriptparser.file;

import io.github.syst3ms.skriptparser.lang.entries.OptionLoader;

import java.util.List;
import java.util.Optional;

/**
 * A class describing a section of a script inside a file (e.g a line ending with a colon and containing all the lines that
 * were indented after it. "all the lines" doesn't exclude sections.
 */
public class FileSection extends FileElement {

    private final List<FileElement> elements;
    private int length = -1;

    public FileSection(String fileName, int line, String content, List<FileElement> elements, int indentation) {
        super(fileName, line, content, indentation);
        this.elements = elements;
    }

    /**
     * Returns the elements inside of the section
     * @return the elements inside of the section
     */
    public List<FileElement> getElements() {
        return elements;
    }

    public int length() {
        if (length >= 0)
            return length;
        length = 0;
        for (var e : elements) {
            if (e instanceof FileSection) {
                length += ((FileSection) e).length() + 1;
            } else {
                length++;
            }
        }
        return length;
    }

    public Optional<FileElement> get(String line) {
        return elements.stream()
                .filter(element -> {
                    String content = element.getLineContent();
                    content = content.substring(0, content.lastIndexOf(OptionLoader.OPTION_SPLIT_PATTERN.trim()));
                    return content.equalsIgnoreCase(line);
                })
                .findFirst();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && elements.equals(((FileSection) obj).elements);
    }

    @Override
    public String toString() {
        return super.toString() + ":";
    }

}
