package io.github.syst3ms.skriptparser.util;

public class StringUtils {
    public static int count(String s, CharSequence... toFind) {
        int count = 0;
        for (CharSequence sequence : toFind) {
            count += (s.length() - s.replace(sequence, "").length()) / sequence.length();
        }
        return count;
    }
}
