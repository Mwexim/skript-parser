package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.event.ScriptLoadContext;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;

import java.util.ArrayList;
import java.util.List;

public class Skript extends SkriptAddon {
    private final String[] mainArgs;
    private List<Trigger> mainTriggers = new ArrayList<>();

    public Skript(String[] mainArgs) {
        this.mainArgs = mainArgs;
    }

    @Override
    public void handleTrigger(Trigger trigger) {
        mainTriggers.add(trigger);
    }

    @Override
    public void finishedLoading() {
        for (Trigger trigger : mainTriggers) {
            Effect.runAll(trigger, new ScriptLoadContext(mainArgs));
        }
    }
}
