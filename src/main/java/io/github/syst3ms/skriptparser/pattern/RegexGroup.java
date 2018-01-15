package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.classes.SkriptParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A group containing a regex in the form of a {@link Pattern}.
 */
public class RegexGroup implements PatternElement {
    private Pattern pattern;

    public RegexGroup(Pattern pattern) {
        this.pattern = pattern;
    }


    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof RegexGroup && pattern.pattern().equals(((RegexGroup) obj).pattern.pattern());
    }

	@Override
	public int match(String s, int index, SkriptParser parser) {
		Matcher m = pattern.matcher(s);
		m.region(index, s.length());
		if (!m.lookingAt()) {
			return -1;
		}
		String match = m.group();
		parser.addRegexMatch(match);
		return index + match.length();
	}
}
