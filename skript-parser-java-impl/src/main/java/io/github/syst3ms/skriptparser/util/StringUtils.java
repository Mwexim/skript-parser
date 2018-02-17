package io.github.syst3ms.skriptparser.util;

import io.github.syst3ms.skriptparser.parsing.SkriptParserException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static final Pattern R_LITERAL_CONTENT_PATTERN = Pattern.compile("(.+?)\\((.+)\\)\\1"); // It's actually rare to be able to use '.+' raw like this

    public static int count(String s, String... toFind) {
        int count = 0;
        for (String sequence : toFind) {
            int occurences = s.length() - s.replace(sequence, "").length();
            count += occurences / sequence.length();
        }
        return count;
    }

    public static String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static int findClosingIndex(String pattern, char opening, char closing, int start) {
        int n = 0;
        for (int i = start; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == closing) {
                n--;
                if (n == 0) {
                    return i;
                }
            } else if (c == opening) {
                n++;
            }
        }
        return -1;
    }

    public static String getEnclosedText(String pattern, char opening, char closing, int start) {
        int closingBracket = findClosingIndex(pattern, opening, closing, start);
        if (closingBracket == -1) {
            return null;
        } else {
            return pattern.substring(start + 1, closingBracket);
        }
    }

    /**
     * Returns the next character in the string, skipping over brackets and string literals
     * @param s the string  to search
     * @param index the current index
     * @return the index of the next "simple" character, or -1 if the end of the string has been reached
     * @throws StringIndexOutOfBoundsException if {@code index < 0}
     */
    public static int nextSimpleCharacterIndex(String s, int index) {
        if (index < 0)
            throw new StringIndexOutOfBoundsException(index);
        char[] chars = s.toCharArray();
        for (int i = index; i < chars.length; i++) {
            char c = chars[i];
            if (c == '\\') {
                if (i == chars.length - 1)
                    return -1;
                return i + 1;
            } else if (c == '(') {
                int closing = findClosingIndex(s, '(', ')', i);
                if (closing == -1)
                    return -1;
                i = closing;
            } else if (c == '{') {
                int closing = findClosingIndex(s, '{', '}', i);
                if (closing == -1)
                    return -1;
                i = closing;
            } else if (c == '"') {
                int closing = findClosingIndex(s, '"', '"', i);
                if (closing == -1)
                    return -1;
                i = closing;
            } else if (c == '\'') {
                int closing = findClosingIndex(s, '\'', '\'', i);
                if (closing == -1)
                    return -1;
                i = closing;
            } else if (c == 'R' && i < s.length() - 2 && chars[i + 1] == '"') {
                Matcher m = R_LITERAL_CONTENT_PATTERN.matcher(s).region(i + 2, s.length());
                if (!m.lookingAt())
                    return -1;
                i = m.end() + 1;
            } else {
                return i;
            }
        }
        return -1;
    }

    public static String getPercentContent(String s, int start) {
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '{') { // We must ignore variable content
                int closing = findClosingIndex(s, '{', '}', i);
                if (closing == -1)
                    return null;
                i += closing;
            } else if (c == '%') {
                return s.substring(start, i);
            } else if (c == '}') { // We normally skip over these, this must be an error
                return null;
            }
        }
        return null; // There were no percents (unclosed percent is handled by VariableString already)
    }

    public static boolean startsWithIgnoreCase(String haystack, String needle) {
        return haystack.toLowerCase().startsWith(needle.toLowerCase());
    }

    public static String fixEncoding(String s) {
        try {
            return new String(s.getBytes(Charset.defaultCharset()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    /**
     * Returns an array of two elements, containing the plural and singular forms of the
     * given pluralizable expression. Does not support escaping.
     */
    public static String[] getForms(String pluralizable) {
        pluralizable = fixEncoding(pluralizable);
        List<String[]> words = new ArrayList<>();
        for (String s : pluralizable.split("\\s+")) {
            int count = count(s, "\u00a6");
            String[] split;
            switch (count) {
                case 0:
                    words.add(new String[]{s, s});
                    break;
                case 1:
                    split = s.split("\\xa6");
                    words.add(new String[]{split[0], split[0] + split[1]});
                    break;
                case 2:
                    split = s.split("\\xa6");
                    words.add(new String[]{split[0] + split[1], split[0] + split[2]});
                    break;
                default:
                    throw new SkriptParserException("Invalid pluralized word : " + s);
            }
        }
        String[] pluralized = new String[]{"", ""};
        for (String[] word : words) {
            pluralized[0] += word[0] + " ";
            pluralized[1] += word[1] + " ";
        }
        return trimAll(pluralized);
    }

    private static String[] trimAll(String[] strings) {
        for (int i = 0; i < strings.length; i++)
            strings[i] = strings[i].trim();
        return strings;
    }
}
