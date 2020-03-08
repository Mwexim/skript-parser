package io.github.syst3ms.skriptparser.util;

import io.github.syst3ms.skriptparser.parsing.SkriptParserException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility functions for strings
 */
public class StringUtils {
    public static final Pattern R_LITERAL_CONTENT_PATTERN = Pattern.compile("(.+?)\\((.+)\\)\\1"); // It's actually rare to be able to use '.+' raw like this

    /**
     * Counts combined occurences of one or more strings in another
     * @param s the string to find occurences in
     * @param toFind the strings to find occurences of
     * @return the amount of total occurences
     */
    public static int count(String s, String... toFind) {
        int count = 0;
        for (String sequence : toFind) {
            int occurences = s.length() - s.replace(sequence, "").length();
            count += occurences / sequence.length();
        }
        return count;
    }

    /**
     * Simply repeats the given string the given amount of times
     * @param str the string
     * @param times the amount of times to be repeated
     * @return the repeated string
     */
    public static String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * Find where a given pair of braces closes.
     * @param pattern the string to look in
     * @param opening the opening brace
     * @param closing the closing brace
     * @param start where the brace pair starts
     * @return the index at which the brace pair closes
     */
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

    /**
     * Similar to {@link #findClosingIndex(String, char, char, int)}, but returns the enclosed text
     * @param pattern the string to look in
     * @param opening the opening brace
     * @param closing the closing brace
     * @param start where the brace pair starts
     * @return the enclosed text
     */
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
     * Returns the next character in the string, skipping over curly braces and string literals
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

    /**
     * Finds the contents of an expression between %%
     * @param s the string containing the percents
     * @param start where the pair begins
     * @return the content between %%
     */
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
                i = closing;
            } else if (c == '%') {
                return s.substring(start, i);
            } else if (c == '}') { // We normally skip over these, this must be an error
                return null;
            }
        }
        return null; // There were no percents (unclosed percent is handled by VariableString already)
    }

    /**
     * Find the first occurence of a string in another one, ignoring case
     * @param haystack the string to look in
     * @param needle the string to look for
     * @param start where to look from
     * @return the index of the first occurence
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

    /**
     * Split a pattern at pipe characters, properly accounting for brackets and escapes
     * @param s the string to split
     * @return the split string
     */
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

    /**
     * Returns an array of two elements, containing the plural and singular forms of the
     * given pluralizable expression. Does not support escaping.
     */
    public static String[] getForms(String pluralizable) {
        List<String[]> words = new ArrayList<>();
        for (String s : pluralizable.split("\\s+")) {
            String[] split = s.split("@");
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

    /**
     * Trims all strings in the array
     * @param strings the strings
     * @return the array with all of its contents trimmed
     */
    private static String[] trimAll(String[] strings) {
        for (int i = 0; i < strings.length; i++)
            strings[i] = strings[i].trim();
        return strings;
    }

    /**
     * Adds a proper English indefinite article to a string
     * @param noun the string
     * @param plural whether it is plural or not
     * @return the string with its proper indefinite article
     */
    public static String withIndefiniteArticle(String noun, boolean plural) {
        noun = noun.trim();
        if (noun.isEmpty())
            return "";
        else if (plural)
            return noun;
        char first = Character.toLowerCase(noun.charAt(0));
        switch (first) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
            case 'y':
                return "an " + noun;
            default:
                return "a " + noun;
        }
    }
}
