package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.Nullable;

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
    public void loadSection(FileSection section) {
        setItems(event.loadSection(section));
    }

    @Override
    protected Statement walk(TriggerContext ctx) {
        return getFirst();
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return event.toString(ctx, debug);
    }
}
