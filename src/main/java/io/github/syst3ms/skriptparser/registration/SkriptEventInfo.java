package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.pattern.PatternElement;

import java.util.List;

/**
 * A class containing info about an {@link SkriptEvent event} syntax
 * @param <E> the {@link SkriptEvent} class
 */
public class SkriptEventInfo<E extends SkriptEvent> extends SyntaxInfo<E> {
    private final Class<? extends TriggerContext>[] contexts;

    public SkriptEventInfo(Class<E> c, Class<? extends TriggerContext>[] handledContexts, List<PatternElement> patterns, int priority, SkriptAddon registerer) {
        super(c, patterns, priority, registerer);
        this.contexts = handledContexts;
    }

    /**
     * @return the list of all {@link TriggerContext}s this event is able to handle.
     */
    public Class<? extends TriggerContext>[] getContexts() {
        return contexts;
    }
}
