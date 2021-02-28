package io.github.syst3ms.skriptparser.event;

import io.github.syst3ms.skriptparser.lang.TriggerContext;

/**
 * The periodical context.
 */
public class PeriodicalContext implements TriggerContext {
    @Override
    public String getName() {
        return "periodical";
    }
}
