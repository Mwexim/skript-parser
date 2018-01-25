package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.parsing.SkriptParser;

/**
 * Text inside of a pattern. Is case and whitespace insensitive.
 */
public class TextElement implements PatternElement {
    private String text;

    public TextElement(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof TextElement && text.equalsIgnoreCase(((TextElement) obj).text);
    }

    @Override
    public int match(String s, int index, SkriptParser parser) {
        if (parser.getElement().equals(this))
            parser.advanceInPattern();
        String trimmed = text.trim();
        while (index < s.length() && s.charAt(index) == ' ') {// Hopefully fix some spacing issues
            index++;
        }
        if (index + trimmed.length() > s.length()) {
            return -1;
        }
        String substr = s.substring(index, index + trimmed.length());
        if (substr.equalsIgnoreCase(trimmed)) {
            return index + text.length(); // Let's not forget the spaces we removed earlier
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return text;
    }
}
