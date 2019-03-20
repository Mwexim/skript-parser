package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;

import java.util.List;

/**
 * The entry point for all code in Skript. Once an event triggers, all of the code inside it is run
 */
public abstract class SkriptEvent implements SyntaxElement {

    /**
     * Whether this event should trigger, given the {@link TriggerContext}
     * @param ctx the TriggerContext to check
     * @return whether the event should trigger
     */
    public abstract boolean check(TriggerContext ctx);

    List<Statement> loadSection(FileSection section, SkriptLogger logger) {
        return ScriptLoader.loadItems(section, logger);
    }
}
