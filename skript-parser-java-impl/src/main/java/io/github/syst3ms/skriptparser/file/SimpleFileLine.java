package io.github.syst3ms.skriptparser.file;

public class SimpleFileLine implements FileElement {
    private String content;

    public SimpleFileLine(String content) {
        this.content = content;
    }

    @Override
    public String getLineContent() {
        return content;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof SimpleFileLine && content.equalsIgnoreCase(((SimpleFileLine) obj).content);
    }
}
