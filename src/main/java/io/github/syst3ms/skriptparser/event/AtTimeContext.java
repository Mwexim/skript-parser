package io.github.syst3ms.skriptparser.event;

import io.github.syst3ms.skriptparser.lang.TriggerContext;

/**
 * The at time-event context.
 */
public class AtTimeContext implements TriggerContext {
    @Override
    public String getName() {
        return "at time";
    }
}
