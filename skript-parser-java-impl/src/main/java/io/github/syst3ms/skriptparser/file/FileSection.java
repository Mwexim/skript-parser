package io.github.syst3ms.skriptparser.file;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FileSection extends SimpleFileLine {
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
