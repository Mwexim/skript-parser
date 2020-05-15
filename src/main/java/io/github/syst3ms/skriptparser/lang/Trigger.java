package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

/**
 * A top-level section, that is not contained in code.
 * Usually declares an event.
 */
public class Trigger extends CodeSection {
    private final SkriptEvent event;

    public Trigger(SkriptEvent event) {
        this.event = event;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        return true;
    }

    @Override
    public void loadSection(FileSection section, SkriptLogger logger) {
        setItems(event.loadSection(section, logger));
    }

    @Override
    protected Statement walk(TriggerContext ctx) {
        return getFirst();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return event.toString(ctx, debug);
    }

    public SkriptEvent getEvent() {
        return event;
    }
}
