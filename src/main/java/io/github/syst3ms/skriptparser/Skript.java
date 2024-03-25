package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.event.*;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.structures.functions.StructFunction;
import io.github.syst3ms.skriptparser.util.DurationUtils;
import io.github.syst3ms.skriptparser.util.ThreadUtils;
import io.github.syst3ms.skriptparser.util.Time;

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
    private final List<Trigger> whenTriggers = new ArrayList<>();
    private final List<Trigger> atTimeTriggers = new ArrayList<>();

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
        } else if (event instanceof EvtWhen) {
            whenTriggers.add(trigger);
        } else if (event instanceof EvtAtTime) {
            atTimeTriggers.add(trigger);
        } else if (event instanceof StructFunction function) {
            function.register(trigger);
        }
    }

    @Override
    public void finishedLoading() {
        for (Trigger trigger : mainTriggers) {
            Statement.runAll(trigger, new ScriptLoadContext(mainArgs));
        }
        for (Trigger trigger : periodicalTriggers) {
            var ctx = new PeriodicalContext();
            var dur = ((EvtPeriodical) trigger.getEvent()).getDuration().getSingle().orElseThrow(AssertionError::new);
            ThreadUtils.runPeriodically(() -> Statement.runAll(trigger, ctx), dur);
        }
        for (Trigger trigger : whenTriggers) {
            var ctx = new WhenContext();
            ThreadUtils.runPeriodically(() -> Statement.runAll(trigger, ctx), Duration.ofMillis(DurationUtils.TICK));
        }
        for (Trigger trigger : atTimeTriggers) {
            var ctx = new AtTimeContext();
            var time = ((EvtAtTime) trigger.getEvent()).getTime().getSingle().orElseThrow(AssertionError::new);
            var initialDelay = (Time.now().getTime().isAfter(time.getTime())
                    ? Time.now().difference(Time.LATEST).plus(time.difference(Time.MIDNIGHT))
                    : Time.now().difference(time));
            ThreadUtils.runPeriodically(() -> Statement.runAll(trigger, ctx), initialDelay, Duration.ofDays(1));
        }
    }

}
