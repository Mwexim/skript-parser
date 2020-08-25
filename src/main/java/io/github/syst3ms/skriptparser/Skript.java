package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.event.EvtPeriodical;
import io.github.syst3ms.skriptparser.event.PeriodicalContext;
import io.github.syst3ms.skriptparser.event.ScriptLoadContext;
import io.github.syst3ms.skriptparser.event.EvtScriptLoad;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.util.ThreadUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link SkriptAddon} representing Skript itself
 */
public class Skript extends SkriptAddon {

    private final String[] mainArgs;

    private final List<Trigger> mainTriggers = new ArrayList<>();
    private final List<Trigger> periodicalTriggers = new ArrayList<>();

    public Skript(String[] mainArgs) {
        this.mainArgs = mainArgs;
    }

    @Override
    public void handleTrigger(Trigger trigger) {
        SkriptEvent event = trigger.getEvent();

        if (!canHandleEvent(event))
            return;

        if (event instanceof EvtScriptLoad) {
            mainTriggers.add(trigger);
        } else if (event instanceof EvtPeriodical) {
            periodicalTriggers.add(trigger);
        }
    }

    @Override
    public void finishedLoading() {
        for (Trigger trigger : mainTriggers) {
            Statement.runAll(trigger, new ScriptLoadContext(mainArgs));
        }
        for (Trigger trigger : periodicalTriggers) {
            PeriodicalContext ctx = new PeriodicalContext();
            Duration dur = ((EvtPeriodical) trigger.getEvent()).getDuration().getSingle(ctx).orElseThrow(AssertionError::new);
            ThreadUtils.runPeriodically(() -> Statement.runAll(trigger, ctx), dur);
        }
    }
}
