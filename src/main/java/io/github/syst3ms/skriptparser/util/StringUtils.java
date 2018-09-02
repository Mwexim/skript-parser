package io.github.syst3ms.skriptparser.util;

import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static final Pattern R_LITERAL_CONTENT_PATTERN = Pattern.compile("(.+?)\\((.+)\\)\\1"); // It's actually rare to be able to use '.+' raw like this
    private static final String osName = System.getProperty("os.name");

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

    @Nullable
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
            } else if (c == '{') {
                int closing = findClosingIndex(s, '{', '}', i);
                if (closing == -1)
                    return -1;
                i = closing;
            } else if (c == '"') {
                int closing = s.indexOf('"', i + 1);
                if (closing == -1)
                    return -1;
                i = closing;
            } else if (c == '\'') {
                int closing = s.indexOf('\'', i + 1);
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
        return s.length();
    }

    @Nullable
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

    /*
     * Does not allocate heap memory, so much better for this purpose
     */
    public static int indexOfIgnoreCase(String haystack,
                                        String needle,
                                        int start) {
        if (needle.isEmpty() || haystack.isEmpty()) {
            // Fallback to legacy behavior.
            return haystack.indexOf(needle);
        }
        for (int i = start; i < haystack.length(); ++i) {
            // Early out, if possible.
            if (i + needle.length() > haystack.length()) {
                return -1;
            }

            // Attempt to match substring starting at position i of haystack.
            int j = 0;
            int ii = i;
            while (ii < haystack.length() && j < needle.length()) {
                char c = Character.toLowerCase(haystack.charAt(ii));
                char c2 = Character.toLowerCase(needle.charAt(j));
                if (c != c2) {
                    break;
                }
                j++;
                ii++;
            }
            // Walked all the way to the end of the needle, return the start
            // position that this was found.
            if (j == needle.length()) {
                return i;
            }
        }
        return -1;
    }

    public static String[] splitVerticalBars(String s) {
        List<String> split = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '\\') {
                sb.append(c);
                if (i + 1 < s.length()) {
                    sb.append(chars[++i]);
                }
            } else if (c == '(' || c == '[') {
                char closing = c == '(' ? ')' : ']';
                String text = getEnclosedText(s, c, closing, i);
                if (text == null)
                    throw new SkriptParserException("Couldn't find a closing '" + c + "' bracket at index " + i);
                sb.append(c).append(text).append(closing);
                i += text.length() + 1;
            } else if (c == '|') {
                split.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        split.add(sb.toString());
        return split.toArray(new String[0]);
    }

    public static String fixEncoding(String s) {
        if (osName.contains("Windows"))
            return new String(s.getBytes(Charset.defaultCharset()), StandardCharsets.UTF_8);
        return s;
    }

    /**
     * Returns an array of two elements, containing the plural and singular forms of the
     * given pluralizable expression. Does not support escaping.
     */
    public static String[] getForms(String pluralizable) {
        pluralizable = fixEncoding(pluralizable);
        List<String[]> words = new ArrayList<>();
        for (String s : pluralizable.split("\\s+")) {
            String[] split = s.split("\\xa6");
            switch (split.length) {
                case 1:
                    words.add(new String[]{s, s});
                    break;
                case 2:
                    words.add(new String[]{split[0], split[0] + split[1]});
                    break;
                case 3:
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

    public static String withIndefiniteArticle(String name, boolean plural) {
        name = name.trim();
        if (name.isEmpty())
            return "";
        else if (plural)
            return name;
        char first = Character.toLowerCase(name.charAt(0));
        switch (first) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
            case 'y':
                return "an " + name;
            default:
                return "a " + name;
        }
    }
}
