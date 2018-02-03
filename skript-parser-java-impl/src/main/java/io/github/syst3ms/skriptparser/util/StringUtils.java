package io.github.syst3ms.skriptparser.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static final Pattern R_LITERAL_CONTENT_PATTERN = Pattern.compile("^(.+?)\\((.+)\\)\\1$"); // It's actually rare to be able to use '.+' raw like this

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

    public static String join(String... strings) {
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static String getEnclosedText(String pattern, char opening, char closing, int start) {
        int n = 0;
        for (int i = start; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == closing) {
                n--;
                if (n == 0) {
                    return pattern.substring(start + 1, i); // We don't want the beginning bracket in there
                }
            } else if (c == opening) {
                n++;
            }
        }
        return null;
    }

    /**
     * Parses a string literal. Supports the following formats :
     * <ul>
     *     <li>Plain old string literal, <strong>characters are escaped using \\</strong></li>
     *     <li>Simple quote raw literal, only simple quotes can be escaped, all the rest corresponds to raw text</li>
     *     <li>C-style R-literals, see definition (among other string literals') <a href="http://en.cppreference.com/w/cpp/language/string_literal">here</a></li>
     * </ul>
     * "Constant" as in "doesn't have variables" (VariableString will be a thing)
     * @param s The string to parse
     * @return the parsed {@link String}, or {@literal null}
     */
    public static String parseConstantString(String s) {
        String parsed = parseSimpleString(s);
        if (parsed == null) {
            parsed = parseSimpleQuoteLiteral(s);
            if (parsed == null) {
                parsed = parseRRawLiteral(s);
                if (parsed == null) {
                    return null;
                }
            }
        }
        return parsed;
    }


    private static String parseSimpleString(String s) {
        if (!s.startsWith("\"") || !s.endsWith("\""))
            return null;
        String withoutEscapes = s.replaceAll("\\\\.", "\0\0"); // Removing character escapes, let's not get bothered by quote escapes
        int start = withoutEscapes.indexOf('"');
        if (start == -1)
            return null;
        int end = withoutEscapes.indexOf('"', start + 1);
        if (end == -1)
            return null;
        return s.substring(start + 1, end).replaceAll("\\\\(.)", "$1");
    }

    private static String parseSimpleQuoteLiteral(String s) {
        if (!s.startsWith("'") || !s.endsWith("'"))
            return null;
        String withoutEscapes = s.replaceAll("\\\\'", "\0\0"); // Same thing, but we only have to worry about single quote escaping here
        int start = withoutEscapes.indexOf('\'');
        if (start == -1)
            return null;
        int end = withoutEscapes.indexOf('\'', start + 1);
        if (end == -1)
            return null;
        return s.substring(start + 1, end).replaceAll("\\\\'", "'");
    }

    /*
     * This is not a typo. This parses C-style "R-literals".
     * Example : R"delimiter(What are "escapes" ?)delimiter" turns into 'What are "escapes" ?'
     */
    private static String parseRRawLiteral(String s) {
        if (!s.startsWith("R\"") || !s.endsWith("\""))
            return null;
        String content = s.substring(2, s.length() - 1);
        Matcher m = R_LITERAL_CONTENT_PATTERN.matcher(content);
        if (!m.matches())
            return null;
        return m.group(2);
    }
}
