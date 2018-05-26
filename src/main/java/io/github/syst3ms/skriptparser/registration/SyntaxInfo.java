package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.List;

public class SyntaxInfo<C> {
    private Class<C> c;
    private List<PatternElement> patterns;
    private int priority;

    public SyntaxInfo(Class<C> c, List<PatternElement> patterns, int priority) {
        this.c = c;
        this.patterns = patterns;
        this.priority = priority;
    }

    public List<PatternElement> getPatterns() {
        return patterns;
    }

    public Class<C> getSyntaxClass() {
        return c;
    }

    public int getPriority() {
        return priority;
    }
}
