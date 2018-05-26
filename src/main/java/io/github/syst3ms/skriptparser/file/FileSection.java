package io.github.syst3ms.skriptparser.file;

import java.util.List;

/**
 * A class describing a section of a script inside a file (e.g a line ending with a colon and containing all the lines that
 * were indented after it. "all the lines" doesn't exclude sections.
 */
public class FileSection extends FileElement {
    private List<FileElement> elements;

    public FileSection(String fileName, int line, String content, List<FileElement> elements, int indentation) {
        super(fileName, line, content, indentation);
        this.elements = elements;
    }

    public List<FileElement> getElements() {
        return elements;
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
