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
        int start = 0;
        int end = 0;
        if (Character.isWhitespace(text.charAt(0))) {
            while (index + start < s.length() && Character.isWhitespace(s.charAt(index + start)))
                start++;
        }
        String trimmed = text.trim();
        // We advance until we reach the first non-whitespace character in s
        if (index + start + trimmed.length() > s.length()) {
            return -1;
        }
        if (trimmed.isEmpty()) {
            return index + start;
        } else if (s.regionMatches(true, index + start, trimmed, 0, trimmed.length())) {
            if (Character.isWhitespace(text.charAt(text.length() - 1))) {
                while (end < s.length() && Character.isWhitespace(s.charAt(index + start + trimmed.length() - end)))
                    end++;
            }
            return index + start + trimmed.length() + end; // Adjusting for some of the whitespace we ignored
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return text;
    }
}
