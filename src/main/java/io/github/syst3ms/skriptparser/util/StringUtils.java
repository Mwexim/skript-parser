package io.github.syst3ms.skriptparser.util;

import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.SkriptParserException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Utility functions for strings
 */
public class StringUtils {
    public static final Pattern R_LITERAL_CONTENT_PATTERN = Pattern.compile("(.+?)\\((.+)\\)\\1"); // It's actually rare to be able to use '.+' raw like this

    /**
     * Counts combined occurrences of one or more strings in another
     * @param s the string to find occurrences in
     * @param toFind the strings to find occurrences of
     * @return the amount of total occurrences
     */
    public static int count(String s, String... toFind) {
        var count = 0;
        for (var sequence : toFind) {
            var occurrences = s.length() - s.replace(sequence, "").length();
            count += occurrences / sequence.length();
        }
        return count;
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
        var n = 0;
        for (var i = start; i < pattern.length(); i++) {
            var c = pattern.charAt(i);
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
    public static Optional<String> getEnclosedText(String pattern, char opening, char closing, int start) {
        var closingBracket = findClosingIndex(pattern, opening, closing, start);
        if (closingBracket == -1) {
            return Optional.empty();
        } else {
            return Optional.of(pattern.substring(start + 1, closingBracket));
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
        var chars = s.toCharArray();
        for (var i = index; i < chars.length; i++) {
            var c = chars[i];
            if (c == '\\') {
                if (i == chars.length - 1)
                    return -1;
                return i + 1;
            } else if (c == '{') {
                var closing = findClosingIndex(s, '{', '}', i);
                if (closing == -1)
                    return -1;
                i = closing;
            } else if (c == '"') {
                var closing = s.indexOf('"', i + 1);
                if (closing == -1)
                    return -1;
                i = closing;
            } else if (c == '\'') {
                var closing = s.indexOf('\'', i + 1);
                if (closing == -1)
                    return -1;
                i = closing;
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
    public static Optional<String> getPercentContent(String s, int start) {
        for (var i = start; i < s.length(); i++) {
            var c = s.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '{') { // We must ignore variable content
                var closing = findClosingIndex(s, '{', '}', i);
                if (closing == -1)
                    return Optional.empty();
                i = closing;
            } else if (c == '%') {
                return Optional.of(s.substring(start, i));
            } else if (c == '}') { // We normally skip over these, this must be an error
                return Optional.empty();
            }
        }
        return Optional.empty(); // There were no percents (unclosed percent is handled by VariableString already)
    }

    /**
     * Finds the contents until a closing bracket is found
     * @param s the string containing the percents
     * @param start where the pair begins
     * @param closingBracket the closing bracket
     * @return the content until the closing bracket
     */
    public static Optional<String> getBracketContent(String s, int start, char closingBracket) {
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == closingBracket) {
                return Optional.of(s.substring(start, i));
            }
        }
        return Optional.empty(); // There was no closing bracket
    }

    /**
     * Find the first occurrence of a string in another one, ignoring case
     * @param haystack the string to look in
     * @param needle the string to look for
     * @param start where to look from
     * @return the index of the first occurrence
     */
    public static int indexOfIgnoreCase(String haystack, String needle, int start) {
        if (needle.isEmpty() || haystack.isEmpty()) {
            // Fallback to legacy behavior.
            return haystack.indexOf(needle);
        }
        for (var i = start; i < haystack.length(); ++i) {
            // Early out, if possible.
            if (i + needle.length() > haystack.length()) {
                return -1;
            }

            // Attempt to match substring starting at position i of haystack.
            var j = 0;
            var k = i;
            while (k < haystack.length() && j < needle.length()) {
                var c = Character.toLowerCase(haystack.charAt(k));
                var c2 = Character.toLowerCase(needle.charAt(j));
                if (c != c2) {
                    break;
                }
                j++;
                k++;
            }
            // Walked all the way to the end of the needle, return the start
            // position that this was found at.
            if (j == needle.length()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Split a pattern at pipe characters, properly accounting for brackets and escapes
     * @param s the string to split
     * @param logger the logger
     * @return the split string
     */
    public static Optional<String[]> splitVerticalBars(String s, SkriptLogger logger) {
        List<String> split = new ArrayList<>();
        var sb = new StringBuilder();
        var chars = s.toCharArray();
        for (var i = 0; i < chars.length; i++) {
            var c = chars[i];
            if (c == '\\') {
                sb.append(c);
                if (i + 1 < s.length()) {
                    sb.append(chars[++i]);
                }
            } else if (c == '(' || c == '[' || c == '<') {
                var closing = c == '(' ? ')' : c == '[' ? ']' : '>';
                var text = getEnclosedText(s, c, closing, i);
                text.ifPresent(st -> sb.append(c).append(st).append(closing));
                if (text.isPresent()) {
                    i += text.get().length() + 1;
                } else {
                    logger.error("Unmatched bracket : '" + s.substring(i) + "'", ErrorType.MALFORMED_INPUT);
                    return Optional.empty();
                }
            } else if (c == '|') {
                split.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        split.add(sb.toString());
        return Optional.of(split.toArray(new String[0]));
    }

    /**
     * Returns an array of two elements, containing the plural and singular forms of the
     * given pluralizable expression. Does not support escaping.
     */
    public static String[] getForms(String pluralizable) {
        List<String[]> words = new ArrayList<>();
        for (var s : pluralizable.split("\\s+")) {
            var split = s.split("@");
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
        var pluralized = new String[]{"", ""};
        for (var word : words) {
            pluralized[0] += word[0] + " ";
            pluralized[1] += word[1] + " ";
        }
        return stripAll(pluralized);
    }

    /**
     * Trims all strings in the array
     * @param strings the strings
     * @return the array with all of its contents trimmed
     */
    private static String[] stripAll(String[] strings) {
        for (var i = 0; i < strings.length; i++)
            strings[i] = strings[i].strip();
        return strings;
    }

    /**
     * Adds a proper English indefinite article to a string
     * @param noun the string
     * @param plural whether it is plural or not
     * @return the string with its proper indefinite article
     */
    public static String withIndefiniteArticle(String noun, boolean plural) {
        noun = noun.strip();
        if (noun.isEmpty())
            return "";
        else if (plural)
            return noun;
        var first = Character.toLowerCase(noun.charAt(0));
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

    /**
     * Capitalizes the first word or all words in a string.
     * A word is separated by a space character.
     * @param str the string to change
     * @return the capitalized string
     */
    public static String toTitleCase(String str, boolean allWords) {
        if (!allWords) {
            return Character.toUpperCase(str.charAt(0)) + str.substring(1);
        } else {
            String[] words = str.split(" ");

            for (int i = 0; i < words.length; i++) {
                words[i] = Character.toUpperCase(words[i].charAt(0)) + words[i].substring(1);
            }
            return String.join(" ", words);
        }
    }

    public static String toCamelCase(String str, boolean firstNoCase) {
        String[] parts = str.split("\\s+");
        StringBuilder ret = new StringBuilder();
        for (String part : parts) {
            if (firstNoCase) {
                firstNoCase = false;
                ret.append(Character.toLowerCase(part.charAt(0)))
                        .append(part.substring(1));
            } else {
                ret.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1));
            }
        }
        return ret.toString();
    }

    /**
     * Converts a string into snake case.
     * @param str the string to convert
     * @param mode 0 for default mode, 1 for uppercase and 2 for lowercase
     * @return the converted string
     */
    public static String toSnakeCase(String str, int mode) {
        if (mode == 0)
            return str.replace(' ', '_');
        StringBuilder sb = new StringBuilder();
        for (int c : (Iterable<Integer>) str.codePoints()::iterator) { // Handles Unicode!
            sb.appendCodePoint(c == ' ' ? '_' : (mode == 1 ? Character.toUpperCase(c) : Character.toLowerCase(c)));
        }
        return sb.toString();
    }

    /**
     * Converts a string into kebab case.
     * @param str the string to convert
     * @param mode 0 for default mode, 1 for uppercase and 2 for lowercase
     * @return the converted string
     */
    public static String toKebabCase(String str, int mode) {
        if (mode == 0)
            return str.replace(' ', '-');
        StringBuilder sb = new StringBuilder();
        for (int c : (Iterable<Integer>) str.codePoints()::iterator) { // Handles Unicode!
            sb.appendCodePoint(c == ' ' ? '-' : (mode == 1 ? Character.toUpperCase(c) : Character.toLowerCase(c)));
        }
        return sb.toString();
    }

    /**
     * Replaces all uppercase characters with lower case ones and vice versa.
     * @param str the string to change
     * @return the reversed string
     */
    public static String toReversedCase(String str) {
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c))
                chars[i] = Character.toLowerCase(c);
            else if (Character.isLowerCase(c))
                chars[i] = Character.toUpperCase(c);
            else
                chars[i] = c;
        }
        return new String(chars);
    }

    /**
     * Mirrors the string. This means the last character will be put first and vice versa.
     * @param str the string to change
     * @return the mirrored string
     */
    public static String mirrored(String str) {
        char[] chars = str.toCharArray();
        char[] ret = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            ret[i] = chars[chars.length - i - 1];
        }
        return new String(ret);
    }
}
