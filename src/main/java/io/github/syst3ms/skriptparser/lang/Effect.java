package io.github.syst3ms.skriptparser.lang;

import io.github.syst3ms.skriptparser.event.TriggerContext;

public abstract class Effect extends Statement {
    protected abstract void execute(TriggerContext ctx);

    @Override
    public boolean run(TriggerContext ctx) {
        execute(ctx);
        return true;
    }
}
