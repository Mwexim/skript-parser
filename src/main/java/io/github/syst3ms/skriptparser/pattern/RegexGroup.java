package io.github.syst3ms.skriptparser.pattern;

import io.github.syst3ms.skriptparser.parsing.MatchContext;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A group containing a regex in the form of a {@link Pattern}.
 */
public class RegexGroup implements PatternElement {
    private final Pattern pattern;

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
            var other = (RegexGroup) obj;
            return pattern.pattern().equals(other.pattern.pattern());
        }
    }

    @Override
    public int match(String s, int index, MatchContext context) {
        var source = context.getSource();
        var flattened = PatternElement.flatten(context.getOriginalElement());
        var possibilityIndex = context.getPatternIndex();
        while (source.isPresent() && possibilityIndex >= flattened.size()) {
            flattened = PatternElement.flatten(source.get().getOriginalElement());
            possibilityIndex = source.get().getPatternIndex();
            source = source.get().getSource();
        }
        var possibleInputs = PatternElement.getPossibleInputs(flattened.subList(possibilityIndex, flattened.size()));
        for (var possibleInput : possibleInputs) {
            if (possibleInput instanceof TextElement) {
                var text = ((TextElement) possibleInput).getText();
                Matcher m;
                if (text.equals("\0")) { // End of line
                    m = pattern.matcher(s).region(index, s.length());
                } else {
                    var i = s.indexOf(text, index);
                    if (i == -1)
                        continue;
                    m = pattern.matcher(s).region(index, i);
                }
                /*
                 * matches() tries to match against the whole region, and that's what we want
                 */
                if (!m.matches())
                    continue;
                context.addRegexMatch(m.toMatchResult());
                var content = m.group();
                return index + content.length();
            } else {
                assert possibleInput instanceof RegexGroup;
                var boundMatcher = ((RegexGroup) possibleInput).getPattern().matcher(s).region(index, s.length());
                while (boundMatcher.lookingAt()) {
                    var i = boundMatcher.start();
                    if (i == -1)
                        continue;
                    var m = pattern.matcher(s).region(index, i + 1);
                    /*
                     * matches() tries to match against the whole region, and that's what we want
                     */
                    if (m.matches()) {
                        context.addRegexMatch(m.toMatchResult());
                        var content = m.group();
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
