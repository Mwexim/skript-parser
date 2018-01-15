package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.classes.SkriptParser;

/**
 * Text inside of a pattern. Is case and whitespace insensitive.
 */
public class TextElement implements PatternElement {
    private String text;

    public TextElement(String text) {
        this.text = text.toLowerCase();
    }


    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof TextElement && text.equalsIgnoreCase(((TextElement) obj).text);
    }

    @Override
    public int match(String s, int index, SkriptParser parser) {
        String matchTo = s.trim();
        String trimmed = text.trim();
        return startsWithIgnoreCaseAndWhitespace(matchTo, trimmed, index) ? index + trimmed.length() : -1;
    }

    private boolean startsWithIgnoreCaseAndWhitespace(String first, String second, int index) {
        String f = first.substring(0, index);
        String s = second.substring(0, index);
        return f.equalsIgnoreCase(s);
    }
}
