package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.parsing.SkriptParser;

import java.util.List;
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

    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof RegexGroup)) {
            return false;
        } else  {
            RegexGroup other = (RegexGroup) obj;
            return pattern.pattern().equals(other.pattern.pattern());
        }
    }

    @Override
    public int match(String s, int index, SkriptParser parser) {
        if (parser.getOriginalElement().equals(this))
            parser.advanceInPattern();
        List<PatternElement> flattened = parser.flatten(parser.getOriginalElement());
        List<PatternElement> possibleInputs = parser.getPossibleInputs(flattened.subList(parser.getPatternIndex(), flattened.size()));
        for (PatternElement possibleInput : possibleInputs) {
            if (possibleInput instanceof TextElement) {
                String text = ((TextElement) possibleInput).getText();
                Matcher m;
                if (text.equals("\0")) { // End of line
                    m = pattern.matcher(s).region(index - 1, s.length());
                } else {
                    int i = s.indexOf(text, index);
                    if (i == -1)
                        continue;
                    m = pattern.matcher(s).region(index - 1, i + 1);
                }
                /*
                 * matches() tries to match against the whole region, and that's what we want
                 */
                if (!m.matches())
                    continue;
                parser.addRegexMatch(m.toMatchResult());
                String content = m.group();
                return index + content.length();
            } else {
                assert possibleInput instanceof RegexGroup;
                Matcher boundMatcher = ((RegexGroup) possibleInput).getPattern().matcher(s).region(index, s.length());
                while (boundMatcher.lookingAt()) {
                    int i = boundMatcher.start();
                    if (i == -1)
                        continue;
                    Matcher m = pattern.matcher(s).region(index, i + 1);
                    /*
                     * matches() tries to match against the whole region, and that's what we want
                     */
                    if (m.matches()) {
                        parser.addRegexMatch(m.toMatchResult());
                        String content = m.group();
                        return index + content.length();
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return "<" + pattern.pattern() + ">";
    }
}
