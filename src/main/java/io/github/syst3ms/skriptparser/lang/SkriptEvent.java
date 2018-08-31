package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.TriggerContext;

public abstract class SkriptEvent extends CodeSection {
    public abstract boolean check(TriggerContext context);
}
