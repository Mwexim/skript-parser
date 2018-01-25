package io.github.syst3ms.skriptparser.file;

import java.util.List;

public class FileSection extends SimpleFileLine {
    private List<FileElement> elements;

    public FileSection(String content, List<FileElement> elements) {
        super(content);
        this.elements = elements;
    }

    public List<FileElement> getElements() {
        return elements;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && elements.equals(((FileSection) obj).elements);
    }
}
