package io.github.syst3ms.skriptparser.file;

import io.github.syst3ms.skriptparser.util.StringUtils;

public class SimpleFileLine implements FileElement {
    protected String content;
    protected int indentation;

    public SimpleFileLine(String content, int indentation) {
        this.content = content;
        this.indentation = indentation;
    }

    @Override
    public String getLineContent() {
        return content;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof SimpleFileLine && content.equalsIgnoreCase(((SimpleFileLine) obj).content)  && indentation == ((SimpleFileLine) obj).indentation;
    }

    public int getIndentation() {
        return indentation;
    }

    @Override
    public String toString() {
        return StringUtils.repeat("    ", indentation) + content;
    }
}
