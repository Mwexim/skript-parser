package io.github.syst3ms.skriptparser.file;

import io.github.syst3ms.skriptparser.util.StringUtils;
import org.jetbrains.annotations.NotNull;

public class SimpleFileLine implements FileElement {
    private final String fileName;
    private final int line;
    private String content;
    private int indentation;

    public SimpleFileLine(String fileName, int line, String content, int indentation) {
        this.fileName = fileName;
        this.line = line;
        this.content = content;
        this.indentation = indentation;
    }

    @Override
    public String getLineContent() {
        return content;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SimpleFileLine)) {
            return false;
        } else {
            SimpleFileLine other = (SimpleFileLine) obj;
            return indentation == other.indentation &&
                   line == other.line &&
                   content.equalsIgnoreCase(other.content) &&
                   fileName.equals(other.fileName);
        }
    }

    public int getIndentation() {
        return indentation;
    }

    @Override
    public String toString() {
        return StringUtils.repeat("    ", indentation) + content;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLine() {
        return line;
    }
}
