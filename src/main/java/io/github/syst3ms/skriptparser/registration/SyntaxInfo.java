package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.SyntaxElement;
import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class containing info about a {@link SyntaxElement} that isn't an {@link Expression} or an {@link SkriptEvent}
 * @param <C> the {@link SyntaxElement} class
 */
public class SyntaxInfo<C> {
    private final Class<C> c;
    private final List<PatternElement> patterns;
    private final int priority;
    private final SkriptAddon registerer;
    protected final Map<String, Object> data;

    public SyntaxInfo(SkriptAddon registerer, Class<C> c, int priority, List<PatternElement> patterns) {
        this(registerer, c, priority, patterns, new HashMap<>());
    }

    public SyntaxInfo(SkriptAddon registerer, Class<C> c, int priority, List<PatternElement> patterns, Map<String, Object> data) {
        this.c = c;
        this.patterns = patterns;
        this.priority = priority;
        this.registerer = registerer;
        this.data = data;
    }

    public SkriptAddon getRegisterer() {
        return registerer;
    }

    public Class<C> getSyntaxClass() {
        return c;
    }

    public int getPriority() {
        return priority;
    }

    public List<PatternElement> getPatterns() {
        return patterns;
    }

    /**
     * Retrieves a data instance by its identifier.
     * @param identifier the identifier
     * @param type the expected data type
     * @return the data instance
     */
    public <T> T getData(String identifier, Class<T> type) {
        return type.cast(data.get(identifier));
    }
}
