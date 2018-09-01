package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.TriggerContext;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;

import java.util.List;

public abstract class SkriptEvent implements SyntaxElement {
    public abstract boolean check(TriggerContext context);

    List<Effect> loadSection(FileSection section) {
        return ScriptLoader.loadItems(section);
    }
}
