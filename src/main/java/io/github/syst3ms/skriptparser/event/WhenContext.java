package io.github.syst3ms.skriptparser.event;

import io.github.syst3ms.skriptparser.lang.TriggerContext;

/**
 * The when-event context.
 */
public class WhenContext implements TriggerContext {
    @Override
    public String getName() {
        return "when";
    }
}
