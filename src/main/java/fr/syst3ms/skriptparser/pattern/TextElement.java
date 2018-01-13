package fr.syst3ms.skriptparser.pattern;

public class TextElement implements PatternElement {
    private String text;

    public TextElement(String text) {
        this.text = text;
    }

    @Override
    public int match(String s, int index) {
        // TODO
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof TextElement && text.equalsIgnoreCase(((TextElement) obj).text);
    }
}
