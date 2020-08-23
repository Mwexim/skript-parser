package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.parsing.MatchContext;
import org.jetbrains.annotations.Nullable;

/**
 * Text inside of a pattern. Is case and whitespace insensitive.
 */
public class TextElement implements PatternElement {
    private final String text;

    public TextElement(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof TextElement && text.equalsIgnoreCase(((TextElement) obj).text);
    }

    @Override
    public int match(String s, int index, MatchContext context) {
        if (text.isEmpty())
            return index;
        var start = 0;
        if (Character.isWhitespace(text.charAt(0))) {
            start = s.length() - s.stripLeading().length();
        }
        var end = 0;
        if (Character.isWhitespace(text.charAt(text.length() - 1))) {
            end = s.length() - s.stripTrailing().length();
        }
        var stripped = text.strip();
        // We advance until we reach the first non-whitespace character in s
        if (index + start + stripped.length() > s.length()) {
            return -1;
        }
        if (stripped.isEmpty()) {
            return index + start;
        } else if (s.regionMatches(true, index + start, stripped, 0, stripped.length())) {
            return index + start + stripped.length() + end; // Adjusting for some of the whitespace we ignored
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return text;
    }
}
