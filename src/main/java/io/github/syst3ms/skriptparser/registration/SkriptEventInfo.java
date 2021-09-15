package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class containing info about an {@link SkriptEvent event} syntax
 * @param <E> the {@link SkriptEvent} class
 */
public class SkriptEventInfo<E extends SkriptEvent> extends SyntaxInfo<E> {
    private final Set<Class<? extends TriggerContext>> contexts;

    public SkriptEventInfo(SkriptAddon registerer, Class<E> c, Set<Class<? extends TriggerContext>> handledContexts, int priority, List<PatternElement> patterns, Map<String, Object> data) {
        super(registerer, c, priority, patterns, data);
        this.contexts = handledContexts;
    }

    /**
     * @return the list of all {@link TriggerContext}s this event is able to handle.
     */
    public Set<Class<? extends TriggerContext>> getContexts() {
        return contexts;
    }
}
