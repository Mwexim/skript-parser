package io.github.syst3ms.skriptparser.file;

import io.github.syst3ms.skriptparser.util.StringUtils;

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
        return obj != null
            && obj instanceof SimpleFileLine && content.equalsIgnoreCase(((SimpleFileLine) obj).content)
            && indentation == ((SimpleFileLine) obj).indentation
            && fileName.equals(((SimpleFileLine) obj).fileName)
            && line == ((SimpleFileLine) obj).line;
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
