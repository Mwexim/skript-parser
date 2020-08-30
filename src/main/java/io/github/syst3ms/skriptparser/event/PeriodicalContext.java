package io.github.syst3ms.skriptparser.event;

import io.github.syst3ms.skriptparser.lang.TriggerContext;

/**
 * The script loading context, which corresponds to running code inside {@code public static void main(String[] args)}
 * in Java.
 */
public class PeriodicalContext implements TriggerContext {

    @Override
    public String getName() {
        return "periodical";
    }
}
