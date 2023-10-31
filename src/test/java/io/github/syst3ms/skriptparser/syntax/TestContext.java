package io.github.syst3ms.skriptparser.syntax;

import io.github.syst3ms.skriptparser.lang.TriggerContext;

import java.time.Duration;

/**
 * The script loading context, which corresponds to running code inside {@code public static void main(String[] args)}
 * in Java.
 */
public class TestContext implements TriggerContext {
    @Override
    public String getName() {
        return "main";
    }

    public String patternValue() {
        return "It works too";
    }

    public Duration oneDay() {
        return Duration.ofDays(1);
    }

    // We want to see if inherited methods are able to register values as well.
    public static class SubTestContext extends TestContext { /* Nothing */ }
}
