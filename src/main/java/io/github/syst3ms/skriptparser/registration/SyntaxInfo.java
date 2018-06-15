package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.List;

public class SyntaxInfo<C> {
    private Class<C> c;
    private List<PatternElement> patterns;
    private int priority;
    private SkriptAddon registerer;

    public SyntaxInfo(Class<C> c, List<PatternElement> patterns, int priority, SkriptAddon registerer) {
        this.c = c;
        this.patterns = patterns;
        this.priority = priority;
        this.registerer = registerer;
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

    public SkriptAddon getRegisterer() {
        return registerer;
    }
}
