package io.github.syst3ms.skriptparser.registration;

import io.github.syst3ms.skriptparser.lang.Trigger;

public abstract class SkriptAddon {
    private String name;

    public abstract void handleTrigger(Trigger trigger);
}
