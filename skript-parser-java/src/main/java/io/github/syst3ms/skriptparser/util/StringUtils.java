package io.github.syst3ms.skriptparser.util;

public class StringUtils {
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
