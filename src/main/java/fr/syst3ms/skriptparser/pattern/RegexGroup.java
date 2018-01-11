package fr.syst3ms.skriptparser.pattern;

import java.util.regex.Pattern;

public class RegexGroup implements PatternElement {
    private Pattern pattern;

    public RegexGroup(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public int match(String s, int index) {
        // TODO
        return 0;
    }
}
