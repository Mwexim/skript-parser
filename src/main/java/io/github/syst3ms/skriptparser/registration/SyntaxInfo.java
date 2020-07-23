package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.List;

/**
 * A class containing info about a {@link SyntaxElement} that isn't an {@link Expression} or an {@link SkriptEvent}
 * @param <C> the {@link SyntaxElement} class
 */
public class SyntaxInfo<C> {
    private final Class<C> c;
    private final List<PatternElement> patterns;
    private final int priority;
    private final SkriptAddon registerer;

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
