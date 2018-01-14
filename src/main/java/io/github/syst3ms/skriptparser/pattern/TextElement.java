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
        String compareTo = s.trim().toLowerCase();
        return compareTo.startsWith(text.trim(), index + 1) ? index + text.length() : -1;
    }
}
