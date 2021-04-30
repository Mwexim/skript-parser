package io.github.syst3ms.skriptparser;

import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.syntax.EvtTest;
import io.github.syst3ms.skriptparser.syntax.TestContext;

import java.util.ArrayList;
import java.util.List;

public class TestAddon extends SkriptAddon {
    private final List<Trigger> testTriggers = new ArrayList<>();

    @Override
    public void handleTrigger(Trigger trigger) {
        SkriptEvent event = trigger.getEvent();

        if (!canHandleEvent(event))
            return;

        if (event instanceof EvtTest) {
            testTriggers.add(trigger);
        }
    }

    @Override
    public void finishedLoading() {
        for (Trigger trigger : testTriggers) {
            Statement.runAll(trigger, new TestContext.RealTestContext());
        }

        // Clear triggers for next test.
        testTriggers.clear();
    }
}
