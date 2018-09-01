package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ParseResult;
import org.jetbrains.annotations.Nullable;

public class Trigger extends CodeSection {
    private final SkriptEvent event;

    public Trigger(SkriptEvent event) {
        this.event = event;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseResult parseResult) {
        return true;
    }

    @Override
    public void loadSection(FileSection section) {
        setItems(event.loadSection(section));
    }

    @Override
    protected Effect walk(TriggerContext e) {
        return getFirst();
    }

    @Override
    public String toString(@Nullable TriggerContext e, boolean debug) {
        return event.toString(e, debug);
    }
}
