package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.event.ScriptLoadContext;
import io.github.syst3ms.skriptparser.event.ScriptLoadEvent;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link SkriptAddon} representing Skript itself
 */
public class Skript extends SkriptAddon {
    private final String[] mainArgs;
    private final List<Trigger> mainTriggers = new ArrayList<>();

    public Skript(String[] mainArgs) {
        this.mainArgs = mainArgs;
    }

    @Override
    public void handleTrigger(Trigger trigger) {
        SkriptEvent event = trigger.getEvent();
        if (!canHandleEvent(event))
            return;
        if (event instanceof ScriptLoadEvent)
            mainTriggers.add(trigger);
    }

    @Override
    public void finishedLoading() {
        for (Trigger trigger : mainTriggers) {
            Statement.runAll(trigger, new ScriptLoadContext(mainArgs));
        }
    }
}
