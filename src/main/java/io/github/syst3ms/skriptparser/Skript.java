package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.event.AtTimeContext;
import io.github.syst3ms.skriptparser.event.EvtAtTime;
import io.github.syst3ms.skriptparser.event.EvtPeriodical;
import io.github.syst3ms.skriptparser.event.EvtScriptLoad;
import io.github.syst3ms.skriptparser.event.EvtWhen;
import io.github.syst3ms.skriptparser.event.PeriodicalContext;
import io.github.syst3ms.skriptparser.event.ScriptLoadContext;
import io.github.syst3ms.skriptparser.event.WhenContext;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.base.ExecutableExpression;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.util.ThreadUtils;
import io.github.syst3ms.skriptparser.util.TimeUtils;
import io.github.syst3ms.skriptparser.util.classes.Time;

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
            ThreadUtils.runPeriodically(() -> Statement.runAll(trigger, ctx), TimeUtils.TICK);
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

    @Override
    public void walkingForward() {
        ExecutableExpression.getCachedValues().clear();
    }
}
