package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.parsing.SkriptParser;
import org.jetbrains.annotations.Nullable;

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
    public boolean equals(@Nullable Object obj) {
        return obj != null && obj instanceof TextElement && text.equalsIgnoreCase(((TextElement) obj).text);
    }

    @Override
    public int match(String s, int index, SkriptParser parser) {
        int i = index;
        if (parser.getOriginalElement().equals(this))
            parser.advanceInPattern();
        String trimmed = text.trim();
        while (i < s.length() && Character.isWhitespace(s.charAt(i)))
            i++;
        if (i + trimmed.length() > s.length()) {
            return -1;
        }
        if (trimmed.isEmpty()) {
            return i;
        } else if (s.regionMatches(true, i, trimmed, 0, trimmed.length())) {
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
