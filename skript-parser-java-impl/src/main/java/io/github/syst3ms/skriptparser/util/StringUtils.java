package io.github.syst3ms.skriptparser.util;

import java.util.StringJoiner;

public class StringUtils {
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
}
